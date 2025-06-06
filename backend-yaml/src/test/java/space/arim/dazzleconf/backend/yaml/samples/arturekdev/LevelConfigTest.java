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

package space.arim.dazzleconf.backend.yaml.samples.arturekdev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.backend.yaml.YamlBackend;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;
import space.arim.dazzleconf2.engine.liaison.SubSection;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Version 2.0 of the library does not yet provide a map liaison")
public class LevelConfigTest {

	private Configuration<LevelConfig> configuration;

	@BeforeEach
	public void setup() {
		configuration = Configuration.defaultBuilder(LevelConfig.class).build();
	}

	@Test
	public void loadDefaults() {
		LevelConfig config = assertDoesNotThrow(configuration::loadDefaults);
		assertEquals(new LevelConfig() {}.levels(), config.levels());
	}

	@Test
	public void writeValues() {
		LevelConfig config = new LevelConfig() {
			@Override
			public Map<Integer, @SubSection Level> levels() {
				return Map.of(1, Level.of(1, 4, -3, 5, 10));
			}
		};
		DataTree.Mut dataTree = new DataTree.Mut();
		configuration.writeTo(config, dataTree);
		assertDoesNotThrow(() -> new YamlBackend(new StringRoot("")).write(Backend.Document.simple(dataTree)));
	}
}
