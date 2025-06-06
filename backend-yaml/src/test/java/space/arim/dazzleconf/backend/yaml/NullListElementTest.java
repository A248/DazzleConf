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

package space.arim.dazzleconf.backend.yaml;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.backend.StringRoot;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NullListElementTest {

	@Test
	public void readLiteralNullListElement() {
		String content = """
				list:
				  - null
				  - 'value2'
				 \s""";
		Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
		Config loaded = configuration.configureWith(new YamlBackend(new StringRoot(content))).getOrThrow();
		assertEquals(List.of("null", "value2"), loaded.list());
	}

	@Test
	public void readImplicitNullListElement() {
		String content = """
				list:
				  - 'value1'
				  -\040
				 \s""";
		Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
		Config loaded = configuration.configureWith(new YamlBackend(new StringRoot(content))).getOrThrow();
		assertEquals(List.of("value1", "null"), loaded.list());
	}

	public interface Config {

		List<String> list();
	}
}
