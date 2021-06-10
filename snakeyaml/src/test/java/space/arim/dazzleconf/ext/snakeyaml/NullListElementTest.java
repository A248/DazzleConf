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

package space.arim.dazzleconf.ext.snakeyaml;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class NullListElementTest {

	@Disabled("Fix scheduled for 1.3.0. See https://github.com/A248/DazzleConf/issues/23")
	@Test
	public void readLiteralNullListElement() throws IOException, InvalidConfigException {
		String content = """
				list:
				  - null
				  - 'value2'
				  """;
		var factory = SnakeYamlConfigurationFactory.create(ListConfig.class, ConfigurationOptions.defaults());
		ListConfig config = factory.load(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		assertEquals(List.of("value2"), config.list());
	}

	@Disabled("See prior")
	@Test
	public void readImplicitNullListElement() throws IOException, InvalidConfigException {
		String content = """
				list:
				  - 'value1'
				  -\040
				  """;
		var factory = SnakeYamlConfigurationFactory.create(ListConfig.class, ConfigurationOptions.defaults());
		ListConfig config = factory.load(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		assertEquals(List.of("value1"), config.list());
	}

	public interface ListConfig {

		List<String> list();
	}
}
