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
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.error.MissingKeyException;
import space.arim.dazzleconf.error.MissingValueException;
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.type.ReturnTypeWithConfigDefinition;
import space.arim.dazzleconf.internal.type.SimpleSubSectionReturnType;
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
			try {
				value = getProcessedValue(entry, getPreValue(entry));
			} catch (MissingKeyException mke) {
				// If missing and auxiliary entries are provided, use auxiliary value
				if (auxiliaryValues == null) {
					throw mke;
				}
				value = getAuxiliaryValue(entry);
			}
			Object formerValue = result.put(methodName, value);
			if (formerValue != null) {
				throw new IllDefinedConfigException("Duplicate method name " + methodName);
			}
		}
	}

	private Object getPreValue(ConfEntry entry) throws InvalidConfigException {
		Object preValue = getValueFromSources(entry);
		String key = entry.getKey();
		if (preValue == null) {
			throw MissingValueException.forKey(key);
		}
		return preValue;
	}
	
	private Object getProcessedValue(ConfEntry entry, Object preValue) throws InvalidConfigException {
		String key = entry.getKey();

		FlexibleType flexibleType = new FlexibleTypeImpl(key, preValue, options, definition.getSerialisers());
		Composition composition = new Composition(this, entry, preValue, flexibleType);
		Object value = composition.processObject();
		validate(entry, value);
		return value;
	}

	private void validate(ConfEntry entry, Object value) throws BadValueException {
		if (entry.returnType() instanceof SimpleSubSectionReturnType) {
			// ValueValidator not supported for simple sub sections
			return;
		}
		String key = entry.getKey();
		// Cannot use Optional#or due to JDK 8 compatibility
		ValueValidator validator = entry.getValidator().orElseGet(() -> options.getValidators().get(key));
		if (validator != null) {
			validator.validate(key, value);
		}
	}
	
	private Object getAuxiliaryValue(ConfEntry entry) {
		Object auxiliaryValue = auxiliaryValues.getEntryValue(entry);
		usedAuxiliary = true;
		return auxiliaryValue;
	}

	<N> N createNested(ConfEntry nestedEntry, ReturnTypeWithConfigDefinition<N, ?> returnType, Object preValue)
			throws InvalidConfigException {
		ConfigurationDefinition<N> nestedDefinition = returnType.configDefinition();
		N nestedAuxiliary = null;
		if (auxiliaryValues != null && returnType instanceof SimpleSubSectionReturnType) {
			Object auxiliaryValue = getAuxiliaryValue(nestedEntry);
			nestedAuxiliary = nestedDefinition.getConfigClass().cast(auxiliaryValue);
		}
		return createChildConfig(options, nestedDefinition, nestedEntry.getKey(), preValue, nestedAuxiliary);
	}

	<N> N createFromProcessor(ProcessorBase<N> childProcessor) throws InvalidConfigException {
		N childConfig = childProcessor.createConfig();
		if (childProcessor.usedAuxiliary) {
			usedAuxiliary = true; // propagate auxiliary usage flag upward
		}
		return childConfig;
	}

	/**
	 * Creates a child config from the following information
	 *
	 * @param options the configuration options
	 * @param childDefinition the child definition
	 * @param key the key, purely informative
	 * @param preValue the pre processing value
	 * @param nestedAuxiliaryValues any auxiliary values
	 * @param <N> the nested config type
	 * @return the child config
	 * @throws InvalidConfigException if something went wrong
	 */
	abstract <N> N createChildConfig(ConfigurationOptions options,
									 ConfigurationDefinition<N> childDefinition,
									 String key, Object preValue,
									 N nestedAuxiliaryValues) throws InvalidConfigException;
	
	/**
	 * Retrieves the pre processing value for a config entry
	 * 
	 * @param entry the config entry
	 * @return the pre processed value
	 * @throws InvalidConfigException if something went wrong
	 */
	abstract Object getValueFromSources(ConfEntry entry) throws InvalidConfigException;
	
}
