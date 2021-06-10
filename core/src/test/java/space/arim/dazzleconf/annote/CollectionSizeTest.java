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

package space.arim.dazzleconf.annote;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.factory.FixedLoaderFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CollectionSizeTest {

	private ConfigurationFactory<Config> factoryFrom(Map<String, Object> source) {
		return new FixedLoaderFactory<>(Config.class, ConfigurationOptions.defaults(), source);
	}

	@TestFactory
	public Stream<DynamicNode> allWithinRange() {
		return Stream.of(
				Config.sourceWithValues(0D, 0, 0D, 0L),
				Config.sourceWithValues(-2D, -2, -2D, -2L),
				Config.sourceWithValues(2D, 2, 2D, 2L)
		).map((configSource) -> DynamicTest.dynamicTest("Valid config within range", () -> {
			var factory = factoryFrom(configSource);
			assertDoesNotThrow(() -> factory.load(InputStream.nullInputStream()));
		}));
	}

	@ParameterizedTest
	@ValueSource(doubles = {-3D, 3D})
	public void doubleOutOfNumericRange(double value) {
		var factory = factoryFrom(Config.sourceWithValues(value, 0, 0D, 0L));
		assertThrows(BadValueException.class, () -> factory.load(InputStream.nullInputStream()));
	}

	@ParameterizedTest
	@ValueSource(ints = {-3, 3})
	public void integerOutOfNumericRange(int value) {
		var factory = factoryFrom(Config.sourceWithValues(0, value, 0D, 0L));
		assertThrows(BadValueException.class, () -> factory.load(InputStream.nullInputStream()));
	}

	@ParameterizedTest
	@ValueSource(doubles = {-3D, 3D})
	public void doubleOutOfIntegerRange(double value) {
		var factory = factoryFrom(Config.sourceWithValues(0D, 0, value, 0L));
		assertThrows(BadValueException.class, () -> factory.load(InputStream.nullInputStream()));
	}

	@ParameterizedTest
	@ValueSource(longs = {-3L, 3L})
	public void longOutOfIntegerRange(long value) {
		var factory = factoryFrom(Config.sourceWithValues(0D, 0, 0D, value));
		assertThrows(BadValueException.class, () -> factory.load(InputStream.nullInputStream()));
	}

	public interface Config {

		static Map<String, Object> sourceWithValues(double doubleUsingNumericRange, int integerUsingNumericRange,
													double doubleUsingIntegerRange, long longUsingIntegerRange) {
			return Map.of("doubleUsingNumericRange", doubleUsingNumericRange,
					"integerUsingNumericRange", integerUsingNumericRange,
					"doubleUsingIntegerRange", doubleUsingIntegerRange,
					"longUsingIntegerRange", longUsingIntegerRange);
		}

		@NumericRange(min = -2D, max = 2D)
		double doubleUsingNumericRange();

		@NumericRange(min = -2D, max = 2D)
		int integerUsingNumericRange();

		@IntegerRange(min = -2L, max = 2L)
		double doubleUsingIntegerRange();

		@IntegerRange(min = -2L, max = 2L)
		long longUsingIntegerRange();

	}
}
