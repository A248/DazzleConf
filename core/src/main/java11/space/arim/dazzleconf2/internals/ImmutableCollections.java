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
package space.arim.dazzleconf2.internals;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ImmutableCollections {

	private ImmutableCollections() {}
	
	public static <E> List<E> emptyList() {
		return List.of();
	}
	
	public static <E> List<E> listOf(E element) {
		return List.of(element);
	}

	@SafeVarargs
	public static <E> List<E> listOf(E...elements) {
		return List.of(elements);
	}

	public static <E> List<E> listOf(Collection<? extends E> coll) {
		return List.copyOf(coll);
	}
	
	public static <E> Set<E> emptySet() {
		return Set.of();
	}
	
	public static <E> Set<E> setOf(E element) {
		return Set.of(element);
	}
	
	public static <E> Set<E> setOf(Collection<? extends E> coll) {
		return Set.copyOf(coll);
	}
	
	public static <K, V> Map<K, V> emptyMap() {
		return Map.of();
	}
	
	public static <K, V> Map<K, V> mapOf(Map<? extends K, ? extends V> map) {
		return Map.copyOf(map);
	}
	
	public static <K, V> Map.Entry<K, V> mapEntryOf(K key, V value) {
		return Map.entry(key, value);
	}
	
}
