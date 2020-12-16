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
package space.arim.dazzleconf.ext.snakeyaml.bluetree;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.ConfigToLines;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;
import space.arim.dazzleconf.helper.ConfigurationHelper;
import space.arim.dazzleconf.sorter.AnnotationBasedSorter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlueTreeConfigTest {

	private ConfigurationFactory<BlueTreeConfig> factory;

	@TempDir
	public Path tempDir;

	@BeforeEach
	public void setup() {
		factory = new SnakeYamlConfigurationFactory<>(BlueTreeConfig.class,
				new ConfigurationOptions.Builder().sorter(new AnnotationBasedSorter()).build(),
				new SnakeYamlOptions.Builder().useCommentingWriter(true).build());
	}

	@Test
	public void generatedLinesMatch() {
		BlueTreeConfig defaults = factory.loadDefaults();
		assumeTrue(defaults != null);

		assertLinesMatch(
				BlueTreeConfig.EXPECTED_LINES.stream(),
				new ConfigToLines<>(factory).writeLines(defaults));
	}

	@Test
	public void generatedLinesMatchUsingHelper() throws IOException, InvalidConfigException {
		ConfigurationHelper<BlueTreeConfig> helper;
		final String FILENAME = "SQL.yml";
		helper = new ConfigurationHelper<>(tempDir, FILENAME, factory);

		helper.reloadConfigData();
		assertLinesMatch(
				BlueTreeConfig.EXPECTED_LINES,
				Files.readAllLines(tempDir.resolve(FILENAME), StandardCharsets.UTF_8));
	}

}
