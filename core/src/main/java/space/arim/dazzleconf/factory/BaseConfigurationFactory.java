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
import java.util.Objects;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;

/**
 * Basic abstract implementation of of {@link ConfigurationFactory}. Eliminates some basic boilerplate
 * associated with IO operations.
 * 
 * @author A248
 *
 * @param <C> the type of the configuration
 * @deprecated Will be removed without replacement in a later release. Contributes
 * little value on its own.
 */
@Deprecated
public abstract class BaseConfigurationFactory<C> implements ConfigurationFactory<C> {

	private final Class<C> configClass;
	private final ConfigurationOptions options;

	/**
	 * Creates from a config class and config options
	 *
	 * @param configClass the config class
	 * @param options configuration options
	 * @throws NullPointerException if {@code configClass} or {@code options} is null
	 * @throws IllegalArgumentException if {@code configClass} is not an interface
	 */
	protected BaseConfigurationFactory(Class<C> configClass, ConfigurationOptions options) {
		Objects.requireNonNull(configClass, "configClazz");
		if (!configClass.isInterface()) {
			throw new IllegalArgumentException(configClass.getName() + " is not an interface");
		}
		this.configClass = configClass;
		this.options = Objects.requireNonNull(options, "options");
	}

	@Override
	public Class<C> getConfigClass() {
		return configClass;
	}

	@Override
	public ConfigurationOptions getOptions() {
		return options;
	}
	
	/**
	 * The charset used by this factory
	 * 
	 * @return the charset to use
	 */
	protected abstract Charset charset();

	/*
	 * Reading
	 */

	/**
	 * Reads config data from the specified reader
	 * 
	 * @param reader the stream reader
	 * @return the config data
	 * @throws IOException if an I/O error occurs
	 * @throws InvalidConfigException if the configuration is not valid
	 */
	protected abstract C loadFromReader(Reader reader) throws IOException, InvalidConfigException;
	
	/**
	 * Reads config data from the specified reader with the given auxiliary entries. The auxiliary entries
	 * are the same ones passed to {@link ConfigurationFactory}
	 * 
	 * @param reader the stream reader
	 * @param auxiliaryEntries the auxiliary entries
	 * @return the config data
	 * @throws IOException if an I/O error occurs
	 * @throws InvalidConfigException if the configuration is not valid
	 */
	protected abstract C loadFromReader(Reader reader, C auxiliaryEntries) throws IOException, InvalidConfigException;

	private C loadConfig(Reader reader) throws IOException, InvalidConfigException {
		try (Reader reader0 = reader; BufferedReader buffReader = new BufferedReader(reader0)) {

			return loadFromReader(buffReader);
		}
	}

	private C loadConfig(Reader reader, C auxiliaryEntries) throws IOException, InvalidConfigException {
		try (Reader reader0 = reader; BufferedReader buffReader = new BufferedReader(reader0)) {

			return loadFromReader(buffReader, auxiliaryEntries);
		}
	}

	private Reader toReader(ReadableByteChannel readChannel) {
		return Channels.newReader(readChannel, charset().newDecoder(), -1); // Channels.newReader performs null check
	}

	private Reader toReader(InputStream inputStream) {
		return new InputStreamReader(inputStream, charset()); // InputStreamReader performs null check
	}

	@Override
	public C load(ReadableByteChannel readChannel) throws IOException, InvalidConfigException {
		return loadConfig(toReader(readChannel));
	}

	@Override
	public C load(InputStream inputStream) throws IOException, InvalidConfigException {
		return loadConfig(toReader(inputStream));
	}

	@Override
	public C load(ReadableByteChannel readChannel, C auxiliaryEntries) throws IOException, InvalidConfigException {
		configClass.cast(Objects.requireNonNull(auxiliaryEntries, "auxiliaryEntries"));
		return loadConfig(toReader(readChannel), auxiliaryEntries);
	}

	@Override
	public C load(InputStream inputStream, C auxiliaryEntries) throws IOException, InvalidConfigException {
		configClass.cast(Objects.requireNonNull(auxiliaryEntries, "auxiliaryEntries"));
		return loadConfig(toReader(inputStream), auxiliaryEntries);
	}

	/*
	 * Writing
	 */

	/**
	 * Writes config data to the specified writer
	 *
	 * @param configData the configuration data to write
	 * @param writer the stream writer
	 * @throws IOException if an I/O error occurs
	 */
	protected abstract void writeToWriter(C configData, Writer writer) throws IOException;

	private void writeConfig(C configData, Writer writer) throws IOException {
		try (Writer writer0 = writer; BufferedWriter buffWriter = new BufferedWriter(writer0)) {

			writeToWriter(configData, buffWriter);
		}
	}

	@Override
	public void write(C configData, WritableByteChannel writableChannel) throws IOException {
		configClass.cast(Objects.requireNonNull(configData, "configData"));
		writeConfig(configData, Channels.newWriter(writableChannel, charset().newEncoder(), -1)); // Channels.newWriter performs null check
	}

	@Override
	public void write(C configData, OutputStream outputStream) throws IOException {
		configClass.cast(Objects.requireNonNull(configData, "configData"));
		writeConfig(configData, new OutputStreamWriter(outputStream, charset())); // OutputStreamWriter performs null check
	}
	
}
