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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import space.arim.dazzleconf.factory.CommentedWrapper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class FullWriter implements YamlWriter {

	private final Writer writer;
	private final Yaml yaml;

	FullWriter(Writer writer, Yaml yaml) {
		this.writer = writer;
		this.yaml = yaml;
	}

	static final class Factory implements YamlWriter.Factory {

		static final Factory INSTANCE = new Factory();

		private Factory() {}

		@Override
		public YamlWriter newWriter(SnakeYamlOptions yamlOptions, Writer writer) {
			return new FullWriter(writer, yamlOptions.yamlSupplier().get());
		}

		@Override
		public boolean supportsComments() {
			return true;
		}

	}

	@Override
	public void writeData(Map<String, Object> configMap, List<String> commentHeader) throws IOException {
		Node mapNode = mapToNode(configMap);
		if (!commentHeader.isEmpty()) {
			mapNode.setBlockComments(convertComments(commentHeader));
		}
		try {
			yaml.serialize(mapNode, writer);
		} catch (YAMLException ex) {
			throw yamlToIoException(ex);
		}
	}

	private Node mapToNode(Map<String, Object> configMap) {
		List<NodeTuple> keyValuePairs = new ArrayList<>(configMap.size());
		configMap.forEach((key, value) -> {
			Node keyNode = yaml.represent(key);
			if (value instanceof CommentedWrapper) {
				CommentedWrapper commentWrapper = (CommentedWrapper) value;

				keyNode.setBlockComments(convertComments(commentWrapper.getComments()));
				value = commentWrapper.getValue();
			}
			Node valueNode = valueToNode(value);
			keyValuePairs.add(new NodeTuple(keyNode, valueNode));
		});
		return new MappingNode(Tag.MAP, keyValuePairs, DumperOptions.FlowStyle.BLOCK);
	}

	private Node valueToNode(Object value) {
		if (value instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) value;

			return mapToNode(map);
		}
		return yaml.represent(value);
	}

	private List<CommentLine> convertComments(List<String> comments) {
		List<CommentLine> commentLines = new ArrayList<>(comments.size());
		for (String comment : comments) {
			commentLines.add(new CommentLine(null, null, " " + comment, CommentType.BLOCK));
		}
		return commentLines;
	}

}
