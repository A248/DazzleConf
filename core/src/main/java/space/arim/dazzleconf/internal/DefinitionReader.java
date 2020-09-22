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
package space.arim.dazzleconf.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.ConfSerialiser;
import space.arim.dazzleconf.annote.ConfValidator;
import space.arim.dazzleconf.annote.SubSection;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.serialiser.ValueSerialiser;
import space.arim.dazzleconf.sorter.ConfigurationSorter;
import space.arim.dazzleconf.validator.ValueValidator;

public class DefinitionReader<C> {

	private final Class<C> configClass;
	private final ConfigurationOptions options;
	
	private Method method;
	
	private final Set<Class<?>> nestedConfigDejaVu;
	
	private boolean foundDefaultMethods;
	private final List<ConfEntry> entries;
	
	DefinitionReader(Class<C> configClass, ConfigurationOptions options) {
		this(configClass, options, new HashSet<>());
	}
	
	private DefinitionReader(Class<C> configClass, ConfigurationOptions options,
			Set<Class<?>> nestedConfigDejaVu) {
		this.configClass = configClass;
		this.options = options;
		entries = new ArrayList<>();
		this.nestedConfigDejaVu = nestedConfigDejaVu;
	}
	
	private ConfigurationSorter sorter() {
		return options.getSorter();
	}
	
	private List<ConfEntry> readEntries() {
		if (!nestedConfigDejaVu.add(configClass)) {
			throw new IllDefinedConfigException("Circular nested configuration for " + configClass.getName());
		}
		for (Method method : configClass.getMethods()) {
			if (DefaultMethodUtil.isDefault(method)) {
				foundDefaultMethods = true;
				continue;
			}
			create(method);
		}
		boolean cleared = nestedConfigDejaVu.remove(configClass);
		assert cleared : configClass;
		// Sort entries
		var sorter = sorter();
		if (sorter != null) {
			entries.sort(sorter);
		}
		return ImmutableCollections.listOf(entries);
	}
	
	ConfigurationInfo<C> read() {
		return new ConfigurationInfo<>(configClass, options, readEntries(), foundDefaultMethods);
	}
	
	private void create(Method method) {
		if (method.getParameterCount() > 0) {
			throw new IllDefinedConfigException("Cannot use a method with parameters in a configuration");
		}
		this.method = method;
		ConfEntry toAdd = create0();
		boolean added = entries.add(toAdd);
		if (!added) {
			throw new IllDefinedConfigException("Duplicate key " + toAdd.getKey());
		}
	}
	
	private ConfEntry create0() {
		if (method.getAnnotation(SubSection.class) != null) {
			Class<?> configClass = method.getReturnType();
			if (!configClass.isInterface()) {
				throw new IllDefinedConfigException(configClass.getName() + " is not an interface");
			}
			DefinitionReader<?> nestedReader = new DefinitionReader<>(configClass, options, nestedConfigDejaVu);
			List<ConfEntry> nestedEntries = nestedReader.readEntries();
			return new NestedConfEntry<>(method, new ConfigurationDefinition<>(configClass, nestedEntries, foundDefaultMethods));
		}
		ValueSerialiser<?> serialiser = getSerialiser();
		ValueValidator validator = getValidator();
		return new SingleConfEntry(method, serialiser, validator);
	}
	
	private ValueSerialiser<?> getSerialiser() {
		ConfSerialiser chosenSerialiser = method.getAnnotation(ConfSerialiser.class);
		return (chosenSerialiser == null) ? null : instantiate(ValueSerialiser.class, chosenSerialiser.value());
	}
	
	private ValueValidator getValidator() {
		ConfValidator chosenValidator = method.getAnnotation(ConfValidator.class);
		return (chosenValidator == null) ? null : instantiate(ValueValidator.class, chosenValidator.value());
	}
	
	private <V> V instantiate(Class<V> intf, Class<? extends V> impl) {
		try {
			Method createMethod = impl.getDeclaredMethod("getInstance");
			if (Modifier.isStatic(createMethod.getModifiers()) && intf.isAssignableFrom(createMethod.getReturnType())) {
				return intf.cast(createMethod.invoke(null));
			}
		} catch (NoSuchMethodException ignored) {

		} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException | SecurityException ex) {
			throw uninstantiable(intf, ex);
		}
		try {
			return impl.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException ex) {
			throw uninstantiable(intf, ex);
		}
	}
	
	private IllDefinedConfigException uninstantiable(Class<?> intf, Throwable cause) {
		return new IllDefinedConfigException(
				"Unable to instantiate chosen " + intf.getSimpleName() + " for " + method.getName(), cause);
	}
	
}
