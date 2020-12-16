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

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.ConfDefault.DefaultString;
import space.arim.dazzleconf.sorter.AnnotationBasedSorter;
import space.arim.dazzleconf.sorter.AnnotationBasedSorter.Order;

public class SortedYamlTest {

	@TempDir
	public Path tempDir;

	public interface Conf {

		@Order(3)
		@DefaultString("three")
		String optionThree();

		@Order(2)
		@DefaultString("two")
		String optionTwo();

		@Order(1)
		@DefaultString("one")
		String optionOne();

	}

	private ConfigurationFactory<Conf> factory;

	@BeforeEach
	public void setup() {
		factory = new SnakeYamlConfigurationFactory<>(Conf.class,
				new ConfigurationOptions.Builder().sorter(new AnnotationBasedSorter()).build());
	}

	@Test
	public void generateOrderedLines() {
		Conf defaults = factory.loadDefaults();
		assumeTrue(defaults != null);

		assertLinesMatch(
				Stream.of("optionOne: one", "optionTwo: two", "optionThree: three"),
				new ConfigToLines<>(factory).writeLines(defaults));
	}

}
