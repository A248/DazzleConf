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
 * Indicates the syntax of a specific config format is invalid
 * 
 * @author A248
 *
 */
public class ConfigFormatSyntaxException extends InvalidConfigException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = 7428120426189473800L;

	/**
	 * Creates the exception
	 * 
	 */
	public ConfigFormatSyntaxException() {
		
	}
	
	/**
	 * Creates the exception with an exception message
	 * 
	 * @param message the message
	 */
	public ConfigFormatSyntaxException(String message) {
		super(message);
	}
	
	/**
	 * Creates the exception with a cause
	 * 
	 * @param cause the cause
	 */
	public ConfigFormatSyntaxException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Creates the exception with an exception message and cause
	 * 
	 * @param message the message
	 * @param cause the cause
	 */
	public ConfigFormatSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
