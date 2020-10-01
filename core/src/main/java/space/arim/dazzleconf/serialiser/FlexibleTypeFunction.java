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

import space.arim.dazzleconf.error.BadValueException;

/**
 * Functional interface which computes a result from a {@link FlexibleType}
 * 
 * @author A248
 *
 * @param <T> the type of the result
 */
@FunctionalInterface
public interface FlexibleTypeFunction<T> {

	/**
	 * Gets a result from the flexible type
	 * 
	 * @param flexibleType the flexible type
	 * @return the result
	 * @throws BadValueException if thrown from {@link FlexibleType}'s methods
	 */
	T getResult(FlexibleType flexibleType) throws BadValueException;
	
}
