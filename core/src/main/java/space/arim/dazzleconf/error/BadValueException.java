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
 * Indicates a value exists, but it itself violates some requirement
 * 
 * @author A248
 *
 */
public final class BadValueException extends ImproperEntryException {

	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = 8515180182868980217L;

	private BadValueException(String key) {
		super(key);
	}
	
	private BadValueException(String key, String message) {
		super(key, message);
	}
	
	private BadValueException(String key, String message, Throwable cause) {
		super(key, message, cause);
	}
	
	private BadValueException(String key, Throwable cause) {
		super(key, cause);
	}
	
	/**
	 * Builder of {@code BadValueException}. It is highly recommended to set {@link #key(String)} to inform users
	 * which key is affected.
	 * 
	 * @author A248
	 *
	 */
	public static class Builder {
		
		private String key;
		private String message;
		private Throwable cause;
		
		public Builder key(String key) {
			this.key = key;
			return this;
		}
		
		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder message(CharSequence message) {
			return message(message.toString());
		}

		public Builder cause(Throwable cause) {
			this.cause = cause;
			return this;
		}
		
		public BadValueException build() {
			String key = this.key;
			if (key == null) {
				key = "unknown";
			}
			if (message == null) {
				if (cause == null) {
					return new BadValueException(key);
				} else {
					return new BadValueException(key, cause);
				}
			} else {
				if (cause == null) {
					return new BadValueException(key, message);
				} else {
					return new BadValueException(key, message, cause);
				}
			}
		}
		
	}
	
}
