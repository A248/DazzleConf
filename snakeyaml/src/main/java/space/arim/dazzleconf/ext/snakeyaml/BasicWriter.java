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

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

final class BasicWriter implements YamlWriter {

	private final Writer writer;
	private final Yaml yaml;
	private final CommentedWriter commentWriter;

	BasicWriter(Writer writer, Yaml yaml, CommentedWriter commentWriter) {
		this.writer = writer;
		this.yaml = yaml;
		this.commentWriter = commentWriter;
	}

	static final class Factory implements YamlWriter.Factory {

		static final Factory INSTANCE = new Factory();

		private Factory() {}

		@Override
		public YamlWriter newWriter(SnakeYamlOptions yamlOptions, Writer writer) {
			return new BasicWriter(
					writer,
					yamlOptions.yamlSupplier().get(),
					new CommentedWriter(writer, CommentMode.DEFAULT_COMMENT_FORMAT));
		}

		@Override
		public boolean supportsComments() {
			return false;
		}

	}

	@Override
	public void writeData(Map<String, Object> configMap, List<String> headerComments) throws IOException {
		commentWriter.writeComments(headerComments);
		try {
			yaml.dump(configMap, writer);
		} catch (YAMLException ex) {
			throw yamlToIoException(ex);
		}
	}

}
