/* 
 * DazzleConf-gson
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-gson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-gson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-gson. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.ext.gson;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.internal.AbstractConfigurationFactoryImpl;
import space.arim.dazzleconf.internal.deprocessor.AddCommentStringBeforeDeprocessor;
import space.arim.dazzleconf.internal.deprocessor.MapDeprocessor;

/**
 * A {@link ConfigurationFactory} implementation using Gson to load a json configuration
 * 
 * @author A248
 *
 * @param <C> the configuration class
 */
public final class GsonConfigurationFactory<C> extends AbstractConfigurationFactoryImpl<C> {

	private final GsonOptions gsonOptions;
	
	/**
	 * Creates from a configuration class, config options, and {@link GsonOptions}
	 * 
	 * @param configClazz the config class
	 * @param options the config options
	 * @param gsonOptions the gson options
	 * @throws NullPointerException if {@code configClazz} or {@code options} is null
	 * @throws IllegalArgumentException if {@code configClazz} is not an interface
	 * @throws IllDefinedConfigException if a configuration entry in {@code configClazz} is not defined properly
	 */
	public GsonConfigurationFactory(Class<C> configClazz, ConfigurationOptions options, GsonOptions gsonOptions) {
		super(configClazz, options);
		this.gsonOptions = gsonOptions;
	}
	
	/**
	 * Creates from a configuration class and config options, using the default {@link GsonOptions} (including
	 * pretty printing and UTF 8)
	 * 
	 * @param configClazz the config class
	 * @param options the config options
	 * @throws NullPointerException if {@code configClazz} or {@code options} is null
	 * @throws IllegalArgumentException if {@code configClazz} is not an interface
	 * @throws IllDefinedConfigException if a configuration entry in {@code configClazz} is not defined properly
	 */
	public GsonConfigurationFactory(Class<C> configClazz, ConfigurationOptions options) {
		this(configClazz, options, new GsonOptions.Builder().build());
	}
	
	@Override
	protected Charset charset() {
		return gsonOptions.charset();
	}

	@Override
	protected Map<String, Object> loadMapFromReader(Reader reader) throws IOException, ConfigFormatSyntaxException {
		Gson gson = gsonOptions.gson();
		TypeAdapter<Map<String, Object>> adapter = gson.getAdapter(new TypeToken<Map<String, Object>>() {});
		JsonReader jsonReader = gson.newJsonReader(reader);
		try {
			return adapter.read(jsonReader);
		} catch (JsonIOException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			}
			throw new IOException(ex);
		} catch (JsonSyntaxException | MalformedJsonException ex) {
			throw new ConfigFormatSyntaxException(ex);
		}
	}

	@Override
	protected void writeMapToWriter(Map<String, Object> rawMap, Writer writer) throws IOException {
		try {
			gsonOptions.gson().toJson(rawMap, writer);
		} catch (JsonIOException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			}
			throw new IOException(ex);
		}
	}
	
	@Override
	protected MapDeprocessor<C> createMapDeprocessor(C configData) {
		if (gsonOptions.pseudoComments()) {
			return new AddCommentStringBeforeDeprocessor<>(getOptions(), getDefinition(), configData);
		}
		return super.createMapDeprocessor(configData);
	}

}
