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

package space.arim.dazzleconf.ext.snakeyaml.mooeypoo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class MainConfigInterfaceTest {

	private ConfigurationFactory<MainConfigInterface> factory;

	@BeforeEach
	public void setup() {
		factory = SnakeYamlConfigurationFactory.create(MainConfigInterface.class, ConfigurationOptions.defaults(),
				new SnakeYamlOptions.Builder().useCommentingWriter(true).build());
	}

	@Test
	public void loadDefaults() {
		assertDoesNotThrow(factory::loadDefaults);
	}

	@Test
	public void reloadDefaults() throws IOException, InvalidConfigException {
		var defaults = factory.loadDefaults();
		var byteArrayOutput = new ByteArrayOutputStream();
		factory.write(defaults, byteArrayOutput);
		assertDoesNotThrow(() -> factory.load(new ByteArrayInputStream(byteArrayOutput.toByteArray())));
	}
}
