
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import space.arim.dazzleconf.factory.CommentedWrapper;

public class CommentedWriterTest {

	private StringWriter stringWriter;
	private CommentedWriter commentedWriter;

	@BeforeEach
	public void setup() {
		stringWriter = new StringWriter();
		commentedWriter = new CommentedWriter(stringWriter, " # %s");
	}

	@Test
	public void emptyMap() throws IOException {
		commentedWriter.writeMap(Map.of("empty-map", Map.of()));

		assertLinesMatch(Stream.of("empty-map: {}"), stringWriter.toString().lines());
	}

	@Test
	public void emptyList() throws IOException {
		commentedWriter.writeMap(Map.of("empty-list", List.of()));

		assertLinesMatch(Stream.of("empty-list: []"), stringWriter.toString().lines());
	}

	@Test
	public void writeMap() throws IOException {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("first-key", -3);
		map.put("other", true);

		Map<String, Object> nestedMap = new LinkedHashMap<>();
		nestedMap.put("sub-key", "sub-value");
		nestedMap.put("sub-list", List.of(1, 2, -15));
		map.put("nested-map", nestedMap);

		commentedWriter.writeMap(map);
		String resultString = stringWriter.toString();

		assertLinesMatch(Stream.of(
				"first-key: -3",
				"other: true",
				"nested-map:",
				"  sub-key: 'sub-value'",
				"  sub-list:",
				"    - 1",
				"    - 2",
				"    - -15",
				""),
				resultString.lines());

		assertEquals(map, new Yaml().load(new StringReader(resultString)));
	}

	@Test
	public void writeComments() throws IOException {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("first-key", -3);
		map.put("other", new CommentedWrapper(List.of("Hello", "Another comment"), true));

		Map<String, Object> nestedMap = new LinkedHashMap<>();
		nestedMap.put("sub-key", "sub-value");
		nestedMap.put("sub-list", new CommentedWrapper(List.of("Comment on sub-list"), List.of(1, 2, -15)));
		map.put("nested-map", nestedMap);

		commentedWriter.writeMap(map);
		String resultString = stringWriter.toString();

		assertLinesMatch(Stream.of(
				"first-key: -3",
				" # Hello",
				" # Another comment",
				"other: true",
				"nested-map:",
				"  sub-key: 'sub-value'",
				"   # Comment on sub-list",
				"  sub-list:",
				"    - 1",
				"    - 2",
				"    - -15",
				""),
				resultString.lines());

		try (Reader reader = new StringReader(resultString)) {
			assertConfigMapsEqual(map, new Yaml().load(reader));
		}
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
