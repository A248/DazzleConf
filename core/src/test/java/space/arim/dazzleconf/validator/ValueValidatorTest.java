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

package space.arim.dazzleconf.validator;

import org.junit.jupiter.api.Test;
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

public class ValueValidatorTest {

	private ConfigurationFactory<Config> factoryFrom(Validator validator, Map<String, Object> source) {
		ConfigurationOptions options = new ConfigurationOptions.Builder().addValidator("key", validator).build();
		return new FixedLoaderFactory<>(Config.class, options, source);
	}

	@Test
	public void validatorSeesValue() throws IOException, InvalidConfigException {
		Validator validator = new Validator();
		Map<String, Object> source = Map.of("key", "value");
		factoryFrom(validator, source).load(InputStream.nullInputStream());
		assertEquals("value", validator.calledWith);
	}

	@Test
	public void validatorRejectsValue() {
		Validator validator = new Validator() {
			@Override
			public void validate(String key, Object value) throws BadValueException {
				super.validate(key, value);
				throw new BadValueException.Builder().key(key).build();
			}
		};
		Map<String, Object> source = Map.of("key", "value");
		var factory = factoryFrom(validator, source);
		assertThrows(BadValueException.class, () -> factory.load(InputStream.nullInputStream()));
		assertEquals("value", validator.calledWith);
	}

	public interface Config {

		String key();
	}

	private static class Validator implements ValueValidator {

		String calledWith;

		@Override
		public void validate(String key, Object value) throws BadValueException {
			if (value instanceof String calledWith) {
				this.calledWith = calledWith;
			}
		}
	}


}
