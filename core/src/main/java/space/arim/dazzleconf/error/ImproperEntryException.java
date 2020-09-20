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

import java.util.Objects;

/**
 * General exception indicating a problem with a specific configuration entry
 * 
 * @author A248
 *
 */
public class ImproperEntryException extends InvalidConfigException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = 5119522236600323970L;

	private final String key;
	
	/**
	 * Creates the exception using the given key
	 * 
	 * @param key the key
	 */
	ImproperEntryException(String key) {
		super("For key " + Objects.requireNonNull(key, "key"));
		this.key = key;
	}
	
	/**
	 * Creates the exception with the given key and message
	 * 
	 * @param key the key
	 * @param message the message
	 */
	ImproperEntryException(String key, String message) {
		super("For key " + Objects.requireNonNull(key, "key") + ", " + Objects.requireNonNull(message, "message"));
		this.key = key;
	}
	
	/**
	 * Creates the exception with the given key, message, and cause
	 * 
	 * @param key the key
	 * @param message the message
	 * @param cause the cause
	 */
	ImproperEntryException(String key, String message, Throwable cause) {
		super("For key " + Objects.requireNonNull(key, "key") + ", " + Objects.requireNonNull(message, "message"), cause);
		this.key = key;
	}
	
	/**
	 * Creates the exception with the given key and cause
	 * 
	 * @param key the key
	 * @param cause the cause
	 */
	ImproperEntryException(String key, Throwable cause) {
		super("For key " + Objects.requireNonNull(key, "key"), cause);
		this.key = key;
	}
	
	/**
	 * Gets the key which this exception applies to
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	
}
