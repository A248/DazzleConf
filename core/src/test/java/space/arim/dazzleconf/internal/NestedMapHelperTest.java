/* 
 * DazzleConf-core
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-core. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import space.arim.dazzleconf.error.MissingKeyException;
import space.arim.dazzleconf.factory.CommentedWrapper;

public class NestedMapHelperTest {

	private final NestedMapHelper mapHelper = new NestedMapHelper(new HashMap<String, Object>());
	
	@Test
	public void basicPutAndCombine() {
		Object firstValue = new Object();

		// Test basic put/get
		mapHelper.put("section.subsection.key", firstValue);
		assertEntry("section.subsection.key", firstValue);

		// Test combine
		mapHelper.combine("section.subsection", Map.of("another-key", 3));
		assertEntry("section.subsection", Map.of("key", firstValue, "another-key", 3));
	}
	
	private void assertEntry(String key, Object value) {
		try {
			assertEquals(value, mapHelper.get(key));
		} catch (MissingKeyException ex) {
			fail(ex);
		}
	}

	@Test
	public void getMapAndCombineComments() {
		// Test get map
		Map<String, Object> toInsert = Map.of("somekey", "somevalue", "anotherkey", true);
		mapHelper.put("category.subkey", toInsert);
		assertEntry("category.subkey", toInsert);

		// Test combine with CommentedWrapper
		CommentedWrapper commentedWrapper = new CommentedWrapper(
				List.of("comment1, comment2"),
				Map.of("addedkey", "addedvalue", "moresubsections", Map.of("verynestedboolean", true)));
		mapHelper.combine("category.subkey", commentedWrapper);
		assertEntry("category.subkey",
				new CommentedWrapper(List.of("comment1, comment2"), Map.of(
					"somekey", "somevalue", "anotherkey", true,
					"addedkey", "addedvalue", "moresubsections", Map.of("verynestedboolean", true))));
	}

	@Test
	public void missingMapDuringGet() {
		mapHelper.put("section.subKey.keyTwo", "value");
		assertEntry("section", Map.of("subKey", Map.of("keyTwo", "value")));
		assertThrows(MissingKeyException.class, () -> mapHelper.get("section.nonExistentKey.otherKey"));
	}
	
}
