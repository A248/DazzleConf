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

import java.util.Map;

import space.arim.dazzleconf.internal.ImmutableCollections;

/**
 * An immutable map of value serialisers, keyed by the type which they serialise.
 * 
 * @author A248
 *
 */
public final class ValueSerialiserMap {

	private final Map<Class<?>, ValueSerialiser<?>> map;
	
	/**
	 * Creates from a map. The map's contents are copied
	 * 
	 * @param map the map
	 * @throws NullPointerException if the map or any of its keys or values i null
	 */
	public ValueSerialiserMap(Map<Class<?>, ValueSerialiser<?>> map) {
		this.map = ImmutableCollections.mapOf(map);
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
	 * Gets this value serialiser map as a {@link Map}. This map is not modifiable.
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
