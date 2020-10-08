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
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.ImmutableCollections;
import space.arim.dazzleconf.internal.NestedConfEntry;
import space.arim.dazzleconf.internal.SingleConfEntry;

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
	
	abstract void finishSimple(String key, SingleConfEntry entry, Object value);
	
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
			SingleConfEntry singleEntry = (SingleConfEntry) entry;
			Object postValue = deprocessObjectAtEntry(singleEntry, value);
			finishSimple(key, singleEntry, postValue);
		}
	}
	
	private <N> void preContinueNested(NestedConfEntry<N> childEntry, Object childConf) {
		continueNested(key, childEntry, childEntry.getDefinition().getConfigClass().cast(childConf));
	}
	
	private Object deprocessObjectAtEntry(SingleConfEntry entry, Object value) {
		return deprocessObjectAtEntryWithGoal(entry, entry.getMethod().getReturnType(), value);
	}
	
	private <G> Object deprocessObjectAtEntryWithGoal(SingleConfEntry entry, Class<G> goal, Object value) {

		if (goal == List.class || goal == Set.class || goal == Collection.class) {
			List<Object> serialised = new ArrayList<>();
			for (Object element : (Collection<?>) value) {
				serialised.add(deprocessObjectAtEntryWithGoal(entry, entry.getCollectionElementType(), element));
			}
			return ImmutableCollections.listOf(serialised);
		}

		@SuppressWarnings("unchecked")
		G castedValue = (G) value; // a class.cast call breaks primitives
		return new DecomposerImpl(key, definition.getSerialisers()).decompose(goal, castedValue);
	}
	
}
