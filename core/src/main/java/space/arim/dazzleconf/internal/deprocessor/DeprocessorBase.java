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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
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

	private final ConfigurationDefinition<C> definition;
	private final C configData;
	
	/** InvocationHandler, nonnull if configData is a proxy */
	private transient final InvocationHandler configDataProxyHandler;
	
	private transient final DecomposerImpl decomposer;
	
	DeprocessorBase(ConfigurationDefinition<C> definition, C configData) {
		this.definition = definition;
		this.configData = configData;
		decomposer = new DecomposerImpl(definition.getSerialisers());
		if (Proxy.isProxyClass(configData.getClass())) {
			configDataProxyHandler = Proxy.getInvocationHandler(configData);
		} else {
			configDataProxyHandler = null;
		}
	}
	
	abstract void finishSimple(String key, SingleConfEntry entry, Object value);
	
	abstract <N> void continueNested(String key, NestedConfEntry<N> childEntry, N childConf);
	
	void deprocess() {
		for (ConfEntry entry : definition.getEntries()) {
			String key = entry.getKey();
			decomposer.setKey(key);
			deprocessEntry(key, entry);
		}
	}
	
	private Object getValueAt(ConfEntry entry) {
		Method method = entry.getMethod();
		try {
			if (configDataProxyHandler != null) {
				return configDataProxyHandler.invoke(configData, method, null);
			} else {
				return method.invoke(configData);
			}

		} catch (RuntimeException | Error ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new RuntimeException("Exception while invoking implementation of " + entry.getQualifiedMethodName()
			+ " in " + configData.getClass().getName(), ex);
		}
	}
	
	private void deprocessEntry(String key, ConfEntry entry) {
		Object value = getValueAt(entry);
		if (entry instanceof NestedConfEntry) {
			preContinueNested(key, (NestedConfEntry<?>) entry, value);
		} else {
			SingleConfEntry singleEntry = (SingleConfEntry) entry;
			Object postValue = deprocessObjectAtEntry(singleEntry, value);
			finishSimple(key, singleEntry, postValue);
		}
	}
	
	private <N> void preContinueNested(String key, NestedConfEntry<N> childEntry, Object childConf) {
		continueNested(key, childEntry, childEntry.getDefinition().getConfigClass().cast(childConf));
	}
	
	private Object deprocessObjectAtEntry(SingleConfEntry entry, Object value) {
		return deprocessObjectAtEntryWithGoal(entry, entry.getMethod().getReturnType(), value);
	}
	
	private <G> Object deprocessObjectAtEntryWithGoal(SingleConfEntry entry, Class<G> goal, Object value) {
		if (goal == List.class || goal == Set.class || goal == Collection.class) {
			Class<?> elementType = entry.getCollectionElementType();
			return decomposeCollection(elementType, value);
		}
		if (goal == Map.class) {
			Class<?> keyType = entry.getMapKeyType();
			Class<?> valueType = entry.getMapValueType();
			return decomposeMap(keyType, valueType, value);
		}

		@SuppressWarnings("unchecked")
		G castedValue = (G) value; // a class.cast call breaks primitives
		return decomposer.decompose(goal, castedValue);
	}
	
	@SuppressWarnings("unchecked")
	private <E> Collection<Object> decomposeCollection(Class<E> elementType, Object value) {
		return decomposer.decomposeCollection(elementType, (Collection<E>) value);
	}
	
	@SuppressWarnings("unchecked")
	private <K, V> Map<Object, Object> decomposeMap(Class<K> keyType, Class<V> valueType, Object value) {
		return decomposer.decomposeMap(keyType, valueType, (Map<K, V>) value);
	}
	
}
