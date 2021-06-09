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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.factory.FixedLoaderFactory;
import space.arim.dazzleconf.factory.MapReceiver;
import space.arim.dazzleconf.factory.TransparentWriterFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class SerializerReturnsNullTest {

	private ConfigurationOptions optionsWith(Serializer serializer) {
		return new ConfigurationOptions.Builder().addSerialiser(serializer).build();
	}

	@Test
	public void deserialiseReturnsNull() {
		Serializer serializer = new Serializer() {

			@Override
			public TheType deserialise(FlexibleType flexibleType) {
				wasCalled = true;
				return null;
			}
		};
		ConfigurationOptions options = optionsWith(serializer);
		ConfigurationFactory<Config> factory = new FixedLoaderFactory<>(Config.class, options, Map.of("value", "val"));
		assertThrows(IllDefinedConfigException.class, () -> factory.load(InputStream.nullInputStream()));
		assertTrue(serializer.wasCalled);
	}

	@Test
	public void serialiseReturnsNull(@Mock MapReceiver mapReceiver) {
		Serializer serializer = new Serializer() {

			@Override
			public TheType serialise(TheType value, Decomposer decomposer) {
				wasCalled = true;
				return null;
			}
		};
		ConfigurationOptions options = optionsWith(serializer);
		Config config = TheType::new;
		ConfigurationFactory<Config> factory = new TransparentWriterFactory<>(Config.class, options, mapReceiver);
		assertThrows(IllDefinedConfigException.class, () -> factory.write(config, OutputStream.nullOutputStream()));
		assertTrue(serializer.wasCalled);
		verifyNoMoreInteractions(mapReceiver);
	}
}
