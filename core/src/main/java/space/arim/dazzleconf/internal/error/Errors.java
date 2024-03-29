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

public final class Errors {

	private Errors() {}

	public enum When {
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

	public interface StandardError extends CharSequence {

		When when();

		String message();

		default String withExtraInfo(String extraInfo) {
			String extra = (extraInfo.isEmpty()) ? "None" : extraInfo;
			return "Encountered an error while " + when() + ". \n" +
					"Reason: " + message() + "\n" +
					"Extra info: " + extra;
		}

	}

}
