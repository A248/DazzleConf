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

package space.arim.dazzleconf.ext.snakeyaml.arturekdev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.ext.snakeyaml.CommentMode;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;

import java.io.OutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LevelConfigTest {

	private ConfigurationFactory<LevelConfig> factory;

	@BeforeEach
	public void setup() {
		SnakeYamlOptions yamlOptions = new SnakeYamlOptions.Builder().commentMode(CommentMode.alternativeWriter()).build();
		factory = SnakeYamlConfigurationFactory.create(LevelConfig.class, ConfigurationOptions.defaults(), yamlOptions);
	}

	@Test
	public void loadDefaults() {
		LevelConfig config = assertDoesNotThrow(factory::loadDefaults);
		assertEquals(LevelConfig.defaultLevels(), config.levels());
	}

	@Test
	public void writeValues() {
		LevelConfig config = () -> Map.of(1, Level.of(1, 4, -3, 5, 10));
		assertDoesNotThrow(() -> factory.write(config, OutputStream.nullOutputStream()));
	}
}
