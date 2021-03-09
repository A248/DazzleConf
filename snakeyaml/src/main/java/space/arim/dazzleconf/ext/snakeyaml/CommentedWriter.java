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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import space.arim.dazzleconf.factory.CommentedWrapper;

final class CommentedWriter implements YamlWriter {

	private final Writer writer;
	private final String commentFormat;
	
	private int depth;
	/**
	 * Indicates that the next value about to be written is a map entry
	 * which is part of a map which is part of a list. Comments for the entry
	 * have already been written on the list element, and leading whitespace
	 * has also already been written before the key.
	 */
	private boolean firstMapEntryIsPartOfList;
	
	CommentedWriter(Writer writer, String commentFormat) {
		this.writer = writer;
		this.commentFormat = commentFormat;
	}

	static final class Factory implements YamlWriter.Factory {

		private final String commentFormat;

		Factory(String commentFormat) {
			this.commentFormat = commentFormat;
		}

		@Override
		public CommentedWriter newWriter(SnakeYamlOptions yamlOptions, Writer writer) {
			return new CommentedWriter(writer, commentFormat);
		}

		@Override
		public boolean supportsComments() {
			return true;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Factory factory = (Factory) o;
			return commentFormat.equals(factory.commentFormat);
		}

		@Override
		public int hashCode() {
			return commentFormat.hashCode();
		}
	}

	@Override
	public void writeData(Map<String, Object> configMap, List<String> headerComments) throws IOException {
		writeComments(headerComments);
		writeMap(configMap);
	}

	/**
	 * Writes a config map
	 *
	 * @throws IOException if an I/O error occurs
	 */
	void writeMap(Map<String, Object> map) throws IOException {
		writeRemainingMapEntries(map.entrySet().iterator());
	}

	private void writeRemainingMapEntries(Iterator<Map.Entry<String, Object>> entryIterator) throws IOException {
		while (entryIterator.hasNext()) {
			Map.Entry<String, Object> entry = entryIterator.next();
			String key = entry.getKey();
			Object value = entry.getValue();

			if (!firstMapEntryIsPartOfList && value instanceof CommentedWrapper) {
				CommentedWrapper commentWrapper = (CommentedWrapper) value;
				writeComments(commentWrapper);
				writeKey(key);
				writeValue(commentWrapper.getValue());
			} else {
				if (firstMapEntryIsPartOfList && value instanceof CommentedWrapper) {
					value = ((CommentedWrapper) value).getValue();
				}
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

	private void writeComments(CommentedWrapper commentWrapper) throws IOException {
		writeComments(commentWrapper.getComments());
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
		depth++;
		whenDescended.run();
		depth--;
	}
	
	/*
	 * Keys
	 */
	
	private void writeKey(String key) throws IOException {
		if (firstMapEntryIsPartOfList) {
			firstMapEntryIsPartOfList = false;
		} else {
			writer.append(depthPrefix());
		}
		writer.append(key).append(':');
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
				writer.append('\n');
				descendAndDo(() -> writeMap(map));
			}
		} else if (value instanceof List) {
			List<?> list = (List<?>) value;
			if (list.isEmpty()) {
				writer.append(" []");
			} else {
				writer.append('\n');
				descendAndDo(() -> {
					CharSequence depthPrefix = depthPrefix();
					for (Object element : list) {
						if (element instanceof Map) {
							@SuppressWarnings("unchecked")
							Map<String, Object> map = (Map<String, Object>) element;
							if (map.isEmpty()) {
								writer.append(depthPrefix).append("- {}");
							} else {
								/*
								 * All of this is necessary because comments on the first entry
								 * of the map must be written before the list element
								 */
								Iterator<Map.Entry<String, Object>> entryIterator = map.entrySet().iterator();
								// Perform a Iterator.peek()
								Map.Entry<String, Object> firstEntry = entryIterator.next();
								Object firstEntryValue = firstEntry.getValue();
								if (firstEntryValue instanceof CommentedWrapper) {
									descendAndDo(() -> writeComments((CommentedWrapper) firstEntryValue));
								}
								// Put the element back; completing the peek()
								Iterator<Map.Entry<String, Object>> newIterator
										= new IteratorWithElementPrepended<>(firstEntry, entryIterator);

								// Actually write the values
								writer.append(depthPrefix).append("- ");
								firstMapEntryIsPartOfList = true;
								descendAndDo(() -> writeRemainingMapEntries(newIterator));
							}
						} else {
							writer.append(depthPrefix).append('-');
							writeSingleValue(element);
						}
						writer.append('\n');
					}
				});
				return; // Don't write extra \n
			}
		} else {
			writeSingleValue(value);
		}
		writer.append('\n');
	}
	
	private void writeSingleValue(Object value) throws IOException {
		writer.append(' ');
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
