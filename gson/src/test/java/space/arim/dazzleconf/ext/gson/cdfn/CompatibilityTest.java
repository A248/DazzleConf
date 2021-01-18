/*
 * DazzleConf
 * Copyright © 2020 Anand Beh
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

package space.arim.dazzleconf.ext.gson.cdfn;

import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.gson.GsonConfigurationFactory;
import space.arim.dazzleconf.ext.gson.GsonOptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * GsonConfigurationFactory in release 1.1.0 uses a default Gson instance of
 * new GsonBuilder().setPrettyPrinting().setLenient().create(). 1.2.0 adds disableHtmlEscaping,
 * and compatibility ought to be ensured by this test.
 *
 */
public class CompatibilityTest {

	private ConfigurationFactory<CDFNConfig> previousFactory;
	private ConfigurationFactory<CDFNConfig> currentFactory;

	@BeforeEach
	public void setup() {
		previousFactory = new GsonConfigurationFactory<>(
				CDFNConfig.class,
				ConfigurationOptions.defaults(),
				new GsonOptions.Builder().gson(
						new GsonBuilder().setPrettyPrinting().setLenient().create()
				).build());
		currentFactory = new GsonConfigurationFactory<>(
				CDFNConfig.class,
				ConfigurationOptions.defaults());
	}

	private static void assertCompatible(ConfigurationFactory<CDFNConfig> writeWith,
										 ConfigurationFactory<CDFNConfig> loadWith)
			throws IOException, InvalidConfigException {
		var baos = new ByteArrayOutputStream();
		CDFNConfig defaults = writeWith.loadDefaults();
		writeWith.write(defaults, baos);

		CDFNConfig reloaded = loadWith.load(new ByteArrayInputStream(baos.toByteArray()));
		assertEquals(defaults.format(), reloaded.format());
	}

	@Test
	public void backwardCompat() throws IOException, InvalidConfigException {
		assertCompatible(previousFactory, currentFactory);
	}

	@Test
	public void forwardCompat() throws IOException, InvalidConfigException {
		assertCompatible(currentFactory, previousFactory);
	}

}
