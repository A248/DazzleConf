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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import space.arim.dazzleconf.internal.SingleConfEntry;
import space.arim.dazzleconf.serialiser.Decomposer;

class Decomposition {

	private final SingleConfEntry entry;
	private final Object value;
	private final Decomposer decomposer;
	
	Decomposition(SingleConfEntry entry, Object value, Decomposer decomposer) {
		this.entry = entry;
		this.value = value;
		this.decomposer = decomposer;
	}
	
	Object deprocessObject() {
		return deprocessObjectWithGoal(entry.getMethod().getReturnType());
	}
	
	private <G> Object deprocessObjectWithGoal(Class<G> goal) {
		if (goal == List.class || goal == Set.class || goal == Collection.class) {
			Class<?> elementType = entry.getCollectionElementType();
			return decomposeCollectionCast(elementType);
		}
		if (goal == Map.class) {
			Class<?> keyType = entry.getMapKeyType();
			Class<?> valueType = entry.getMapValueType();
			return decomposeMapCast(keyType, valueType);
		}

		@SuppressWarnings("unchecked")
		G castedValue = (G) value; // a class.cast call breaks primitives
		return decomposer.decompose(goal, castedValue);
	}
	
	@SuppressWarnings("unchecked")
	private <E> Collection<Object> decomposeCollectionCast(Class<E> elementType) {
		return decomposer.decomposeCollection(elementType, (Collection<E>) value);
	}
	
	@SuppressWarnings("unchecked")
	private <K, V> Map<Object, Object> decomposeMapCast(Class<K> keyType, Class<V> valueType) {
		return decomposer.decomposeMap(keyType, valueType, (Map<K, V>) value);
	}
	
}
