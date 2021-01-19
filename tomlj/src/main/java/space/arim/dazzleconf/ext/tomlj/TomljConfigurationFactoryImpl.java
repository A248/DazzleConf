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

package space.arim.dazzleconf.ext.tomlj;

import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseError;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.factory.HumanReadableConfigurationFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class TomljConfigurationFactoryImpl<C> extends HumanReadableConfigurationFactory<C> {

	private final TomljOptions tomlOptions;

	TomljConfigurationFactoryImpl(Class<C> configClass, ConfigurationOptions options, TomljOptions tomlOptions) {
		super(configClass, options);
		this.tomlOptions = tomlOptions;
	}

	@Override
	public Charset charset() {
		return tomlOptions.charset();
	}

	@Override
	public Map<String, Object> loadMap(Reader reader) throws IOException, InvalidConfigException {
		TomlParseResult parseResult = Toml.parse(reader, tomlOptions.tomlVersion());
		if (parseResult.hasErrors()) {
			// Take the first exception; add the rest as suppressed
			Iterator<TomlParseError> errorIterator = new ArrayList<>(parseResult.errors()).iterator();
			TomlParseError firstEx = errorIterator.next();
			errorIterator.remove();
			errorIterator.forEachRemaining(firstEx::addSuppressed);

			throw new ConfigFormatSyntaxException(
					"Toml parsing error at " + firstEx.position(), firstEx);
		}
		return unwrapTomlMap(parseResult.toMap());
	}

	/*
	 * Both TomlTable#toMap and TomlArray#toList do not perform a "deep conversion".
	 * Rather, they can return further TomlTables and TomlArrays
	 */

	private Map<String, Object> unwrapTomlMap(Map<String, Object> tomlMap) {
		LinkedHashMap<String, Object> configMap = new LinkedHashMap<>(tomlMap);
		for (Map.Entry<String, Object> entry : tomlMap.entrySet()) {
			entry.setValue(unwrapTomlObject(entry.getValue()));
		}
		return configMap;
	}

	private List<Object> unwrapTomlList(List<Object> tomlList) {
		List<Object> configList = new ArrayList<>(tomlList.size());
		for (Object tomlValue : tomlList) {
			configList.add(unwrapTomlObject(tomlValue));
		}
		return configList;
	}

	private Object unwrapTomlObject(Object tomlValue) {
		if (tomlValue instanceof TomlTable) {
			return unwrapTomlMap(((TomlTable) tomlValue).toMap());
		} else if (tomlValue instanceof TomlArray) {
			return unwrapTomlList(((TomlArray) tomlValue).toList());
		}
		return tomlValue;
	}

	@Override
	public void writeMap(Map<String, Object> config, Writer writer) throws IOException {
		throw new UnsupportedOperationException("Writing toml not yet supported - see #12");
	}
}
