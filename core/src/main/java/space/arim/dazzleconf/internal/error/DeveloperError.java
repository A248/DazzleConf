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

import space.arim.dazzleconf.error.IllDefinedConfigException;

public enum DeveloperError implements Errors.StandardError {

	EXPECTED_MAP_WHILE_WRITE(0x1A, Errors.When.WRITE_CONFIG,
			"Expected a configuration section inside the data, but a simple object was present."),
	REPLACED_OBJECT(0x1B, Errors.When.WRITE_CONFIG,
			"Replaced a configuration object with another one."),
	EXPECTED_MAP_WHILE_LOAD(0x1C, Errors.When.LOAD_CONFIG,
			"Expected a configuration section inside the data, but a simple object was present.");

	private final int errorCode;
	private final Errors.When when;
	private final String message;

	DeveloperError(int errorCode, Errors.When when, String message) {
		this.errorCode = errorCode;
		this.when = when;
		this.message = message;
	}

	@Override
	public Errors.When when() {
		return when;
	}

	@Override
	public CharSequence errorCodeDisplay() {
		return Errors.pad('D', 2, errorCode);
	}

	@Override
	public String message() {
		return message;
	}

	public IllDefinedConfigException toIllDefinedConfigException(String extraInfo) {
		return new IllDefinedConfigException(fullMessage(extraInfo));
	}

}
