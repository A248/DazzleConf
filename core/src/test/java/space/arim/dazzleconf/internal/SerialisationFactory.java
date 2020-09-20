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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Map;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.InvalidConfigException;

public class SerialisationFactory<C> extends AbstractConfigurationFactoryImpl<C> {

	public SerialisationFactory(Class<C> configClazz, ConfigurationOptions options) {
		super(configClazz, options);
	}
	
	@Override
	public C load(ReadableByteChannel readChannel) throws IOException, InvalidConfigException {
		try (InputStream inputStream = Channels.newInputStream(readChannel)) {
			return load(inputStream);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public C load(InputStream inputStream) throws IOException, InvalidConfigException {
		try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
			return fromRawMap((Map<String, Object>) ois.readObject());
		} catch (ClassNotFoundException ex) {
			throw new IOException(ex);
		}
	}
	
	@Override
	public void write(C configData, WritableByteChannel writableChannel) throws IOException {
		try (OutputStream outputStream = Channels.newOutputStream(writableChannel)) {
			write(configData, outputStream);
		}
	}

	@Override
	public void write(C configData, OutputStream outputStream) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
			oos.writeObject(toRawMap(configData));
		}
	}

	@Override
	protected void writeMapToWriter(Map<String, Object> config, Writer writer) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Map<String, Object> loadMapFromReader(Reader reader) throws IOException, ConfigFormatSyntaxException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Charset charset() {
		throw new UnsupportedOperationException();
	}

}
