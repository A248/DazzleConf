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

package space.arim.dazzleconf.ext.gson;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.error.MissingKeyException;
import space.arim.dazzleconf.sorter.AnnotationBasedSorter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GsonConfigurationFactoryTest {

	private <C> Stream<String> configToLines(ConfigurationFactory<C> factory, C config) throws IOException {
		var byteArrayOutput = new ByteArrayOutputStream();
		factory.write(config, byteArrayOutput);
		return byteArrayOutput.toString(StandardCharsets.UTF_8).lines();
	}

	@Test
	public void writeOrderedJson() throws IOException, InvalidConfigException {
		ConfigurationFactory<Config> factory = new GsonConfigurationFactory<>(Config.class,
				new ConfigurationOptions.Builder().sorter(new AnnotationBasedSorter()).build());

		Config defaults = factory.loadDefaults();

		assertLinesMatch(Stream.of(
				"{",
				"  \"optionOne\": \"one\",",
				"  \"optionTwo\": 5,",
				"  \"optionThree\": false",
				"}"),
				configToLines(factory, defaults));

		var byteArrayOutput = new ByteArrayOutputStream();
		factory.write(defaults, byteArrayOutput);
		Config reloaded = factory.load(new ByteArrayInputStream(byteArrayOutput.toByteArray()));
		assertEquals(defaults.optionOne(), reloaded.optionOne());
		assertEquals(defaults.optionTwo(), reloaded.optionTwo());
		assertEquals(defaults.optionThree(), reloaded.optionThree());
	}

	@Test
	public void writePseudoComments() throws IOException {
		ConfigurationFactory<Config> factory = new GsonConfigurationFactory<>(Config.class,
				new ConfigurationOptions.Builder().sorter(new AnnotationBasedSorter()).build(),
				new GsonOptions.Builder().pseudoCommentsSuffix("-comment").build());

		assertLinesMatch(Stream.of(
				"{",
				"  \"optionOne-comment\": \"Comment on first option\",",
				"  \"optionOne\": \"one\",",
				"  \"optionTwo\": 5,",
				"  \"optionThree\": false",
				"}"),
				configToLines(factory, factory.loadDefaults()));
	}

	private InputStream streamFor(String content) {
		return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "    ", " \n "})
	public void loadEmptyDocument(String emptyString) {
		var factory = new GsonConfigurationFactory<>(Config.class, ConfigurationOptions.defaults());
		var stream = streamFor(emptyString);
		assertThrows(MissingKeyException.class, () -> factory.load(stream));
	}
	
	@Test
	public void loadMissingKeys() {
		var factory = new GsonConfigurationFactory<>(Config.class, ConfigurationOptions.defaults());
		var stream = streamFor("{\"optionOne\": \"one\"}");
		assertThrows(MissingKeyException.class, () -> factory.load(stream));
	}

}
