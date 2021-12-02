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

import java.util.Objects;
import java.util.stream.IntStream;

public final class UserError implements Errors.StandardError {

	private final Errors.When when;
	private final String message;

	private UserError(Errors.When when, String message) {
		this.when = Objects.requireNonNull(when, "when");
		this.message = Objects.requireNonNull(message, "message");
	}

	public static UserError wrongType(UserType expected, Object actual) {
		StringBuilder message = new StringBuilder();
		message.append("The wrong type was entered. The value should be ");
		message.append(expected.toString());
		message.append(", ");
		message.append("but it was really ");
		message.append(actual.toString());
		String[] validExamples = expected.examples();
		if (validExamples.length != 0) {
			message.append('\n');
			message.append("Some examples of valid input: ");
			for (int n = 0; n < validExamples.length; n++) {
				if (n != 0) {
					message.append(", ");
					message.append(validExamples[n]);
				}
			}
		}
		return new UserError(Errors.When.LOAD_CONFIG, message.toString());
	}

	public static UserError sizeTooSmall(Number actual, Number minimum) {
		assert actual.doubleValue() < minimum.doubleValue() : "Internal error";
		return new UserError(Errors.When.LOAD_CONFIG,
				"The value's size of " + actual + " must be more than the minimum size of " + minimum);
	}

	public static UserError sizeTooBig(Number actual, Number maximum) {
		assert actual.doubleValue() > maximum.doubleValue() : "Internal error";
		return new UserError(Errors.When.LOAD_CONFIG,
				"The value's size of " + actual + " must be less than the maximum size of " + maximum);
	}

	@Override
	public Errors.When when() {
		return when;
	}

	@Override
	public String message() {
		return message;
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
