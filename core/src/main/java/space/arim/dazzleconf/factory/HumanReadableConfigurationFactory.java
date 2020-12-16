/*
 * DazzleConf
 * Copyright © 2020 Anand Beh
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

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.InvalidConfigException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Extension of {@link ConfigurationFormatFactory} which turns {@code ReadableByteChannel} and
 * {@code InputStream} into {@code Reader} and {@code WritableByteChannel} and {@code OutputStream}
 * into {@code Writer}. Performs buffering as well.
 *
 * @param <C> the type of the configuration
 */
public abstract class HumanReadableConfigurationFactory<C> extends ConfigurationFormatFactory<C> {

	/**
	 * Creates from a config class and config options
	 *
	 * @param configClass the config class
	 * @param options     configuration options
	 * @throws NullPointerException      if {@code configClass} or {@code options} is null
	 * @throws IllegalArgumentException  if {@code configClass} is not an interface
	 * @throws IllDefinedConfigException if the configuration entries defined in the config class are invalid
	 */
	protected HumanReadableConfigurationFactory(Class<C> configClass, ConfigurationOptions options) {
		super(configClass, options);
	}

	/**
	 * Gets the charset which will be used to create readers and writers
	 *
	 * @return the charset
	 */
	public abstract Charset charset();

	/*
	 * Reading
	 */

	@Override
	public final Map<String, Object> loadMap(ReadableByteChannel readChannel)
			throws IOException, InvalidConfigException {
		return bufferedLoadMap(Channels.newReader(readChannel, charset().newDecoder(), -1));
	}

	@Override
	public final Map<String, Object> loadMap(InputStream inputStream)
			throws IOException, InvalidConfigException {
		return bufferedLoadMap(new InputStreamReader(inputStream, charset()));
	}

	private Map<String, Object> bufferedLoadMap(Reader reader) throws IOException, InvalidConfigException {
		try (Reader unbuffered = reader; BufferedReader buffered = new BufferedReader(unbuffered)) {
			return loadMap(buffered);
		}
	}

	/**
	 * Loads a map of config values from a reader
	 *
	 * @param reader the reader
	 * @return the hierarchical configuration map
	 * @throws IOException if an I/O error occurs
	 * @throws InvalidConfigException if the configuration format dictates the data is not valid syntax.
	 * 	 *                                Usually {@code ConfigFormatSyntaxException}
	 */
	public abstract Map<String, Object> loadMap(Reader reader)
			throws IOException, InvalidConfigException;

	/*
	 * Writing
	 */

	@Override
	public final void writeMap(Map<String, Object> config, WritableByteChannel writeChannel)
			throws IOException {
		bufferedWriteMap(config, Channels.newWriter(writeChannel, charset().newEncoder(), -1));
	}

	@Override
	public final void writeMap(Map<String, Object> config, OutputStream outputStream)
			throws IOException {
		bufferedWriteMap(config, new OutputStreamWriter(outputStream, charset()));
	}

	private void bufferedWriteMap(Map<String, Object> config, Writer writer) throws IOException {
		try (Writer unbuffered = writer; BufferedWriter buffered = new BufferedWriter(unbuffered)) {
			writeMap(config, buffered);
		}
	}

	/**
	 * Writes a map of config values to a writer
	 *
	 * @param config the hierarchical configuration map
	 * @param writer the writer
	 * @throws IOException if an I/O error occurs
	 */
	public abstract void writeMap(Map<String, Object> config, Writer writer) throws IOException;

}
