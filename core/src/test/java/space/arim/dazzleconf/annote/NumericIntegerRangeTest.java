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

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.factory.FixedLoaderFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NumericIntegerRangeTest {

	private ConfigurationFactory<Config> factoryFrom(Map<String, Object> source) {
		return new FixedLoaderFactory<>(Config.class, ConfigurationOptions.defaults(), source);
	}

	@Test
	public void withinRangeLowerBound() {
		var factory = factoryFrom(Map.of("value", List.of("one")));
		assertDoesNotThrow(() -> factory.load(InputStream.nullInputStream()));
	}

	@Test
	public void withinRange() {
		var factory = factoryFrom(Map.of("value", List.of("one", "two")));
		assertDoesNotThrow(() -> factory.load(InputStream.nullInputStream()));
	}

	@Test
	public void withinRangeUpperBound() {
		var factory = factoryFrom(Map.of("value", List.of("one", "two", "three")));
		assertDoesNotThrow(() -> factory.load(InputStream.nullInputStream()));
	}

	@Test
	public void belowRange() {
		var factory = factoryFrom(Map.of("value", List.of("one", "two", "three", "four")));
		assertThrows(BadValueException.class, () -> factory.load(InputStream.nullInputStream()));
	}

	@Test
	public void aboveRange() {
		var factory = factoryFrom(Map.of("value", List.of("one", "two", "three", "four")));
		assertThrows(BadValueException.class, () -> factory.load(InputStream.nullInputStream()));
	}

	public interface Config {

		@CollectionSize(min = 1, max = 3)
		List<String> value();

	}
}
