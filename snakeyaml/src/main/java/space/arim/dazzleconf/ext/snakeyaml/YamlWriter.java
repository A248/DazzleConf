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
import java.util.Collections;
import java.util.List;
import java.util.Map;

interface YamlWriter {

	/**
	 * Writes a config map and its comment header
	 *
	 * @parma configMap the config map
	 * @param headerComments the comment header
	 * @throws IOException if an I/O error occurs
	 */
	void writeData(Map<String, Object> configMap, List<String> headerComments) throws IOException;

	default void writeData(Map<String, Object> configMap) throws IOException {
		writeData(configMap, Collections.emptyList());
	}

	default IOException yamlToIoException(YAMLException yamlException) {
		Throwable cause = yamlException.getCause();
		if (cause instanceof IOException) {
			return (IOException) cause;
		}
		return new IOException("Unexpected YAMLException while writing to stream", yamlException);
	}

	interface Factory {

		YamlWriter newWriter(SnakeYamlOptions yamlOptions, Writer writer);

		boolean supportsComments();

	}

}
