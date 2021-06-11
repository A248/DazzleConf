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

/**
 * A factory for test purposes which, instead of loading from input stream, returns
 * the same data consistently. <br>
 * <br>
 * Does not implement writing
 *
 * @param <C> the config class
 */
public class FixedLoaderFactory<C> extends ConfigurationFormatFactory<C> {

	private final Map<String, Object> map;

	public FixedLoaderFactory(Class<C> configClass, ConfigurationOptions options, Map<String, Object> map) {
		super(configClass, options);
		this.map = Map.copyOf(map);
	}

	@Override
	public Map<String, Object> loadMap(ReadableByteChannel readChannel) {
		return map;
	}

	@Override
	public Map<String, Object> loadMap(InputStream inputStream) {
		return map;
	}

	@Override
	public void writeMap(Map<String, Object> config, WritableByteChannel writeChannel) {
		throw Assertions.<RuntimeException>fail("Not implemented");
	}

	@Override
	public void writeMap(Map<String, Object> config, OutputStream outputStream) {
		throw Assertions.<RuntimeException>fail("Not implemented");
	}
}
