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
package space.arim.dazzleconf.serialiser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import space.arim.dazzleconf.internal.util.ImmutableCollections;

/**
 * An immutable map of value serialisers, keyed by the type which they serialise.
 * 
 * @author A248
 *
 */
public final class ValueSerialiserMap {

	private final Map<Class<?>, ValueSerialiser<?>> map;

	private static final ValueSerialiserMap EMPTY = new ValueSerialiserMap();
	
	private ValueSerialiserMap() {
		this.map = ImmutableCollections.emptyMap();
	}
	
	private ValueSerialiserMap(Map<Class<?>, ValueSerialiser<?>> map) {
		this.map = ImmutableCollections.mapOf(map);
	}
	
	private static ValueSerialiserMap fromMap(Map<Class<?>, ValueSerialiser<?>> map) {
		if (map.isEmpty()) {
			return EMPTY;
		}
		return new ValueSerialiserMap(map);
	}
	
	/**
	 * Creates from a collection of serialisers. If any two serialisers specify the same target type
	 * ({@link ValueSerialiser#getTargetClass()}), {@code IllegalArgumentException} is thrown
	 * 
	 * @param serialisers the value serialisers
	 * @return the value serialiser map
	 * @throws NullPointerException if any serialiser is null
	 * @throws IllegalArgumentException if any value serialisers conflict
	 */
	public static ValueSerialiserMap of(Collection<? extends ValueSerialiser<?>> serialisers) {
		Map<Class<?>, ValueSerialiser<?>> map = new HashMap<>();
		for (ValueSerialiser<?> serialiser : serialisers) {
			Objects.requireNonNull(serialiser, "serialiser");
			ValueSerialiser<?> previous = map.putIfAbsent(serialiser.getTargetClass(), serialiser);
			if (previous != null) {
				throw new IllegalArgumentException("ValueSerialiser " + serialiser + " conflicts with " + previous);
			}
		}
		return fromMap(map);
	}
	
	/**
	 * Creates from a map of serialisers. If any serialisers are at a mismatched key,
	 * {@code IllegalArgumentException} is thrown
	 * 
	 * @param serialisers the value serialisers
	 * @return the value serialiser map
	 * @throws NullPointerException if any serialiser is null
	 * @throws IllegalArgumentException if any value serialisers are at a mismatched key
	 */
	public static ValueSerialiserMap of(Map<Class<?>, ? extends ValueSerialiser<?>> serialisers) {
		Map<Class<?>, ValueSerialiser<?>> map = new HashMap<>(serialisers);
		for (Map.Entry<Class<?>, ValueSerialiser<?>> entry : map.entrySet()) {
			Class<?> actualKey = entry.getKey();
			ValueSerialiser<?> serialiser = entry.getValue();
			Objects.requireNonNull(serialiser, "serialiser");
			Class<?> expectedKey = serialiser.getTargetClass();
			if (actualKey != expectedKey) {
				throw new IllegalArgumentException("ValueSerialiser " + serialiser
						+ " is at a mismatched key, expected " + expectedKey + " but got " + actualKey);
			}
		}
		return fromMap(map);
	}
	
	/**
	 * Gets an empty value serialiser map
	 * 
	 * @return an empty map
	 */
	public static ValueSerialiserMap empty() {
		return EMPTY;
	}
	
	/**
	 * Gets the value serialiser for a given type
	 * 
	 * @param <T> the type
	 * @param type the type class
	 * @return the value serialiser for the type or {@code null} if there is none
	 */
	public <T> ValueSerialiser<T> getSerialiser(Class<T> type) {
		@SuppressWarnings("unchecked")
		ValueSerialiser<T> serialiser = (ValueSerialiser<T>) map.get(type);
		return serialiser;
	}
	
	/**
	 * Gets this value serialiser map as a {@link Map}. This map is immutable.
	 * 
	 * @return the map of classes to value serialisers, never {@code null}
	 */
	public Map<Class<?>, ValueSerialiser<?>> asMap() {
		return map;
	}

	@Override
	public String toString() {
		return "ValueSerialiserMap [map=" + map + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + map.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof ValueSerialiserMap)) {
			return false;
		}
		ValueSerialiserMap other = (ValueSerialiserMap) object;
		return map.equals(other.map);
	}
	
}
