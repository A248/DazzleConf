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

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.factory.FixedLoaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParsePrimitivesTest {

	private ConfigurationFactory<Config> factoryFrom(Serializer serializer, Map<String, Object> source) {
		ConfigurationOptions options = new ConfigurationOptions.Builder().addSerialiser(serializer).build();
		return new FixedLoaderFactory<>(Config.class, options, source);
	}

	private <T> void assertSuccess(T value, Object representation, FlexibleTypeFunction<T> function,
								   Class<T> boxedType, Class<T> primitiveType)
			throws IOException, InvalidConfigException {
		Serializer serializer = new Serializer() {

			@Override
			public TheType deserialise(FlexibleType flexibleType) throws BadValueException {
				wasCalled = true;
				assertEquals(value, function.getResult(flexibleType));
				assertEquals(value, flexibleType.getObject(boxedType));
				assertEquals(value, flexibleType.getObject(primitiveType));
				return new TheType();
			}
		};
		factoryFrom(serializer, Map.of("value", representation)).load(InputStream.nullInputStream());
		assertTrue(serializer.wasCalled);
	}

	private <T> void assertFailure(Object representation, FlexibleTypeFunction<T> function,
								   Class<T> boxedType, Class<T> primitiveType)
			throws IOException, InvalidConfigException {
		Serializer serializer = new Serializer() {

			@Override
			public TheType deserialise(FlexibleType flexibleType) {
				wasCalled = true;
				assertThrows(BadValueException.class, () -> function.getResult(flexibleType));
				assertThrows(BadValueException.class, () -> flexibleType.getObject(boxedType));
				assertThrows(BadValueException.class, () -> flexibleType.getObject(primitiveType));
				return new TheType();
			}
		};
		factoryFrom(serializer, Map.of("value", representation)).load(InputStream.nullInputStream());
		assertTrue(serializer.wasCalled);
	}

	// Bytes

	@Test
	public void simpleByte() throws IOException, InvalidConfigException {
		assertSuccess((byte) 1, (byte) 1, FlexibleType::getByte, Byte.class, byte.class);
	}

	@Test
	public void stringByte() throws IOException, InvalidConfigException {
		assertSuccess((byte) 1, Byte.toString((byte) 1), FlexibleType::getByte, Byte.class, byte.class);
	}

	@Test
	public void notAStringByte() throws IOException, InvalidConfigException {
		assertFailure("not a byte", FlexibleType::getByte, Byte.class, byte.class);
	}

	@Test
	public void notAByte() throws IOException, InvalidConfigException {
		assertFailure(new OtherType(), FlexibleType::getByte, Byte.class, byte.class);
	}

	// Shorts

	@Test
	public void simpleShort() throws IOException, InvalidConfigException {
		assertSuccess((short) 1, (short) 1, FlexibleType::getShort, Short.class, short.class);
	}

	@Test
	public void stringShort() throws IOException, InvalidConfigException {
		assertSuccess((short) 1, Short.toString((short) 1), FlexibleType::getShort, Short.class, short.class);
	}

	@Test
	public void notAStringShort() throws IOException, InvalidConfigException {
		assertFailure("not a short", FlexibleType::getShort, Short.class, short.class);
	}

	@Test
	public void notAShort() throws IOException, InvalidConfigException {
		assertFailure(new OtherType(), FlexibleType::getShort, Short.class, short.class);
	}

	// Integers

	@Test
	public void simpleInteger() throws IOException, InvalidConfigException {
		assertSuccess(1, 1, FlexibleType::getInteger, Integer.class, int.class);
	}

	@Test
	public void stringInteger() throws IOException, InvalidConfigException {
		assertSuccess(1, Integer.toString(1), FlexibleType::getInteger, Integer.class, int.class);
	}

	@Test
	public void notAStringInteger() throws IOException, InvalidConfigException {
		assertFailure("not an integer", FlexibleType::getInteger, Integer.class, int.class);
	}

	@Test
	public void notAnInteger() throws IOException, InvalidConfigException {
		assertFailure(new OtherType(), FlexibleType::getInteger, Integer.class, int.class);
	}

	// Longs

	@Test
	public void simpleLong() throws IOException, InvalidConfigException {
		assertSuccess(1L, 1L, FlexibleType::getLong, Long.class, long.class);
	}

	@Test
	public void stringLong() throws IOException, InvalidConfigException {
		assertSuccess(1L, Long.toString(1L), FlexibleType::getLong, Long.class, long.class);
	}

	@Test
	public void notAStringLong() throws IOException, InvalidConfigException {
		assertFailure("not a long", FlexibleType::getLong, Long.class, long.class);
	}

	@Test
	public void notALong() throws IOException, InvalidConfigException {
		assertFailure(new OtherType(), FlexibleType::getLong, Long.class, long.class);
	}

	// Floats

	@Test
	public void simpleFloat() throws IOException, InvalidConfigException {
		assertSuccess(1F, 1F, FlexibleType::getFloat, Float.class, float.class);
	}

	@Test
	public void stringFloat() throws IOException, InvalidConfigException {
		assertSuccess(1F, Float.toString(1F), FlexibleType::getFloat, Float.class, float.class);
	}

	@Test
	public void notAStringFloat() throws IOException, InvalidConfigException {
		assertFailure("not a float", FlexibleType::getFloat, Float.class, float.class);
	}

	@Test
	public void notAFloat() throws IOException, InvalidConfigException {
		assertFailure(new OtherType(), FlexibleType::getFloat, Float.class, float.class);
	}

	// Doubles

	@Test
	public void simpleDouble() throws IOException, InvalidConfigException {
		assertSuccess(1D, 1D, FlexibleType::getDouble, Double.class, double.class);
	}

	@Test
	public void stringDouble() throws IOException, InvalidConfigException {
		assertSuccess(1D, Double.toString(1D), FlexibleType::getDouble, Double.class, double.class);
	}

	@Test
	public void notAStringDouble() throws IOException, InvalidConfigException {
		assertFailure("not a double", FlexibleType::getDouble, Double.class, double.class);
	}

	@Test
	public void notADouble() throws IOException, InvalidConfigException {
		assertFailure(new OtherType(), FlexibleType::getDouble, Double.class, double.class);
	}

	// Character

	@Test
	public void simpleCharacter() throws IOException, InvalidConfigException {
		assertSuccess('a', 'a', FlexibleType::getCharacter, Character.class, char.class);
	}

	@Test
	public void stringCharacter() throws IOException, InvalidConfigException {
		assertSuccess('a', "a", FlexibleType::getCharacter, Character.class, char.class);
	}

	@Test
	public void notACharacter() throws IOException, InvalidConfigException {
		assertFailure("not a character", FlexibleType::getCharacter, Character.class, char.class);
	}

	@Test
	public void notACharacterOrEvenAString() throws IOException, InvalidConfigException {
		assertFailure(new OtherType(), FlexibleType::getCharacter, Character.class, char.class);
	}

}
