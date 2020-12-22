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

package space.arim.dazzleconf.ext.snakeyaml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.MissingKeyException;
import space.arim.dazzleconf.sorter.AnnotationBasedSorter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class SnakeYamlConfigurationFactoryTest {

	private ConfigurationFactory<Config> factory;

	@BeforeEach
	public void setup() {
		factory = new SnakeYamlConfigurationFactory<>(Config.class,
				new ConfigurationOptions.Builder().sorter(new AnnotationBasedSorter()).build());
	}

	@Test
	public void writeOrderedYaml() {
		Config defaults = factory.loadDefaults();
		assumeTrue(defaults != null);

		assertLinesMatch(
				Stream.of("optionOne: one", "optionTwo: two", "optionThree: three"),
				new ConfigToLines<>(factory).writeLines(defaults));
	}

	private InputStream streamFor(String content) {
		return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "    ", " \n "})
	public void loadEmptyDocument(String emptyString) {
		var stream = streamFor(emptyString);
		assertThrows(MissingKeyException.class, () -> factory.load(stream));
	}

	@Test
	public void loadMissingKeys() {
		var stream = streamFor("optionThree: 'three'");
		assertThrows(MissingKeyException.class, () -> factory.load(stream));
	}

}
