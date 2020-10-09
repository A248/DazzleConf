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
import java.util.Map;

/**
 * A complement to {@link FlexibleType} which allows objects to be broken down during reserialisation. <br>
 * <br>
 * Note that, unlike a {@code FlexibleType}, a decomposer does not store any value paired with it.
 * 
 * @author A248
 *
 */
public interface Decomposer {

	/**
	 * Decomposes all the elements of a collection. Equivalent to calling {@link #decompose(Class, Object)}
	 * to process each element. <br>
	 * <br>
	 * The returned collection may or may not be mutable. The caller should create a copy if modification is desired.
	 * 
	 * @param <E> the element type
	 * @param elementType the element type class
	 * @param collection the collection to decompose
	 * @return the serialised form of the object
	 */
	<E> Collection<Object> decomposeCollection(Class<E> elementType, Collection<? extends E> collection);
	
	/**
	 * Decomposes all the keys and values of a map. Equivalent to calling {@link #decompose(Class, Object)}
	 * to process each key and value. <br>
	 * <br>
	 * The returned map may or may not be mutable. The caller should create a copy if modification is desired.
	 * 
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param keyType the key type class
	 * @param valueType the value type class
	 * @param map the map to decompose
	 * @return the serialised form of the object
	 */
	<K, V> Map<Object, Object> decomposeMap(Class<K> keyType, Class<V> valueType, Map<? extends K, ? extends V> map);
	
	/**
	 * Decomposes an object of a certain type, using a value serialiser if necessary
	 * 
	 * @param <T> the type according to which to serialise
	 * @param type the type of the object according to which to serialise
	 * @param value the object to serialise
	 * @return the serialised form of the object
	 */
	<T> Object decompose(Class<T> type, T value);
	
}
