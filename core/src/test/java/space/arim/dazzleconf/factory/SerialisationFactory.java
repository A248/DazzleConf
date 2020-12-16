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
package space.arim.dazzleconf.factory;

import space.arim.dazzleconf.ConfigurationOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

public class SerialisationFactory<C> extends ConfigurationFormatFactory<C> {

	public SerialisationFactory(Class<C> configClazz, ConfigurationOptions options) {
		super(configClazz, options);
	}

	@Override
	public Map<String, Object> loadMap(ReadableByteChannel readChannel) throws IOException {
		try (InputStream inputStream = Channels.newInputStream(readChannel)) {
			return loadMap(inputStream);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> loadMap(InputStream inputStream) throws IOException {
		try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
			return (Map<String, Object>) ois.readObject();
		} catch (ClassNotFoundException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void writeMap(Map<String, Object> config, WritableByteChannel writeChannel) throws IOException {
		try (OutputStream outputStream = Channels.newOutputStream(writeChannel)) {
			writeMap(config, outputStream);
		}
	}

	@Override
	public void writeMap(Map<String, Object> config, OutputStream outputStream) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
			oos.writeObject(config);
		}
	}

}
