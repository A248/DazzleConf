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

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import space.arim.dazzleconf.AuxiliaryKeys;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.ImproperEntryException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.error.MissingKeyException;
import space.arim.dazzleconf.error.MissingValueException;
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.NestedConfEntry;
import space.arim.dazzleconf.internal.SingleConfEntry;
import space.arim.dazzleconf.internal.util.ConfigurationInvoker;
import space.arim.dazzleconf.serialiser.FlexibleType;
import space.arim.dazzleconf.validator.ValueValidator;

public abstract class ProcessorBase<C> {

	private final ConfigurationOptions options;
	private final ConfigurationDefinition<C> definition;
	
	/** Null if no auxiliary values provided */
	private final ConfigurationInvoker<C> auxiliaryValues;
	
	private final Map<String, Object> result = new HashMap<>();
	private boolean usedAuxiliary;
	
	/**
	 * Creates from options, definition, and auxiliary config values
	 * 
	 * @param options the config options
	 * @param definition the config definition
	 * @param auxiliaryValues the auxiliary config, null for none
	 */
	ProcessorBase(ConfigurationOptions options, ConfigurationDefinition<C> definition, C auxiliaryValues) {
		this.options = options;
		this.definition = definition;
		this.auxiliaryValues = (auxiliaryValues == null) ? null : new ConfigurationInvoker<>(auxiliaryValues);
	}
	
	abstract <N> ProcessorBase<N> continueNested(ConfigurationOptions options, NestedConfEntry<N> childEntry,
			N nestedAuxiliaryValues) throws ImproperEntryException;
	
	/**
	 * Creates a configuration
	 * 
	 * @return an instance of the config class
	 * @throws InvalidConfigException if the input to this processor is invalid for the configuration
	 */
	public C createConfig() throws InvalidConfigException {
		process();

		Class<C> configClass = definition.getConfigClass();
		Class<?>[] intf;
		if (usedAuxiliary) {
			intf = new Class<?>[] {configClass, AuxiliaryKeys.class};
		} else {
			intf = new Class<?>[] {configClass};
		}
		ClassLoader classLoader = configClass.getClassLoader();
		Object proxy;
		if (definition.hasDefaultMethods()) {
			DefaultMethodConfigInvocationHandler handler = new DefaultMethodConfigInvocationHandler(result);
			proxy = Proxy.newProxyInstance(classLoader, intf, handler);
			handler.initDefaultMethods(proxy, definition.getDefaultMethods());
		} else {
			proxy = Proxy.newProxyInstance(classLoader, intf, new ConfigInvocationHandler(result));
		}
		return configClass.cast(proxy);
	}
	
	private void process() throws InvalidConfigException {
		for (ConfEntry entry : definition.getEntries()) {
			String methodName = entry.getMethod().getName();
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
	
	private <N> N getNestedSection(NestedConfEntry<N> nestedEntry) throws InvalidConfigException {
		N nestedAuxiliary = (auxiliaryValues == null) ? null
				: getNestedAuxiliaryValue(nestedEntry); // Pass along auxiliary entries

		ProcessorBase<N> childProcessor;
		try {
			childProcessor = continueNested(options, nestedEntry, nestedAuxiliary);
		} catch (MissingKeyException mke) {
			if (auxiliaryValues == null) {
				throw mke;
			}
			return nestedAuxiliary;
		}
		N nestedSection = childProcessor.createConfig();
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
		String key = entry.getKey();
		if (preValue == null) {
			throw MissingValueException.forKey(key);
		}

		FlexibleType flexType = new FlexibleTypeImpl(key, preValue, options, definition.getSerialisers());
		Object value = new Composition(entry, flexType).processObject();

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
		Object auxiliaryValue = auxiliaryValues.getEntryValue(entry);
		usedAuxiliary = true;
		return auxiliaryValue;
	}
	
	private <N> N getNestedAuxiliaryValue(NestedConfEntry<N> nestedEntry) {
		Class<N> configClass = nestedEntry.getDefinition().getConfigClass();
		ConfEntry entry = nestedEntry;
		return configClass.cast(getAuxiliaryValue(entry));
	}
	
	/**
	 * Retrieves the pre processing value for a config entry
	 * 
	 * @param entry the config entry
	 * @return the pre processed value
	 * @throws MissingKeyException if the key is not defined
	 */
	abstract Object getValueFromSources(SingleConfEntry entry) throws MissingKeyException;
	
}
