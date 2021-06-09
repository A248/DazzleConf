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

package space.arim.dazzleconf.serialiser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.factory.FixedLoaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParseBooleanTest {

	private ConfigurationFactory<Config> factoryFrom(Serializer serializer, Map<String, Object> source) {
		ConfigurationOptions options = new ConfigurationOptions.Builder().addSerialiser(serializer).build();
		return new FixedLoaderFactory<>(Config.class, options, source);
	}

	private void assertSuccess(boolean value, Object representation) throws IOException, InvalidConfigException {
		Serializer serializer = new Serializer() {

			@Override
			public TheType deserialise(FlexibleType flexibleType) throws BadValueException {
				wasCalled = true;
				assertEquals(value, flexibleType.getBoolean());
				return new TheType();
			}
		};
		factoryFrom(serializer, Map.of("value", representation)).load(InputStream.nullInputStream());
		assertTrue(serializer.wasCalled);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void simpleBoolean(boolean value) throws IOException, InvalidConfigException {
		assertSuccess(value, value);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void stringBoolean(boolean value) throws IOException, InvalidConfigException {
		assertSuccess(value, Boolean.toString(value));
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void stringBooleanIgnoreCase(boolean value) throws IOException, InvalidConfigException {
		assertSuccess(value, (value) ? "True" : "False");
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void yesNoStringBoolean(boolean value) throws IOException, InvalidConfigException {
		assertSuccess(value, (value) ? "yes" : "no");
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void yesNoStringBooleanIgnoreCase(boolean value) throws IOException, InvalidConfigException {
		assertSuccess(value, (value) ? "Yes" : "No");
	}

	@Test
	public void notABoolean() throws IOException, InvalidConfigException {
		Serializer serializer = new Serializer() {

			@Override
			public TheType deserialise(FlexibleType flexibleType) {
				wasCalled = true;
				assertThrows(BadValueException.class, flexibleType::getBoolean);
				return new TheType();
			}
		};
		factoryFrom(serializer, Map.of("value", "not a boolean")).load(InputStream.nullInputStream());
		assertTrue(serializer.wasCalled);
	}
}
