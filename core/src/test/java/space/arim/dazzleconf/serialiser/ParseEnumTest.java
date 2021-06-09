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

public class ParseEnumTest {

	private ConfigurationFactory<Config> factoryFrom(ConfigurationOptions options, Map<String, Object> source) {
		return new FixedLoaderFactory<>(Config.class, options, source);
	}

	private void assertSuccess(boolean strictParseEnums, Object representation) throws IOException, InvalidConfigException {
		Serializer serializer = new Serializer() {

			@Override
			public TheType deserialise(FlexibleType flexibleType) throws BadValueException {
				wasCalled = true;
				assertEquals(TheEnum.ENTRY_ONE, flexibleType.getEnum(TheEnum.class));
				return new TheType();
			}

		};
		ConfigurationOptions options = new ConfigurationOptions.Builder()
				.addSerialiser(serializer).setStrictParseEnums(strictParseEnums).build();
		Map<String, Object> source = Map.of("value", representation);
		factoryFrom(options, source).load(InputStream.nullInputStream());
		assertTrue(serializer.wasCalled);
	}

	private void assertFailure(boolean strictParseEnums, Object representation) throws IOException, InvalidConfigException {
		Serializer serializer = new Serializer() {

			@Override
			public TheType deserialise(FlexibleType flexibleType) {
				wasCalled = true;
				assertThrows(BadValueException.class, () -> flexibleType.getEnum(TheEnum.class));
				return new TheType();
			}

		};
		ConfigurationOptions options = new ConfigurationOptions.Builder()
				.addSerialiser(serializer).setStrictParseEnums(strictParseEnums).build();
		Map<String, Object> source = Map.of("value", representation);
		factoryFrom(options, source).load(InputStream.nullInputStream());
		assertTrue(serializer.wasCalled);
	}

	// Tests which should not be affected by case-sensitivity

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void alreadyAnEnum(boolean strictParseEnums) throws IOException, InvalidConfigException {
		assertSuccess(strictParseEnums, TheEnum.ENTRY_ONE);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void parseEnum(boolean strictParseEnums) throws IOException, InvalidConfigException {
		assertSuccess(strictParseEnums, "ENTRY_ONE");
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void failToParseEnum(boolean strictParseEnums) throws IOException, InvalidConfigException {
		assertFailure(strictParseEnums, "not the right value");
	}

	// Tests where parsing succeeds reliant on case sensitivity

	@ParameterizedTest
	@ValueSource(strings = {"entry_one", "EnTRy_One"})
	public void parseEnumCaseInsensitive(String providedValue) throws IOException, InvalidConfigException {
		assertSuccess(false, providedValue);
	}

	@ParameterizedTest
	@ValueSource(strings = {"entry_one", "EnTRy_One"})
	public void failToParseEnumCaseSensitive(String providedValue) throws IOException, InvalidConfigException {
		assertFailure(true, providedValue);
	}

	public enum TheEnum {
		ENTRY_ONE
	}
}
