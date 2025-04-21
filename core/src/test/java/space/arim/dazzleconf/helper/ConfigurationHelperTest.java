/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
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
package space.arim.dazzleconf.helper;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import space.arim.dazzleconf.V1DummyConfig;
import space.arim.dazzleconf.V1DummyConfigDefaults;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.factory.SerialisationFactory;

public class ConfigurationHelperTest {

	@TempDir
	public Path tempDir;
	
	private ConfigurationHelper<V1DummyConfig> helper;
	
	private final V1DummyConfigDefaults defaults = new V1DummyConfigDefaults();

	@BeforeEach
	public void setup() {
		helper = new ConfigurationHelper<>(tempDir, "config.yml",
				new SerialisationFactory<>(V1DummyConfig.class, V1DummyConfigDefaults.createOptions()));
	}

	@Test
	public void testLoad() {
		V1DummyConfig configData;
		try {
			configData = helper.reloadConfigData();
		} catch (IOException | InvalidConfigException ex) {
			throw Assertions.<RuntimeException>fail(ex);
		}
		defaults.assertDefaultValues(configData);
	}

	@Test
	public void testReload() {
		V1DummyConfig initialData;
		try {
			initialData = helper.reloadConfigData();
		} catch (IOException | InvalidConfigException ex) {
			throw Assertions.<RuntimeException>fail(ex);
		}
		defaults.assumeDefaultValues(initialData);

		V1DummyConfig reloadedData;
		try {
			reloadedData = helper.reloadConfigData();
		} catch (IOException | InvalidConfigException ex) {
			throw Assertions.<RuntimeException>fail(ex);
		}
		defaults.assertDefaultValues(reloadedData);
	}

}
