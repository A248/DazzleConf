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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.yaml.snakeyaml.Yaml;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NonStringKeysTest {

	private final Map<Integer, String> values = Map.of(1, "value1", 2, "value2");

	private ConfigurationFactory<Config> factoryFrom(CommentMode commentMode) {
		return SnakeYamlConfigurationFactory.create(Config.class, ConfigurationOptions.defaults(),
				new SnakeYamlOptions.Builder().commentMode(commentMode).build());
	}

	@ParameterizedTest
	@ArgumentsSource(CommentModeArgumentsProvider.class)
	public void writeNonStringKeys(CommentMode commentMode) {
		var factory = factoryFrom(commentMode);
		Config config = () -> values;

		var output = new ByteArrayOutputStream();
		assertDoesNotThrow(() -> factory.write(config, output));
		assertEquals(Map.of("map", values), new Yaml().load(new ByteArrayInputStream(output.toByteArray())));
	}

	@Test
	public void loadNonStringKeys() {
		var factory = factoryFrom(CommentMode.fullComments());

		StringWriter writer = new StringWriter();
		new Yaml().dump(Map.of("map", values), writer);
		InputStream input = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));

		Config config = assertDoesNotThrow(() -> factory.load(input));
		assertEquals(values, config.map());
	}

	public interface Config {

		Map<Integer, String> map();
	}
}
