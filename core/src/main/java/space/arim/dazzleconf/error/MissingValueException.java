/*
 * DazzleConf
 * Copyright © 2021 Anand Beh
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

package space.arim.dazzleconf.error;

/**
 * Indicates a value is nonexistent (e.g. null) at a specific key
 * 
 * @author A248
 *
 */
public class MissingValueException extends ImproperEntryException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = 832461791665011503L;

	private MissingValueException(String key) {
		super(key);
	}
	
	private MissingValueException(String key, String message) {
		super(key, message);
	}
	
	/**
	 * Creates the exception using the given key
	 * 
	 * @param key the key
	 * @return the exception
	 * @throws NullPointerException if {@code key} is null
	 */
	public static MissingValueException forKey(String key) {
		return new MissingValueException(key);
	}
	
	/**
	 * Creates the exception with the given key and message
	 * 
	 * @param key the key
	 * @param message the message
	 * @return the exception
	 * @throws NullPointerException if {@code key} or {@code message} is null
	 */
	public static MissingValueException forKeyAndMessage(String key, String message) {
		return new MissingValueException(key, message);
	}

	/**
	 * Creates the exception with the given key and message
	 *
	 * @param key the key
	 * @param message the message
	 * @return the exception
	 * @throws NullPointerException if {@code key} or {@code message} is null
	 */
	public static MissingValueException forKeyAndMessage(String key, CharSequence message) {
		return forKeyAndMessage(key, message.toString());
	}

}
