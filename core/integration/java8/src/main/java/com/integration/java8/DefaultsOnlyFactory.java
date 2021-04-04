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
package com.integration.java8;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.factory.HumanReadableConfigurationFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

public class DefaultsOnlyFactory<C> extends HumanReadableConfigurationFactory<C> {

	public DefaultsOnlyFactory(Class<C> configClazz, ConfigurationOptions options) {
		super(configClazz, options);
	}

	@Override
	public Charset charset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> loadMap(Reader reader) throws IOException, ConfigFormatSyntaxException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeMap(Map<String, Object> config, Writer writer) throws IOException {
		throw new UnsupportedOperationException();
	}

}
