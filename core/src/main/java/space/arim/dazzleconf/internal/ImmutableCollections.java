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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ImmutableCollections {
	
	private static final boolean PRE_JAVA_10;
	
	static {
		boolean preJava10 = false;
		try {
			List.copyOf(List.of());
		} catch (NoSuchMethodError nsme) {
			preJava10 = true;
		}
		PRE_JAVA_10 = preJava10;
	}

	private ImmutableCollections() {}
	
	public static <E> List<E> emptyList() {
		if (PRE_JAVA_10) {
			return Collections.emptyList();
		}
		return List.of();
	}
	
	public static <E> List<E> listOf(E element) {
		if (PRE_JAVA_10) {
			Objects.requireNonNull(element, "element");
			return Collections.singletonList(element);
		}
		return List.of(element);
	}
	
	@SafeVarargs
	public static <E> List<E> listOf(E...elements) {
		if (PRE_JAVA_10) {
			E[] clone = elements.clone();
			if (clone.length == 0) {
				return emptyList();
			}
			for (E element : clone) {
				Objects.requireNonNull(element, "element");
			}
			return Collections.unmodifiableList(Arrays.asList(clone));
		}
		return List.of(elements);
	}
	
	@SuppressWarnings("unchecked")
	public static <E> List<E> listOf(Collection<? extends E> coll) {
		if (PRE_JAVA_10) {
			return (List<E>) listOf(coll.toArray());
		}
		return List.copyOf(coll);
	}
	
	public static <E> Set<E> emptySet() {
		if (PRE_JAVA_10) {
			return Collections.emptySet();
		}
		return Set.of();
	}
	
	public static <E> Set<E> setOf(Collection<? extends E> coll) {
		if (PRE_JAVA_10) {
			Set<E> hashSet = new HashSet<>(coll);
			if (hashSet.isEmpty()) {
				return emptySet();
			}
			for (E element : hashSet) {
				Objects.requireNonNull(element, "element");
			}
			return Collections.unmodifiableSet(hashSet);
		}
		return Set.copyOf(coll);
	}
	
	public static <K, V> Map<K, V> emptyMap() {
		if (PRE_JAVA_10) {
			return Collections.emptyMap();
		}
		return Map.of();
	}
	
	public static <K, V> Map<K, V> mapOf(Map<? extends K, ? extends V> map) {
		if (PRE_JAVA_10) {
			Map<K, V> hashMap = new HashMap<>(map);
			if (hashMap.isEmpty()) {
				return emptyMap();
			}
			for (Map.Entry<K, V> entry : hashMap.entrySet()) {
				Objects.requireNonNull(entry.getKey(), "key");
				Objects.requireNonNull(entry.getValue(), "value");
			}
			return Collections.unmodifiableMap(hashMap);
		}
		return Map.copyOf(map);
	}
	
}
