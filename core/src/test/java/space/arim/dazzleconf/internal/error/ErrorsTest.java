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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ErrorsTest {

	@ParameterizedTest
	@EnumSource(Errors.When.class)
	public void helpfulWhenToString(Errors.When when) {
		String toString = when.toString();
		assertNotEquals(toString, when.name());
		assertFalse(toString.isBlank());
	}

	@ParameterizedTest
	@ArgumentsSource(StandardErrorArgumentsProvider.class)
	public void fullMessage(Errors.StandardError error) {
		assertDoesNotThrow(error::toString);
	}

	@ParameterizedTest
	@ArgumentsSource(StandardErrorArgumentsProvider.class)
	public void fullMessageIncludesInfo(Errors.StandardError error) {
		String extraInfo = "something you will never see in an actual error message";
		String message = error.withExtraInfo(extraInfo);
		assertTrue(message.contains(extraInfo));
	}

	@ParameterizedTest
	@ArgumentsSource(StandardErrorArgumentsProvider.class)
	public void fullMessageIncludesWhen(Errors.StandardError error) {
		String extraInfo = "something you will never see in an actual error message";
		String message = error.withExtraInfo(extraInfo);
		assertTrue(message.contains(error.when().toString()));
	}
}
