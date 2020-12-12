/* 
 * DazzleConf-snakeyaml
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-snakeyaml is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-snakeyaml is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-snakeyaml. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.ext.snakeyaml.bluetree;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;
import space.arim.dazzleconf.helper.ConfigurationHelper;
import space.arim.dazzleconf.sorter.AnnotationBasedSorter;

public class BlueTreeConfigManagerTest {

	@TempDir
	public Path tempDir;
	private static final String FILENAME = "SQL.yml";

	private ConfigurationHelper<BlueTreeConfig> helper;

	@BeforeEach
	public void setup() {
		helper = new ConfigurationHelper<>(tempDir, FILENAME,
				new SnakeYamlConfigurationFactory<>(BlueTreeConfig.class,
						new ConfigurationOptions.Builder().sorter(new AnnotationBasedSorter()).build(),
						new SnakeYamlOptions.Builder().useCommentingWriter(true).build()));
	}

	@Test
	public void testSortedWrite() throws IOException, InvalidConfigException {
		helper.reloadConfigData();
		assertLinesMatch(
				BlueTreeConfig.EXPECTED_LINES,
				Files.readAllLines(tempDir.resolve(FILENAME), StandardCharsets.UTF_8));
	}

}
