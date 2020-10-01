/* 
 * DazzleConf-core
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-core. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.internal.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import space.arim.dazzleconf.AuxiliaryKeys;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.CollectionSize;
import space.arim.dazzleconf.annote.IntegerRange;
import space.arim.dazzleconf.annote.NumericRange;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.ImproperEntryException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.error.MissingKeyException;
import space.arim.dazzleconf.error.MissingValueException;
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.DefaultMethodUtil;
import space.arim.dazzleconf.internal.ImmutableCollections;
import space.arim.dazzleconf.internal.NestedConfEntry;
import space.arim.dazzleconf.internal.SingleConfEntry;
import space.arim.dazzleconf.serialiser.FlexibleType;
import space.arim.dazzleconf.serialiser.FlexibleTypeFunction;
import space.arim.dazzleconf.validator.ValueValidator;

public abstract class ProcessorBase {

	private final ConfigurationOptions options;
	private final ConfigurationDefinition<?> definition;
	private final Object auxiliaryValues;
	
	private String key;
	
	private final Map<String, Object> result = new HashMap<>();
	private boolean usedAuxiliary;
	
	/**
	 * Creates from options, definition, and auxiliary config values
	 * 
	 * @param options the config options
	 * @param definition the config definition
	 * @param auxiliaryValues the auxiliary config, null for none
	 */
	ProcessorBase(ConfigurationOptions options, ConfigurationDefinition<?> definition, Object auxiliaryValues) {
		this.options = options;
		this.definition = definition;
		this.auxiliaryValues = auxiliaryValues;
	}
	
	abstract ProcessorBase continueNested(ConfigurationOptions options, NestedConfEntry<?> childEntry,
			Object nestedAuxiliaryValues) throws ImproperEntryException;
	
	/**
	 * Creates a configuration using the specified processor
	 * 
	 * @param <C> the type of the config class
	 * @param definition the config definition
	 * @param processor the processor
	 * @return an instance of the config class
	 * @throws InvalidConfigException if the input to the processor is invalid for the configuration
	 */
	public static <C> C createConfig(ConfigurationDefinition<C> definition, ProcessorBase processor) throws InvalidConfigException {
		processor.process();

		ConfigInvocationHandler handler;
		if (definition.hasDefaultMethods()) {
			handler = new DefaultMethodConfigInvocationHandler(processor.result);
		} else {
			handler = new ConfigInvocationHandler(processor.result);
		}
		Class<C> configClass = definition.getConfigClass();
		Class<?>[] intf;
		if (processor.usedAuxiliary) {
			intf = new Class<?>[] {configClass, AuxiliaryKeys.class};
		} else {
			intf = new Class<?>[] {configClass};
		}
		Object proxy = Proxy.newProxyInstance(configClass.getClassLoader(), intf, handler);
		if (definition.hasDefaultMethods()) {
			((DefaultMethodConfigInvocationHandler) handler).initDefaultMethods(proxy, definition.getDefaultMethods());
		}
		return configClass.cast(proxy);
	}
	
	private void process() throws InvalidConfigException {
		for (ConfEntry entry : definition.getEntries()) {
			Method method = entry.getMethod();
			if (DefaultMethodUtil.isDefault(method)) {
				continue;
			}
			key = entry.getKey();
			String methodName = method.getName();
			Object value;
			if (entry instanceof NestedConfEntry) {
				value = getNestedSection((NestedConfEntry<?>) entry);
			} else {
				value = getSingleValue((SingleConfEntry) entry);
			}
			Object formerValue = result.put(methodName, value);
			if (formerValue != null) {
				throw new IllDefinedConfigException("Duplicate method name " + methodName);
			}
		}
	}
	
	private Object getNestedSection(NestedConfEntry<?> nestedEntry) throws InvalidConfigException {
		Object nestedAuxiliary = (auxiliaryValues == null) ?
				null : getAuxiliaryValue(nestedEntry); // Pass along auxiliary entries
		ProcessorBase childProcessor;
		try {
			childProcessor = continueNested(options, nestedEntry, nestedAuxiliary);
		} catch (MissingKeyException mke) {
			if (auxiliaryValues == null) {
				throw mke;
			}
			return getAuxiliaryValue(nestedEntry);
		}
		Object nestedSection = createConfig(nestedEntry.getDefinition(), childProcessor);
		if (childProcessor.usedAuxiliary) {
			usedAuxiliary = true; // propagate auxiliary usage flag upward
		}
		return nestedSection;
	}
	
	private Object getSingleValue(SingleConfEntry entry) throws InvalidConfigException {
		// Get pre value; if missing and auxiliary entries are provided, return auxiliary value
		Object preValue;
		try {
			preValue = getValueFromSources(entry);
		} catch (MissingKeyException mke) {
			if (auxiliaryValues == null) {
				throw mke;
			}
			return getAuxiliaryValue(entry);
		}
		if (preValue == null) {
			throw MissingValueException.forKey(key);
		}

		Object value = processObjectAtEntryWithGoal(entry, entry.getMethod().getReturnType(), preValue);

		ValueValidator validator = entry.getValidator();
		if (validator == null) {
			validator = options.getValidators().get(key);
		}
		if (validator != null) {
			validator.validate(key, value);
		}
		return value;
	}
	
	private Object getAuxiliaryValue(ConfEntry entry) {
		Object auxiliaryValue;
		try {
			auxiliaryValue = entry.getMethod().invoke(auxiliaryValues);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new IllDefinedConfigException(
					"Configuration " + auxiliaryValues + " threw an exception while getting value at " + key, ex);
		}
		if (auxiliaryValue == null) {
			throw new IllDefinedConfigException(
					"Configuration " + auxiliaryValues + " returned null while getting value at " + key);
		}
		usedAuxiliary = true;
		return auxiliaryValue;
	}
	
	private <G> Object processObjectAtEntryWithGoal(SingleConfEntry entry, Class<G> goal, Object preValue)
			throws BadValueException {
		FlexibleTypeImpl flexType = new FlexibleTypeImpl(key, preValue, options, definition.getSerialisers());

		Method method = entry.getMethod();

		// Numerics types can't be delegated to FlexibleType, since @IntegerRange/@NumericRange need to be checked
		if (goal == int.class || goal == Integer.class) {
			return getAsNumber(method, flexType).intValue();
		} else if (goal == long.class || goal == Long.class) {
			return getAsNumber(method, flexType).longValue();
		} else if (goal == short.class || goal == Short.class) {
			return getAsNumber(method, flexType).shortValue();
		} else if (goal == byte.class || goal == Byte.class) {
			return getAsNumber(method, flexType).byteValue();
		}
		if (goal == double.class || goal == Double.class) {
			return getAsNumber(method, flexType).doubleValue();
		} else if (goal == float.class || goal == Float.class) {
			return getAsNumber(method, flexType).floatValue();
		}
		// Same goes for Collections with @CollectionSize
		if (goal == List.class || goal == Set.class || goal == Collection.class) {
			Class<?> elementType = entry.getCollectionElementType();
			return getCollection(goal, flexType, method, elementType);
		}
		// And Maps with @CollectionSize
		if (goal == Map.class) {
			Class<?> keyType = entry.getMapKeyType();
			Class<?> valueType = entry.getMapValueType();
			return getMap(flexType, method, keyType, valueType);
		}

		// Everything else
		return flexType.getObject(goal);
	}
	
	private <E> Collection<E> getCollection(Class<?> goal, FlexibleType flexType, Method method, Class<E> elementClass)
			throws BadValueException {

		FlexibleTypeFunction<E> function = (element) -> element.getObject(elementClass);
		Collection<E> collection;
		if (goal == List.class) {
			collection = flexType.getList(function);
		} else if (goal == Set.class) {
			collection = flexType.getSet(function);
		} else if (goal == Collection.class) { 
			collection = flexType.getCollection(function);
		} else {
			throw new IllegalArgumentException("Internal error: Unknown goal " + goal + ", expected List/Set/Collection");
		}
		checkSize(method, collection.size());
		return collection;
	}
	
	private <K, V> Map<K, V> getMap(FlexibleType flexType, Method method, Class<K> keyClass, Class<V> valueClass)
			throws BadValueException {
		Map<K, V> map = flexType.getMap((flexibleKey, flexibleValue) -> {
			K key = flexibleKey.getObject(keyClass);
			V value = flexibleValue.getObject(valueClass);
			return ImmutableCollections.mapEntryOf(key, value);
		});
		checkSize(method, map.size());
		return map;
	}
	
	private void checkSize(Method method, int size) throws BadValueException {
		CollectionSize sizing = method.getAnnotation(CollectionSize.class);
		if (sizing != null) {
			if (size < sizing.min()) {
				throw new BadValueException.Builder().key(key)
						.message("value's size " + size + " is less than minimum size " + sizing.min()).build();
			}
			if (size > sizing.max()) {
				throw new BadValueException.Builder().key(key).message(
						"value's size " + size + " is more than maximum size " + sizing.max()).build();
			}
		}
	}
	
	private Number getAsNumber(Method method, FlexibleType flexType) throws BadValueException {
		Number number = flexType.getObject(Number.class);
		checkRange(method, number);
		return number;
	}
	
	private void checkRange(Method method, Number number) throws BadValueException {
		NumericRange numericRange = method.getAnnotation(NumericRange.class);
		if (numericRange != null) {
			double asDouble = number.doubleValue();
			if (asDouble < numericRange.min()) {
				throw new BadValueException.Builder().key(key)
						.message("value's size " + asDouble + " is less than minimum size " + numericRange.min()).build();
			}
			if (asDouble > numericRange.max()) {
				throw new BadValueException.Builder().key(key)
						.message("value's size " + asDouble + " is more than maximum size " + numericRange.max()).build();
			}
		}
		IntegerRange intRange = method.getAnnotation(IntegerRange.class);
		if (intRange != null) {
			long asLong = number.longValue();
			if (asLong < intRange.min()) {
				throw new BadValueException.Builder().key(key)
						.message("value's size " + asLong + " is less than minimum size " + intRange.min()).build();
			}
			if (asLong > intRange.max()) {
				throw new BadValueException.Builder().key(key)
						.message("value's size " + asLong + " is more than maximum size " + intRange.max()).build();
			}
		}
	}
	
	/**
	 * Retrieves the pre processing value for a config entry
	 * 
	 * @param entry the config entry
	 * @return the pre processed value
	 * @throws MissingKeyException if the key is not defined
	 */
	abstract Object getValueFromSources(ConfEntry entry) throws MissingKeyException;
	
}
