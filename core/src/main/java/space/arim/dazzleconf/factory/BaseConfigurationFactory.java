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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.internal.BaseConfigurationFactoryImpl;

/**
 * Base implementation of of {@link ConfigurationFactory}. Eliminates some basic boilerplate
 * associated with IO operations.
 * 
 * @author A248
 *
 * @param <C> the type of the configuration
 */
public abstract class BaseConfigurationFactory<C> extends BaseConfigurationFactoryImpl<C> {

	/**
	 * Creates from a config class and config options
	 * 
	 * @param configClass the config class
	 * @param options configuration options
	 * @throws NullPointerException if {@code configClass} or {@code options} is null
	 * @throws IllegalArgumentException if {@code configClass} is not an interface
	 */
	protected BaseConfigurationFactory(Class<C> configClass, ConfigurationOptions options) {
		super(configClass, options);
	}
	
	/**
	 * The charset used by this factory
	 * 
	 * @return the charset to use
	 */
	@Override
	protected abstract Charset charset();
	
	/**
	 * Reads config data from the specified reader
	 * 
	 * @return the config data
	 * @throws IOException if an I/O error occurs
	 * @throws InvalidConfigException if the configuration is not valid
	 */
	@Override
	protected abstract C loadFromReader(Reader reader) throws IOException, InvalidConfigException;
	
	/**
	 * Writes config data to the specified writer
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected abstract void writeToWriter(C configData, Writer writer) throws IOException;
	
}
