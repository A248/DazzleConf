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

/**
 * A complement to {@link FlexibleType} which allows objects to be broken down during reserialisation
 * 
 * @author A248
 *
 */
public interface Decomposer {

	/**
	 * Decomposes an object of a certain type, using a value serialiser if necessary
	 * 
	 * @param <T> the type according to which to serialise
	 * @param clazz the type of the object according to which to serialise
	 * @param value the object to serialise
	 * @return the serialised form of the object
	 */
	<T> Object decompose(Class<T> clazz, T value);
	
}
