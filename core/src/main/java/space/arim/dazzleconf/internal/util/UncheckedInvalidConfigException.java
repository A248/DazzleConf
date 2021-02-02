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

package space.arim.dazzleconf.internal.util;

import space.arim.dazzleconf.error.InvalidConfigException;

public class UncheckedInvalidConfigException extends RuntimeException {

	public UncheckedInvalidConfigException(InvalidConfigException cause) {
		super("This exception should not be thrown from a public API method. " +
				"If you see this, please report the bug", cause);
	}

	@Override
	public synchronized InvalidConfigException getCause() {
		return (InvalidConfigException) super.getCause();
	}

}
