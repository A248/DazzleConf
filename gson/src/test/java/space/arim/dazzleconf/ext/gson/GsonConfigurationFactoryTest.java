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
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.sorter.AnnotationBasedSorter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class GsonConfigurationFactoryTest {

	private <C> Stream<String> configToLines(ConfigurationFactory<C> factory, C config) throws IOException {
		var byteArrayOutput = new ByteArrayOutputStream();
		factory.write(config, byteArrayOutput);
		return byteArrayOutput.toString(StandardCharsets.UTF_8).lines();
	}

	@Test
	public void orderedJson() throws IOException, InvalidConfigException {
		ConfigurationFactory<Conf> factory = new GsonConfigurationFactory<>(Conf.class,
				new ConfigurationOptions.Builder().sorter(new AnnotationBasedSorter()).build());

		Conf defaults = factory.loadDefaults();

		assertLinesMatch(Stream.of(
				"{",
				"  \"optionOne\": \"one\",",
				"  \"optionTwo\": 5,",
				"  \"optionThree\": false",
				"}"),
				configToLines(factory, defaults));

		var byteArrayOutput = new ByteArrayOutputStream();
		factory.write(defaults, byteArrayOutput);
		Conf reloaded = factory.load(new ByteArrayInputStream(byteArrayOutput.toByteArray()));
		assertEquals(defaults.optionOne(), reloaded.optionOne());
		assertEquals(defaults.optionTwo(), reloaded.optionTwo());
		assertEquals(defaults.optionThree(), reloaded.optionThree());
	}

	@Test
	public void pseudoComments() throws IOException {
		ConfigurationFactory<Conf> factory = new GsonConfigurationFactory<>(Conf.class,
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

	public interface Conf {

		@ConfComments("Comment on first option")
		@AnnotationBasedSorter.Order(1)
		@ConfDefault.DefaultString("one")
		String optionOne();

		@AnnotationBasedSorter.Order(2)
		@ConfDefault.DefaultInteger(5)
		int optionTwo();

		@AnnotationBasedSorter.Order(3)
		@ConfDefault.DefaultBoolean(false)
		boolean optionThree();
	}

}
