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
package space.arim.dazzleconf.ext.snakeyaml;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import space.arim.dazzleconf.factory.CommentedWrapper;

class CommentedWriter {

	private final Map<String, Object> rawMap;
	private final Writer writer;
	
	private int depth;
	
	CommentedWriter(Map<String, Object> rawMap, Writer writer) {
		this.rawMap = rawMap;
		this.writer = writer;
	}
	
	void write() throws IOException {
		writeMap(rawMap);
	}
	
	private void writeMap(Map<String, Object> map) throws IOException {
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
	
	/*
	 * Depth control
	 */
	
	private CharSequence depthPrefix() {
		StringBuilder builder = new StringBuilder();
		int spaces = 2 * depth;
		for (int n = 0; n < spaces; n++) {
			builder.append(' ');
		}
		return builder;
	}
	
	@FunctionalInterface
	private static interface IORunnable {
		void run() throws IOException;
	}
	
	private void descendAndDo(IORunnable whenDescended) throws IOException {
		writer.append('\n');
		depth++;
		whenDescended.run();
		depth--;
	}
	
	/*
	 * Comments
	 */
	
	private void writeComments(List<String> comments) throws IOException {
		CharSequence depthPrefix = depthPrefix();
		for (String comment : comments) {
			StringBuilder commentBuilder = new StringBuilder(depthPrefix);
			commentBuilder.append(" # ").append(comment).append('\n');
			writer.append(commentBuilder);
		}
	}
	
	/*
	 * Keys
	 */
	
	private void writeKey(String key) throws IOException {
		StringBuilder keyBuilder = new StringBuilder(depthPrefix());
		keyBuilder.append(key).append(':');
		writer.append(keyBuilder);
	}
	
	/*
	 * Values
	 */
	
	private void writeValue(Object value) throws IOException {
		Objects.requireNonNull(value, "Null value in map entry");

		if (value instanceof Map) {
			descendAndDo(() -> {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) value;
				writeMap(map);
			});
			return;
		}
		if (value instanceof List) {
			descendAndDo(() -> {
				CharSequence depthPrefix = depthPrefix();
				for (Object element : (List<?>) value) {
					StringBuilder elementBuilder = new StringBuilder(depthPrefix);
					elementBuilder.append('-').append(' ');
					writer.append(elementBuilder);
					writeSingleValue(element);
				}
			});
			return;
		}
		writeSingleValue(value);
	}
	
	private void writeSingleValue(Object value) throws IOException {
		writer.append(' ');
		writeSingleValue0(value);
		writer.append('\n');
	}
	
	private void writeSingleValue0(Object value) throws IOException {
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
