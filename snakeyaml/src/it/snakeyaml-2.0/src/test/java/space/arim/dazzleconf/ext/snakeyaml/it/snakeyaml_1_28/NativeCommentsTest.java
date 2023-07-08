/*
 * DazzleConf
 * Copyright © 2023 Anand Beh
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

package space.arim.dazzleconf.ext.snakeyaml.it.snakeyaml_2_0;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class NativeCommentsTest {

	@Test
	public void writeComments() {
		Yaml yaml = new SnakeYamlOptions.Builder().build().yamlSupplier().get();

		Node mapNode;
		{
			Node keyNode = yaml.represent("key");
			keyNode.setBlockComments(List.of(new CommentLine(null, null, " Comment on entry", CommentType.BLOCK)));
			mapNode = new MappingNode(
					Tag.MAP,
					List.of(new NodeTuple(keyNode, yaml.represent("value"))),
					DumperOptions.FlowStyle.AUTO
			);
			mapNode.setBlockComments(List.of(
					new CommentLine(null, null, " Header 1", CommentType.BLOCK),
					new CommentLine(null, null, " Header 2", CommentType.BLOCK)
			));
		}
		StringWriter output = new StringWriter();
		yaml.serialize(mapNode, output);
		assertLinesMatch(Stream.of(
						"# Header 1",
						"# Header 2",
						"# Comment on entry",
						"key: value"),
				output.toString().lines(),
				() -> "Was really " + output);
	}

}
