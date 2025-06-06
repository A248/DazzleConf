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

package space.arim.dazzleconf.backend.yaml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.PathRoot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExampleTest {

	private final Path file;

    public ExampleTest(@TempDir Path tempDir) {
        file = tempDir.resolve("config.yml");
    }

    public interface MyConfig {

		default String helloWorld() {
			return "yay";
		}
	}

	@Test
	public void helloWorld_loadDefaults() throws IOException {
		Backend yamlBackend = new YamlBackend(new PathRoot(file));
		Configuration<MyConfig> configuration = Configuration.defaultBuilder(MyConfig.class).build();
		MyConfig loaded = configuration.configureWith(yamlBackend).getOrThrow();
		assertEquals("yay", loaded.helloWorld());
		assertEquals("hello-world: yay", Files.readString(file).trim());
	}

}
