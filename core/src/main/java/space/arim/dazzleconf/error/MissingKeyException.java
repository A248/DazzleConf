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
package space.arim.dazzleconf.error;

/**
 * Indicates a specific key does not exist
 * 
 * @author A248
 *
 */
public class MissingKeyException extends ImproperEntryException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = 29487887930314372L;

	private MissingKeyException(String key) {
		super(key);
	}
	
	private MissingKeyException(String key, String message) {
		super(key, message);
	}
	
	/**
	 * Creates the exception using the given key
	 * 
	 * @param key the key
	 * @return the exception
	 * @throws NullPointerException if {@code key} is null
	 */
	public static MissingKeyException forKey(String key) {
		return new MissingKeyException(key);
	}
	
	/**
	 * Creates the exception with the given key and message
	 * 
	 * @param key the key
	 * @param message the message
	 * @return the exception
	 * @throws NullPointerException if {@code key} or {@code message} is null
	 */
	public static MissingKeyException forKeyAndMessage(String key, String message) {
		return new MissingKeyException(key, message);
	}

	/**
	 * Creates the exception with the given key and message
	 *
	 * @param key the key
	 * @param message the message
	 * @return the exception
	 * @throws NullPointerException if {@code key} or {@code message} is null
	 */
	public static MissingKeyException forKeyAndMessage(String key, CharSequence message) {
		return forKeyAndMessage(key, message.toString());
	}

}
