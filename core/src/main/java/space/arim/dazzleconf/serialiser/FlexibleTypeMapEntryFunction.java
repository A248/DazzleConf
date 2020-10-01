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

import java.util.Map.Entry;

import space.arim.dazzleconf.error.BadValueException;

/**
 * Functional interface which computes a <pre>{@code Map.Entry<K, V>>}</pre> from a {@link FlexibleType}
 * key and value
 * 
 * @author A248
 *
 * @param <K> the type of the key result
 * @param <V> the type of the value result
 */
@FunctionalInterface
public interface FlexibleTypeMapEntryFunction<K, V> {

	/**
	 * Gets a result from two flexible types
	 * 
	 * @param flexibleKey the flexible type key
	 * @param flexibleValue the flexible type value
	 * @return the map entry
	 * @throws BadValueException if thrown from {@link FlexibleType}'s methods
	 */
	Entry<K, V> getResult(FlexibleType flexibleKey, FlexibleType flexibleValue) throws BadValueException;
	
}
