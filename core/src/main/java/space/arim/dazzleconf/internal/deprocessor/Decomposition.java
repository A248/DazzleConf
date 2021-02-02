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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.type.CollectionReturnType;
import space.arim.dazzleconf.internal.type.MapReturnType;
import space.arim.dazzleconf.internal.type.ReturnType;
import space.arim.dazzleconf.internal.type.SimpleCollectionReturnType;
import space.arim.dazzleconf.internal.type.SimpleMapReturnType;
import space.arim.dazzleconf.internal.type.SimpleSubSectionReturnType;
import space.arim.dazzleconf.internal.type.SubSectionCollectionReturnType;
import space.arim.dazzleconf.internal.type.SubSectionMapReturnType;
import space.arim.dazzleconf.serialiser.Decomposer;

class Decomposition {

	private final DeprocessorBase<?> deprocessor;
	private final ConfEntry entry;
	private final Object value;
	private final Decomposer decomposer;
	
	Decomposition(DeprocessorBase<?> deprocessor, ConfEntry entry, Object value, Decomposer decomposer) {
		this.deprocessor = deprocessor;
		this.entry = entry;
		this.value = value;
		this.decomposer = decomposer;
	}
	
	Object deprocessObject() {
		return deprocessObjectWithReturnType(entry.returnType());
	}
	
	private <G> Object deprocessObjectWithReturnType(ReturnType<G> returnType) {
		if (returnType instanceof SimpleSubSectionReturnType) {
			SimpleSubSectionReturnType<G> subSectionReturnType = ((SimpleSubSectionReturnType<G>) returnType);
			ConfigurationDefinition<G> configDefinition = subSectionReturnType.configDefinition();
			return deprocessor.deprocessNested(configDefinition, configDefinition.getConfigClass().cast(value));
		}
		if (returnType instanceof CollectionReturnType) {
			return decomposeCollection((CollectionReturnType<?, ?>) returnType);
		}
		if (returnType instanceof MapReturnType) {
			return decomposeMap((MapReturnType<?, ?>) returnType);
		}
		Class<G> goal = returnType.typeInfo().rawType();
		@SuppressWarnings("unchecked")
		G castedValue = (G) value; // a class.cast call breaks primitives
		return decomposer.decompose(goal, castedValue);
	}

	private <E, R extends Collection<E>> Collection<Object> decomposeCollection(CollectionReturnType<E, R> returnType) {
		@SuppressWarnings("unchecked")
		Collection<E> collection = (Collection<E>) value;
		if (returnType instanceof SimpleCollectionReturnType) {
			return decomposer.decomposeCollection(returnType.elementTypeInfo().rawType(), collection);
		}
		SubSectionCollectionReturnType<E, R> subSectionReturnType = (SubSectionCollectionReturnType<E, R>) returnType;
		ConfigurationDefinition<E> configDefinition = subSectionReturnType.configDefinition();
		List<Object> serialised = new ArrayList<>(collection.size());
		for (E element : collection) {
			serialised.add(deprocessor.deprocessNested(configDefinition, element));
		}
		return serialised;
	}

	private <K, V> Map<Object, Object> decomposeMap(MapReturnType<K, V> returnType) {
		Class<K> keyType = returnType.keyTypeInfo().rawType();
		Class<V> valueType = returnType.valueTypeInfo().rawType();
		@SuppressWarnings("unchecked")
		Map<K, V> map = (Map<K, V>) value;
		if (returnType instanceof SimpleMapReturnType) {
			return decomposer.decomposeMap(keyType, valueType, map);
		}
		SubSectionMapReturnType<K, V> subSectionReturnType = (SubSectionMapReturnType<K, V>) returnType;
		ConfigurationDefinition<V> configDefinition = subSectionReturnType.configDefinition();
		Map<Object, Object> serialised = new LinkedHashMap<>((int) (map.size() / 0.74f));
		for (Map.Entry<K, V> entry : map.entrySet()) {
			serialised.put(
					decomposer.decompose(keyType, entry.getKey()),
					deprocessor.deprocessNested(configDefinition, entry.getValue()));
		}
		return serialised;
	}
	
}
