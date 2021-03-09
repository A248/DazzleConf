
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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.yaml.snakeyaml.Yaml;

import space.arim.dazzleconf.factory.CommentedWrapper;

public class YamlWriterTest {

	private final StringWriter stringWriter = new StringWriter();
	private YamlWriter writer;

	private void setup(YamlWriter.Factory writerFactory) {
		writer = writerFactory.newWriter(new SnakeYamlOptions.Builder().build(), stringWriter);
	}

	/*
	 * Our CommentedWriter has slight differences from snakeyaml
	 */

	private String listIndent() {
		return (writer instanceof CommentedWriter) ? "  " : "";
	}

	private String quoteValue(String value) {
		return (writer instanceof CommentedWriter) ? ("'" + value + "'") : value;
	}

	/**
	 * Provides leniency in case there is an extra new line in {@code actualLines}
	 *
	 * @param expected the expected lines
	 * @param actual the actual lines
	 */
	private static void assertLinesMatch(Stream<String> expected, Stream<String> actual) {
		List<String> expectedLines = expected.collect(Collectors.toUnmodifiableList());
		List<String> actualLines = actual.collect(Collectors.toUnmodifiableList());
		if (actualLines.size() == expectedLines.size() + 1) {
			expectedLines = new ArrayList<>(expectedLines);
			expectedLines.add("");
		}
		Assertions.assertLinesMatch(expectedLines, actualLines);
	}

	@ParameterizedTest
	@ArgumentsSource(YamlWriterFactoryProvider.class)
	public void emptyMap(YamlWriter.Factory writerFactory) throws IOException {
		setup(writerFactory);

		writer.writeData(Map.of("empty-map", Map.of()));

		assertLinesMatch(Stream.of("empty-map: {}"), stringWriter.toString().lines());
	}

	@ParameterizedTest
	@ArgumentsSource(YamlWriterFactoryProvider.class)
	public void emptyList(YamlWriter.Factory writerFactory) throws IOException {
		setup(writerFactory);

		writer.writeData(Map.of("empty-list", List.of()));

		assertLinesMatch(Stream.of("empty-list: []"), stringWriter.toString().lines());
	}

	@ParameterizedTest
	@ArgumentsSource(YamlWriterFactoryProvider.class)
	public void writeMap(YamlWriter.Factory writerFactory) throws IOException {
		setup(writerFactory);

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("first-key", -3);
		map.put("other", true);

		Map<String, Object> nestedMap = new LinkedHashMap<>();
		nestedMap.put("sub-key", "sub-value");
		nestedMap.put("sub-list", List.of(1, 2, -15));
		map.put("nested-map", nestedMap);

		writer.writeData(map);
		String resultString = stringWriter.toString();

		String listIndent = listIndent();
		assertLinesMatch(Stream.of(
				"first-key: -3",
				"other: true",
				"nested-map:",
				"  sub-key: " + quoteValue("sub-value"),
				"  sub-list:",
				listIndent + "  - 1",
				listIndent + "  - 2",
				listIndent + "  - -15"),
				resultString.lines());

		assertEquals(map, new Yaml().load(new StringReader(resultString)));
	}

	@ParameterizedTest
	@ArgumentsSource(YamlWriterFactoryProvider.class)
	public void writeListWithMaps(YamlWriter.Factory writerFactory) throws IOException {
		setup(writerFactory);

		Map<String, Object> mapInList = new LinkedHashMap<>();
		mapInList.put("key1", "value");
		mapInList.put("key2", 3);
		writer.writeData(Map.of("config", List.of(mapInList)));

		String listIndent = listIndent();
		assertLinesMatch(Stream.of(
				"config:",
				listIndent + "- key1: " + quoteValue("value"),
				listIndent + "  key2: 3"),
				stringWriter.toString().lines());
	}

	@ParameterizedTest
	@ArgumentsSource(CommentingYamlWriterFactoryProvider.class)
	public void writeComments(YamlWriter.Factory writerFactory) throws IOException {
		setup(writerFactory);

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("first-key", new CommentedWrapper(List.of("First comment"), -3));
		map.put("second-key", 1);
		map.put("other", new CommentedWrapper(List.of("Hello", "Another comment"), true));

		Map<String, Object> nestedMap = new LinkedHashMap<>();
		nestedMap.put("sub-key", "sub-value");
		nestedMap.put("sub-list", new CommentedWrapper(List.of("Comment on sub-list"), List.of(1, 2, -15)));
		map.put("nested-map", nestedMap);

		writer.writeData(map);
		String resultString = stringWriter.toString();

		String listIndent = listIndent();
		assertLinesMatch(Stream.of(
				"# First comment",
				"first-key: -3",
				"second-key: 1",
				"# Hello",
				"# Another comment",
				"other: true",
				"nested-map:",
				"  sub-key: " + quoteValue("sub-value"),
				"  # Comment on sub-list",
				"  sub-list:",
				listIndent + "  - 1",
				listIndent + "  - 2",
				listIndent + "  - -15"),
				resultString.lines());

		try (Reader reader = new StringReader(resultString)) {
			assertConfigMapsEqual(map, new Yaml().load(reader));
		}
	}

	@ParameterizedTest
	@ArgumentsSource(CommentingYamlWriterFactoryProvider.class)
	public void writeCommentHeader(YamlWriter.Factory writerFactory) throws IOException {
		setup(writerFactory);

		writer.writeData(
				Map.of("key", new CommentedWrapper(List.of("Comment on entry"), "value")),
				List.of("Header 1", "Header 2"));
		String resultString = stringWriter.toString();

		assertLinesMatch(Stream.of(
				"# Header 1",
				"# Header 2",
				"# Comment on entry",
				"key: " + quoteValue("value")),
				resultString.lines());
	}
	
	private static void assertConfigMapsEqual(Map<String, Object> map1, Map<String, Object> map2) {
		map2 = new HashMap<>(map2);
		for (Map.Entry<String, Object> entry : map1.entrySet()) {
			assertEqualsUsingWrapper(entry.getValue(), map2.remove(entry.getKey()));
		}
		assertTrue(map2.isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	private static void assertEqualsUsingWrapper(Object object1, Object object2) {
		if (object1 instanceof CommentedWrapper) {
			assertEqualsUsingWrapper(((CommentedWrapper) object1).getValue(), object2);
			return;
		}
		if (object2 instanceof CommentedWrapper) {
			assertEqualsUsingWrapper(object1, ((CommentedWrapper) object2).getValue());
			return;
		}
		if (object1 instanceof Map && object2 instanceof Map) {
			assertConfigMapsEqual((Map<String, Object>) object1, (Map<String, Object>) object2);
			return;
		}
		assertEquals(object1, object2);
	}
	
}
