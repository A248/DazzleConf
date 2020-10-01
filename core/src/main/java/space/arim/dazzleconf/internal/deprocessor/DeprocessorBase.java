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
package space.arim.dazzleconf.internal.deprocessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.ImmutableCollections;
import space.arim.dazzleconf.internal.NestedConfEntry;
import space.arim.dazzleconf.internal.SingleConfEntry;
import space.arim.dazzleconf.serialiser.ValueSerialiser;

/**
 * Base class for deprocessors. A deprocessor is responsible for serialising config values. While this
 * class handles most of such, subclasses are tasked with handling the config values post deprocessing.
 * 
 * @author A248
 *
 * @param <C> the type of the configuration
 */
abstract class DeprocessorBase<C> {

	final ConfigurationOptions options;
	private final ConfigurationDefinition<?> definition;
	private final C configData;
	
	private String key;
	
	DeprocessorBase(ConfigurationOptions options, ConfigurationDefinition<C> definition, C configData) {
		this.options = options;
		this.definition = definition;
		this.configData = configData;
	}
	
	abstract void finishSimple(String key, ConfEntry entry, Object value);
	
	abstract <N> void continueNested(String key, NestedConfEntry<N> childEntry, N childConf);
	
	void deprocess() {
		for (ConfEntry entry : definition.getEntries()) {
			String key = entry.getKey();
			this.key = key;
			deprocessEntry(entry);
		}
	}
	
	private void deprocessEntry(ConfEntry entry) {
		Method method = entry.getMethod();
		Object value;
		try {
			value = method.invoke(configData);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException("Exception while invoking implementation of " + entry.getQualifiedMethodName()
					+ " in " + configData.getClass().getName(), ex);
		}
		if (entry instanceof NestedConfEntry) {
			preContinueNested((NestedConfEntry<?>) entry, value);
		} else {
			Object postValue = deprocessObjectAtEntry(entry, value);
			finishSimple(key, entry, postValue);
		}
	}
	
	private <N> void preContinueNested(NestedConfEntry<N> childEntry, Object childConf) {
		continueNested(key, childEntry, childEntry.getDefinition().getConfigClass().cast(childConf));
	}
	
	private Object deprocessObjectAtEntry(ConfEntry entry, Object value) {
		return deprocessObjectAtEntryWithGoal((SingleConfEntry) entry, entry.getMethod().getReturnType(), value);
	}
	
	private <G> Object deprocessObjectAtEntryWithGoal(SingleConfEntry entry, Class<G> goal, Object value) {

		if (goal == List.class || goal == Set.class || goal == Collection.class) {
			List<Object> serialised = new ArrayList<>();
			for (Object element : (Collection<?>) value) {
				serialised.add(deprocessObjectAtEntryWithGoal(entry, entry.getCollectionElementType(), element));
			}
			return ImmutableCollections.listOf(serialised);
		}

		if (goal == String.class) {
			return value;
		}
		if (goal == char.class || goal == Character.class) {
			return String.valueOf(value);
		}
		if (value instanceof Boolean || value instanceof Number) { // Includes boxes and primitives
			return value;
		}
		if (value instanceof Enum) {
			return ((Enum<?>) value).name();
		}
		return fromSerialiser(getSerialiser(goal), goal.cast(value));
	}
	
	private <G> ValueSerialiser<G> getSerialiser(Class<G> goal) {
		ValueSerialiser<G> serialiser  = definition.getSerialisers().getSerialiser(goal);
		if (serialiser == null) {
			throw new RuntimeException("Internal error: no ValueSerialiser for " + goal + " at entry " + key);
		}
		return serialiser;
	}
	
	private <G> Object fromSerialiser(ValueSerialiser<G> serialiser, G value) {
		Object serialised = serialiser.serialise(value);
		if (serialised == null) {
			throw new IllDefinedConfigException(
					"At key " + key + ", ValueSerialiser#serialise for " + serialiser + " returned null");
		}
		return serialised;
	}
	
}
