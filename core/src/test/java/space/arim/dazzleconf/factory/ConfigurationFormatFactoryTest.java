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

package space.arim.dazzleconf.factory;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.ConfHeader;
import space.arim.dazzleconf.error.InvalidConfigException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationFormatFactoryTest {

	private ConfigurationFormatFactory<Config> factory() {
		return new SerialisationFactory<>(Config.class, ConfigurationOptions.defaults());
	}

	@Test
	public void getHeader() {
		assertEquals(List.of("Header 1", "Header 2"), factory().getHeader());
	}

	@Test
	public void reloadSameConfig() throws IOException, InvalidConfigException {
		Config config = Config.withValues("val1", "val2");
		var output = new ByteArrayOutputStream();
		var factory = factory();
		factory.write(config, output);
		Config reloaded = factory.load(new ByteArrayInputStream(output.toByteArray()));
		assertEquals("val1", reloaded.valueOne());
		assertEquals("val2", reloaded.valueTwo());
	}

	@ConfHeader({"Header 1", "Header 2"})
	public interface Config {

		static Config withValues(String valueOne, String valueTwo) {
			return new Config() {
				@Override
				public String valueOne() { return valueOne; }

				@Override
				public String valueTwo() { return valueTwo; }
			};
		}

		String valueOne();

		String valueTwo();
	}
}
