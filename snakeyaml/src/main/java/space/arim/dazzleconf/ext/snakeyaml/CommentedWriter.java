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

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import space.arim.dazzleconf.factory.CommentedWrapper;

class CommentedWriter {

	private final Writer writer;
	private final String commentFormat;
	
	private int depth;
	
	CommentedWriter(Writer writer, String commentFormat) {
		this.writer = writer;
		this.commentFormat = commentFormat;
	}

	/**
	 * Writes a config map
	 *
	 * @throws IOException if an I/O error occurs
	 */
	void writeMap(Map<String, Object> map) throws IOException {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof CommentedWrapper) { 
				CommentedWrapper commentWrapper = (CommentedWrapper) value;
				writeComments(commentWrapper.getComments());
				writeKey(key);
				writeValue(commentWrapper.getValue());
			} else {
				writeKey(key);
				writeValue(value);
			}
		}
	}

	/**
	 * Writes comment lines
	 *
	 * @param comments the comments to writer
	 * @throws IOException if an I/O error occurs
	 */
	void writeComments(List<String> comments) throws IOException {
		CharSequence depthPrefix = depthPrefix();
		for (String comment : comments) {
			writer.append(depthPrefix).append(String.format(commentFormat, comment)).append('\n');
		}
	}
	
	/*
	 * Depth control
	 */
	
	private CharSequence depthPrefix() {
		if (depth == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		int spaces = 2 * depth;
		for (int n = 0; n < spaces; n++) {
			builder.append(' ');
		}
		return builder;
	}
	
	@FunctionalInterface
	private interface WriterAction {
		void run() throws IOException;
	}
	
	private void descendAndDo(WriterAction whenDescended) throws IOException {
		writer.append('\n');
		depth++;
		whenDescended.run();
		depth--;
	}
	
	/*
	 * Keys
	 */
	
	private void writeKey(String key) throws IOException {
		writer.append(depthPrefix()).append(key).append(':');
	}
	
	/*
	 * Values
	 */
	
	private void writeValue(Object value) throws IOException {
		Objects.requireNonNull(value, "Null value in map entry");

		if (value instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) value;
			if (map.isEmpty()) {
				writer.append(" {}");
			} else {
				descendAndDo(() -> writeMap(map));
			}

		} else if (value instanceof List) {
			List<?> list = (List<?>) value;
			if (list.isEmpty()) {
				writer.append(" []");
			} else {
				descendAndDo(() -> {
					CharSequence depthPrefix = depthPrefix();
					for (Object element : list) {
						writer.append(depthPrefix).append("- ");
						writeSingleValue(element);
						writer.append('\n');
					}
				});
				return; // Don't write extra \n
			}
		} else {
			writer.append(' ');
			writeSingleValue(value);
		}
		writer.append('\n');
	}
	
	private void writeSingleValue(Object value) throws IOException {
		if (value instanceof String || value instanceof Character) {
			writer.append('\'');
			writer.append(value.toString().replace("'", "''"));
			writer.append('\'');
			return;
		}
		if (value instanceof Number || value instanceof Boolean) {
			writer.append(value.toString());
			return;
		}
		throw new IllegalArgumentException("Unknown single value type " + value.getClass());
	}
	
}
