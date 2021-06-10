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

package space.arim.dazzleconf.serialiser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.factory.FixedLoaderFactory;
import space.arim.dazzleconf.internal.type.CollectionKind;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FlexibleTypeTest {

	private void runTest(ConfigurationOptions options, Object value) {
		var factory = new FixedLoaderFactory<>(Config.class, options, Map.of("value", value));
		try {
			factory.load(InputStream.nullInputStream());
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (InvalidConfigException ex) {
			throw Assertions.<RuntimeException>fail(ex);
		}
	}

	private Serializer createSerializer(FlexibleTypeFunction<FlexibleType> assertion) {
		return new Serializer() {

			@Override
			public TheType deserialise(FlexibleType flexibleType) throws BadValueException {
				assertion.getResult(flexibleType);
				return new TheType();
			}
		};
	}

	@Test
	public void getAssociatedKey() {
		Serializer serializer = createSerializer((flexibleType) -> {
			assertEquals("value", flexibleType.getAssociatedKey());
			return null;
		});
		runTest(new ConfigurationOptions.Builder().addSerialiser(serializer).build(), "val");
	}

	@TestFactory
	public Stream<DynamicNode> getXCollection() {
		return Arrays.stream(CollectionKind.values())
				.flatMap((kind) -> Stream.of(
						new GetCollectionTest(kind, false, false),
						new GetCollectionTest(kind, false, true),
						new GetCollectionTest(kind, true, false),
						new GetCollectionTest(kind, true, true)))
				.map((test) -> DynamicTest.dynamicTest("Running " + test, () -> runGetXCollection(test)));
	}

	record GetCollectionTest(CollectionKind kind, boolean createSingleElementCollections,
							 boolean singleElementIsValue) { }

	private void runGetXCollection(GetCollectionTest testInfo) {
		String expectedElement = "element value";
		Collection<String> expectedValue;
		FlexibleTypeFunction<Collection<String>> getCollectionDirect;
		FlexibleTypeFunction<Collection<FlexibleType>> getCollectionIndirect;
		Collector<String, ?, ? extends Collection<String>> collector;
		switch (testInfo.kind()) {
		case LIST -> {
			expectedValue = List.of(expectedElement);
			getCollectionDirect = (flexibleType) -> flexibleType.getList(FlexibleType::getString);
			getCollectionIndirect = FlexibleType::getList;
			collector = Collectors.toUnmodifiableList();
		}
		case SET -> {
			expectedValue = Set.of(expectedElement);
			getCollectionDirect = (flexibleType) -> flexibleType.getSet(FlexibleType::getString);
			getCollectionIndirect = FlexibleType::getSet;
			collector = Collectors.toUnmodifiableSet();
		}
		case COLLECTION -> {
			expectedValue = Set.of(expectedElement);
			getCollectionDirect = (flexibleType) -> flexibleType.getCollection(FlexibleType::getString);
			getCollectionIndirect = FlexibleType::getCollection;
			collector = Collectors.toUnmodifiableSet();
		}
		default -> throw new IllegalArgumentException();
		}
		Serializer serializer = new Serializer() {

			@Override
			public TheType deserialise(FlexibleType flexibleType) throws BadValueException {

				if (testInfo.singleElementIsValue() && !testInfo.createSingleElementCollections()) {
					// In this case, failure is expected
					assertThrows(BadValueException.class, () -> getCollectionDirect.getResult(flexibleType));
					assertThrows(BadValueException.class, () -> getCollectionIndirect.getResult(flexibleType));
					return new TheType();
				}
				assertEquals(expectedValue, getCollectionDirect.getResult(flexibleType));
				Collection<String> collected = getCollectionIndirect.getResult(flexibleType).stream()
						.map((flexibleElement) -> {
							try {
								return flexibleElement.getString();
							} catch (BadValueException ex) {
								throw Assertions.<RuntimeException>fail(ex);
							}
						}).collect(collector);
				assertEquals(expectedValue, collected);
				return new TheType();
			}
		};
		ConfigurationOptions options = new ConfigurationOptions.Builder()
				.addSerialiser(serializer)
				.setCreateSingleElementCollections(testInfo.createSingleElementCollections())
				.build();
		Object representation = (testInfo.singleElementIsValue()) ? expectedElement : expectedValue;
		runTest(options, representation);
	}

}
