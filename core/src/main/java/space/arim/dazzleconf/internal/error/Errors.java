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

package space.arim.dazzleconf.internal.error;

import java.util.Locale;

public final class Errors {

	private Errors() {}

	enum When {
		LOAD_CONFIG("loading the configuration"),
		WRITE_CONFIG("writing or creating the configuration");

		private final String display;

		private When(String display) {
			this.display = display;
		}

		@Override
		public String toString() {
			return display;
		}

	}

	static CharSequence pad(char prefix, int digits, int errorCode) {
		String codeString = Integer.toHexString(errorCode).toUpperCase(Locale.ROOT);
		int padAmount = digits - codeString.length();
		StringBuilder builder = new StringBuilder();
		builder.append(prefix);
		for (int n = 0; n < padAmount; n++) {
			builder.append('0');
		}
		builder.append(codeString);
		return builder;
	}

	public interface StandardError {

		When when();

		CharSequence errorCodeDisplay();

		String message();

		default String fullMessage(String extraInfo) {
			String extra = (extraInfo.isEmpty()) ? "None" : extraInfo;
			return "Encountered an error while " + when() + ". \n" +
					"Error code: " + errorCodeDisplay() + ". " + message() + "\n" +
					"\n" +
					"Consult the error codes: https://github.com/A248/DazzleConf/wiki/Error-Messages \n" +
					"Extra info: " + extra;
		}

	}

}
