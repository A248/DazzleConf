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

package space.arim.dazzleconf.internal.type;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

public class EqualsTest {

	@TestFactory
	public Stream<DynamicNode> testEquals() {
		return Stream.of(
				SimpleCollectionReturnType.class, SimpleMapReturnType.class, SimpleSubSectionReturnType.class,
				SimpleTypeReturnType.class, SubSectionCollectionReturnType.class, SubSectionMapReturnType.class,
				TypeInfo.class)
				.map((type) -> DynamicTest.dynamicTest("Testing equals for " + type, () -> runTestEquals(type)));
	}

	private void runTestEquals(Class<?> type) {
		EqualsVerifier.forClass(type).suppress(Warning.NULL_FIELDS)
				.withPrefabValues(TypeInfo.class, typeInfo(Object.class), typeInfo(String.class))
				.verify();
	}

	private <T> TypeInfo<T> typeInfo(Class<T> type) {
		return new TypeInfo<>(type, List.of(), List.of());
	}

}
