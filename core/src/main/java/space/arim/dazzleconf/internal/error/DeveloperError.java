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
import space.arim.dazzleconf.serialiser.ValueSerialiser;

import java.util.stream.IntStream;

public final class DeveloperError implements Errors.StandardError {

	private final Errors.When when;
	private final String message;

	private DeveloperError(Errors.When when, String message) {
		this.when = when;
		this.message = message;
	}

	public static DeveloperError expectedMap(Errors.When when, String fullKey, Object actualValue) {
		return new DeveloperError(Errors.When.LOAD_CONFIG,
				"Expected a configuration section inside the data, but a simple object was present. " +
						"Key: " + fullKey + ". Actual value: " + actualValue);
	}

	public static DeveloperError replacedObject(String key, Object replaced, Object replacement) {
		return new DeveloperError(Errors.When.WRITE_CONFIG, "Unexpectedly replaced object " + replaced +
				" at key " + key + " with " + replacement + ". No objects should be replaced during " +
				"writing of the configuration, so this indicates an issue.");
	}

	public static DeveloperError noSerializerFound(Errors.When when, String key, Class<?> type) {
		return new DeveloperError(when,
				"No ValueSerialiser was found for " + type + " at entry " + key + ". " +
						"To use custom types, a relevant ValueSerialiser must exist.");
	}

	public static DeveloperError serializerReturnedNull(Errors.When when, String key, ValueSerialiser<?> serialiser) {
		return new DeveloperError(when,
				"At key " + key + ", the ValueSerialiser (" + serialiser + ") returned null. " +
						"This is a breach of contract; ValueSerialisers should never return null");
	}

	@Override
	public Errors.When when() {
		return when;
	}

	@Override
	public String message() {
		return message + "\n(This is an error on behalf of the developer)";
	}

	public IllDefinedConfigException toConfigException() {
		return new IllDefinedConfigException(toString());
	}

	// CharSequence implementation

	@Override
	public String toString() {
		return withExtraInfo("");
	}

	@Override
	public int length() {
		return toString().length();
	}

	@Override
	public char charAt(int index) {
		return toString().charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return toString().subSequence(start, end);
	}

	@Override
	public IntStream chars() {
		return toString().chars();
	}

	@Override
	public IntStream codePoints() {
		return toString().codePoints();
	}
}
