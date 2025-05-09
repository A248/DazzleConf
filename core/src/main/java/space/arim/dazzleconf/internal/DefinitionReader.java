/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
 *
 * DazzleConf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DazzleConf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */

package space.arim.dazzleconf.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.ConfSerialisers;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.internal.type.TypeInfo;
import space.arim.dazzleconf.internal.type.TypeInfoCreation;
import space.arim.dazzleconf.internal.util.MethodUtil;
import space.arim.dazzleconf.serialiser.ValueSerialiser;
import space.arim.dazzleconf.serialiser.ValueSerialiserMap;

public final class DefinitionReader<C> {

	private final Class<C> configClass;
	final ConfigurationOptions options;

	private final Set<Class<?>> nestedConfigDejaVu;

	private final Set<Method> defaultMethods = new HashSet<>();
	private final Map<String, ConfEntry> entries = new LinkedHashMap<>();

	public DefinitionReader(Class<C> configClass, ConfigurationOptions options) {
		this(configClass, options, new HashSet<>());
	}

	private DefinitionReader(Class<C> configClass, ConfigurationOptions options,
			Set<Class<?>> nestedConfigDejaVu) {
		Objects.requireNonNull(configClass, "config class");
		if (!configClass.isInterface()) {
			throw new IllDefinedConfigException(configClass.getName() + " is not an interface");
		}
		this.configClass = configClass;
		this.options = options;
		this.nestedConfigDejaVu = nestedConfigDejaVu;
	}

	public ConfigurationDefinition<C> read() {
		ValueSerialiserMap serialiserMap = readSerialisers();
		List<ConfEntry> sortedEntries = readAndSortEntries();
		return new ConfigurationDefinition<>(configClass, sortedEntries, defaultMethods, serialiserMap);
	}

	public <N> ConfigurationDefinition<N> createChildDefinition(TypeInfo<N> configClassTypeInfo) {
		Class<N> configClass = configClassTypeInfo.rawType();
		DefinitionReader<N> reader = new DefinitionReader<>(configClass, options, nestedConfigDejaVu);
		return reader.read();
	}

	private ValueSerialiserMap readSerialisers() {
		ConfSerialisers confSerialisers = configClass.getAnnotation(ConfSerialisers.class);
		if (confSerialisers == null) {
			return options.getSerialisers();
		}
		Map<Class<?>, ValueSerialiser<?>> serialisers = new HashMap<>(options.getSerialisers().asMap());
		for (Class<? extends ValueSerialiser<?>> serialiserClass : confSerialisers.value()) {
			ValueSerialiser<?> serialiser = instantiate(ValueSerialiser.class, serialiserClass);
			serialisers.put(serialiser.getTargetClass(), serialiser);
		}
		return ValueSerialiserMap.of(serialisers);
	}

	private List<ConfEntry> readAndSortEntries() {
		if (!nestedConfigDejaVu.add(configClass)) {
			throw new IllDefinedConfigException("Circular nested configuration for " + configClass.getName());
		}
		for (Method method : configClass.getMethods()) {
			if (Modifier.isStatic(method.getModifiers())) {
				continue;
			}
			if (MethodUtil.isDefault(method)) {
				defaultMethods.add(method);
				continue;
			}
			create(method);
		}
		boolean cleared = nestedConfigDejaVu.remove(configClass);
		assert cleared : configClass;

		/*
		 * Sort entries
		 */
		List<ConfEntry> entriesList = new ArrayList<>(entries.values());
		options.getConfigurationSorter().ifPresent(entriesList::sort);
		return entriesList;
	}

	private void create(Method method) {
		ConfEntryCreation entryCreation = new ConfEntryCreation(
				this, method,
				new TypeInfoCreation(method.getAnnotatedReturnType()));
		ConfEntry entry = entryCreation.create();
		ConfEntry previous = entries.put(entry.getKey(), entry);
		if (previous != null) {
			throw new IllDefinedConfigException(
					"Duplicate key " + entry.getKey() + ". " +
					"It is not possible to have multiple configuration options using the same key.");
		}
	}

	<V> V instantiate(Class<V> intf, Class<? extends V> impl) {
		try {
			Method createMethod = impl.getDeclaredMethod("getInstance");
			if (Modifier.isStatic(createMethod.getModifiers()) && intf.isAssignableFrom(createMethod.getReturnType())) {
				return intf.cast(createMethod.invoke(null));
			}
		} catch (NoSuchMethodException ignored) {

		} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException | SecurityException ex) {
			throw uninstantiable(intf, impl, ex);
		}
		try {
			return impl.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException ex) {
			throw uninstantiable(intf, impl, ex);
		}
	}

	private <V> IllDefinedConfigException uninstantiable(Class<V> intf, Class<? extends V> impl, Throwable cause) {
		return new IllDefinedConfigException(
				"Unable to instantiate " + intf.getSimpleName() + " implemented by " + impl.getName(), cause);
	}

}
