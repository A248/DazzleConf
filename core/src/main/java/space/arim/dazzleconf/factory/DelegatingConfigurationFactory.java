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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;

abstract class DelegatingConfigurationFactory<C> implements ConfigurationFactory<C> {

	abstract ConfigurationFactory<C> delegate();

	@Override
	public Class<C> getConfigClass() {
		return delegate().getConfigClass();
	}

	@Override
	public ConfigurationOptions getOptions() {
		return delegate().getOptions();
	}

	@Override
	public C load(ReadableByteChannel readChannel) throws IOException, InvalidConfigException {
		return delegate().load(readChannel);
	}

	@Override
	public C load(InputStream inputStream) throws IOException, InvalidConfigException {
		return delegate().load(inputStream);
	}

	@Override
	public C loadDefaults() {
		return delegate().loadDefaults();
	}

	@Override
	public void write(C configData, WritableByteChannel writableChannel) throws IOException {
		delegate().write(configData, writableChannel);
	}

	@Override
	public void write(C configData, OutputStream outputStream) throws IOException {
		delegate().write(configData, outputStream);
	}
	
}
