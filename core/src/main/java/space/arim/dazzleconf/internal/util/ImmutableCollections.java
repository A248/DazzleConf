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
package space.arim.dazzleconf.internal.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class is present in the multirelease directory, so changes to method signatures
 * should be done exactly likewise there. <br>
 * <br>
 * Java 8 users are the reason this class exists. Please update to JDK 11 or later.
 */
public final class ImmutableCollections {

	private ImmutableCollections() {}
	
	public static <E> List<E> emptyList() {
		return Collections.emptyList();
	}
	
	public static <E> List<E> listOf(E element) {
		Objects.requireNonNull(element, "element");
		return Collections.singletonList(element);
	}
	
	@SafeVarargs
	public static <E> List<E> listOf(E...elements) {
		elements = elements.clone();
		for (E element : elements) {
			Objects.requireNonNull(element, "element");
		}
		return Collections.unmodifiableList(Arrays.asList(elements));
	}
	
	@SuppressWarnings("unchecked")
	public static <E> List<E> listOf(Collection<? extends E> coll) {
		return (List<E>) listOf(coll.toArray());
	}
	
	public static <E> Set<E> emptySet() {
		return Collections.emptySet();
	}
	
	public static <E> Set<E> setOf(E element) {
		Objects.requireNonNull(element, "element");
		return Collections.singleton(element);
	}
	
	public static <E> Set<E> setOf(Collection<? extends E> coll) {
		Set<E> copy = new HashSet<>(coll);
		for (E element : copy) {
			Objects.requireNonNull(element, "element");
		}
		return Collections.unmodifiableSet(copy);
	}
	
	public static <K, V> Map<K, V> emptyMap() {
		return Collections.emptyMap();
	}
	
	public static <K, V> Map<K, V> mapOf(Map<? extends K, ? extends V> map) {
		Map<K, V> copy = new HashMap<>(map);
		for (Map.Entry<K, V> entry : copy.entrySet()) {
			Objects.requireNonNull(entry.getKey(), "key");
			Objects.requireNonNull(entry.getValue(), "value");
		}
		return Collections.unmodifiableMap(copy);
	}
	
	public static <K, V> Map.Entry<K, V> mapEntryOf(K key, V value) {
		Objects.requireNonNull(key, "key");
		Objects.requireNonNull(value, "value");
		return new java.util.AbstractMap.SimpleImmutableEntry<>(key, value);
	}
	
}
