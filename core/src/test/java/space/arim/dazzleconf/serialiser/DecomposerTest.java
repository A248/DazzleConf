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
import space.arim.dazzleconf.annote.SubSection;
import space.arim.dazzleconf.factory.MapReceiver;
import space.arim.dazzleconf.factory.TransparentWriterFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DecomposerTest {

	private final MapReceiver mapReceiver;

	public DecomposerTest(@Mock MapReceiver mapReceiver) {
		this.mapReceiver = mapReceiver;
	}

	private <C> ConfigurationFactory<C> factoryFor(Class<C> config, Serializer serializer) {
		ConfigurationOptions options = new ConfigurationOptions.Builder().addSerialiser(serializer).build();
		return new TransparentWriterFactory<>(config, options, mapReceiver);
	}

	@Test
	public void decomposeSectionCollection() throws IOException {
		Serializer serializer = new Serializer() {
			@Override
			public Object serialise(TheType value, Decomposer decomposer) {
				wasCalled = true;
				return value.toString();
			}
		};
		TheType elem1 = new TheType();
		TheType elem2 = new TheType();
		CollectionConfig config = () -> List.of(Config.withValue(elem1), Config.withValue(elem2));
		factoryFor(CollectionConfig.class, serializer).write(config, OutputStream.nullOutputStream());
		verify(mapReceiver).writeMap(
				Map.of("sections",
						List.of(Map.of("value", elem1.toString()), Map.of("value", elem2.toString()))));
		assertTrue(serializer.wasCalled);
	}

	@Test
	public void decomposeSectionMap() throws IOException {
		Serializer serializer = new Serializer() {
			@Override
			public Object serialise(TheType value, Decomposer decomposer) {
				wasCalled = true;
				return value.toString();
			}
		};
		TheType elem1 = new TheType();
		TheType elem2 = new TheType();
		MapConfig config = () -> Map.of("k1", Config.withValue(elem1), "k2", Config.withValue(elem2));
		factoryFor(MapConfig.class, serializer).write(config, OutputStream.nullOutputStream());
		verify(mapReceiver).writeMap(
				Map.of("sections",
						Map.of("k1", Map.of("value", elem1.toString()), "k2", Map.of("value", elem2.toString()))));
		assertTrue(serializer.wasCalled);
	}

	public interface CollectionConfig {

		List<@SubSection Config> sections();
	}

	public interface MapConfig {

		Map<String, @SubSection Config> sections();
	}
}
