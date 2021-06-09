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

package space.arim.dazzleconf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.factory.SerialisationFactory;
import space.arim.dazzleconf.serialiser.Decomposer;
import space.arim.dazzleconf.serialiser.FlexibleType;
import space.arim.dazzleconf.serialiser.ValueSerialiser;

import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NoSuchSerializerTest {

	private ConfigurationFactory<Config> factoryFromOptions(ConfigurationOptions options) {
		return new SerialisationFactory<>(Config.class, options);
	}

	@Test
	public void flexibleType() {
		Serializer serializer = new Serializer() {
			@Override
			public TheType deserialise(FlexibleType flexibleType) throws BadValueException {
				wasCalled = true;
				assertThrows(IllDefinedConfigException.class, () -> flexibleType.getObject(OtherClass.class));
				assertEquals("value", flexibleType.getString());
				return new TheType();
			}
		};
		ConfigurationOptions options = new ConfigurationOptions.Builder().addSerialiser(serializer).build();
		factoryFromOptions(options).loadDefaults();
		assertTrue(serializer.wasCalled);
	}

	@Test
	public void decomposer() throws IOException {
		Serializer serializer = new Serializer() {
			@Override
			public Object serialise(TheType value, Decomposer decomposer) {
				wasCalled = true;
				assertThrows(IllDefinedConfigException.class,
						() -> decomposer.decompose(OtherClass.class, new OtherClass()));
				return "value";
			}
		};
		ConfigurationOptions options = new ConfigurationOptions.Builder().addSerialiser(serializer).build();
		Config config = TheType::new;
		factoryFromOptions(options).write(config, OutputStream.nullOutputStream());
		assertTrue(serializer.wasCalled);
	}

	public interface Config {

		@ConfDefault.DefaultString("value")
		TheType value();
	}
	public static class TheType { }
	public static class OtherClass { }

	private static abstract class Serializer implements ValueSerialiser<TheType> {

		boolean wasCalled;

		@Override
		public Class<TheType> getTargetClass() {
			return TheType.class;
		}

		@Override
		public TheType deserialise(FlexibleType flexibleType) throws BadValueException {
			throw Assertions.<RuntimeException>fail("Not implemented");
		}

		@Override
		public Object serialise(TheType value, Decomposer decomposer) {
			throw Assertions.<RuntimeException>fail("Not implemented");
		}
	}

}
