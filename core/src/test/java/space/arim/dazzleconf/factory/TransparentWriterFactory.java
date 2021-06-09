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

package space.arim.dazzleconf.factory;

import org.junit.jupiter.api.Assertions;
import space.arim.dazzleconf.ConfigurationOptions;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.Objects;

public class TransparentWriterFactory<C> extends ConfigurationFormatFactory<C> {

	private final MapReceiver mapReceiver;

	public TransparentWriterFactory(Class<C> configClass, ConfigurationOptions options,
									MapReceiver mapReceiver) {
		super(configClass, options);
		this.mapReceiver = Objects.requireNonNull(mapReceiver, "mapReceiver");
	}

	private static UnsupportedOperationException notImplemented() {
		return Assertions.fail("Not implemented");
	}

	@Override
	public Map<String, Object> loadMap(ReadableByteChannel readChannel) {
		throw notImplemented();
	}

	@Override
	public Map<String, Object> loadMap(InputStream inputStream) {
		throw notImplemented();
	}

	@Override
	public void writeMap(Map<String, Object> config, WritableByteChannel writeChannel) {
		mapReceiver.writeMap(config);
	}

	@Override
	public void writeMap(Map<String, Object> config, OutputStream outputStream) {
		mapReceiver.writeMap(config);
	}

}
