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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import space.arim.dazzleconf.factory.CommentedWrapper;

public class CommentedWriterTest {
	
	@Test
	public void testCommentedWriter() throws IOException {
		Map<String, Object> map = Map.of(
				"first-key", -3,
				"other", new CommentedWrapper(List.of("Hello", "Another comment!"), true),
				"nested-map", Map.of("sub-key", "sub-value", "sub-list", List.of(1, 2, -15))
				);

		String fullString;
		try (StringWriter writer = new StringWriter()) {
			new CommentedWriter(map, writer).write();
			fullString = writer.toString();
		}
		try (Reader reader = new StringReader(fullString)) {
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
		assertTrue(object1.equals(object2));
	}
	
}
