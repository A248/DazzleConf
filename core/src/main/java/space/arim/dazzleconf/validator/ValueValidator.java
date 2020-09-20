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
package space.arim.dazzleconf.validator;

import space.arim.dazzleconf.error.BadValueException;

/**
 * Functional interface for validating configuration entries.
 * 
 * @author A248
 * 
 */
@FunctionalInterface
public interface ValueValidator {

	/**
	 * Validates a value. The value provided is the one after any type conversions. It may be casted
	 * to the config method's return type.
	 * 
	 * @param key the key, used to construct {@link BadValueException}
	 * @param value the value
	 * @throws BadValueException if the value is invalid
	 */
	void validate(String key, Object value) throws BadValueException;
	
}
