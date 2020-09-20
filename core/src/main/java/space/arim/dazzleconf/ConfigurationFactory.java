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
package space.arim.dazzleconf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.InvalidConfigException;

/**
 * Loader and writer of configuration data
 * 
 * @author A248
 *
 * @param <C> the type of the configuration
 */
public interface ConfigurationFactory<C> {

	/**
	 * Gets the configuration class this factory is for
	 * 
	 * @return the configuration class
	 */
	Class<C> getConfigClass();
	
	/**
	 * Gets the configuration options this factory uses
	 * 
	 * @return the configuration options
	 */
	ConfigurationOptions getOptions();
	
	/**
	 * Reads configuration data from a readable channel
	 * 
	 * @param readChannel the channel from which to read the data
	 * @return the read config data
	 * @throws IOException if an I/O error occurs
	 * @throws InvalidConfigException if the configuration is invalid. Where possible, more specific subclasses are thrown
	 * @throws NullPointerException if {@code readChannel} is null
	 */
	C load(ReadableByteChannel readChannel) throws IOException, InvalidConfigException;
	
	/**
	 * Reads configuration data from an input stream
	 * 
	 * @param inputStream the stream from which to read the data
	 * @return the read config data
	 * @throws IOException if an I/O error occurs
	 * @throws InvalidConfigException if the configuration is invalid. Where possible, more specific subclasses are thrown
	 * @throws NullPointerException if {@code inputStream} is null
	 */
	C load(InputStream inputStream) throws IOException, InvalidConfigException;
	
	/**
	 * Reads the default configuration data from annotations in {@link ConfDefault} <br>
	 * <br>
	 * If any config entry is missing a default value annotation, or the default values cannot be deserialised
	 * to the appropriate config value, {@link IllDefinedConfigException} is thrown
	 * 
	 * @return the read config data
	 * @throws IllDefinedConfigException if the defaults are not defined or ill defined
	 */
	C loadDefaults();
	
	/**
	 * Writes configuration data to a writable byte channel
	 * 
	 * @param configData the configuration data
	 * @param writableChannel the channel to which to write the data
	 * @throws IOException if an I/O error occurs
	 * @throws NullPointerException if either parameter is null
	 */
	void write(C configData, WritableByteChannel writableChannel) throws IOException;
	
	/**
	 * Writes configuration data to an output stream
	 * 
	 * @param configData the configuration data
	 * @param outputStream the stream to which to write the data
	 * @throws IOException if an I/O error occurs
	 * @throws NullPointerException if either parameter is null
	 */
	void write(C configData, OutputStream outputStream) throws IOException;
	
}
