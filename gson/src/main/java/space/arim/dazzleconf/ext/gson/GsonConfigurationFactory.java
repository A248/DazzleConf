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

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

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
import space.arim.dazzleconf.factory.AbstractConfigurationFactory;

/**
 * A {@link ConfigurationFactory} implementation using Gson to load a json configuration. <br>
 * <br>
 * <b>Note: Relying on the identity of this class is deprecated</b>, for example relying on
 * this class implementing {@code ConfigurationFactory}. In a future major release, the implementation
 * of the factory returned from the {@code create} methods may be refactored.
 * 
 * @author A248
 *
 * @param <C> the configuration class
 */
@SuppressWarnings("deprecation")
public class GsonConfigurationFactory<C> extends AbstractConfigurationFactory<C> {

	private final GsonOptions gsonOptions;
	
	/**
	 * Creates from a configuration class, config options, and {@link GsonOptions}
	 * 
	 * @param configClazz the config class
	 * @param options the config options
	 * @param gsonOptions the gson options
	 * @throws IllegalArgumentException if {@code configClazz} is not an interface
	 * @throws IllDefinedConfigException if a configuration entry in {@code configClazz} is not defined properly
	 * @deprecated Use {@link #create(Class, ConfigurationOptions, GsonOptions)}. Subclassing should not
	 * be used; prefer the delegation pattern instead. See the class javadoc for more info.
	 */
	@Deprecated
	public GsonConfigurationFactory(Class<C> configClazz, ConfigurationOptions options, GsonOptions gsonOptions) {
		super(configClazz, options);
		this.gsonOptions = Objects.requireNonNull(gsonOptions, "gsonOptions");
	}
	
	/**
	 * Creates from a configuration class and config options, using the default {@link GsonOptions} (including
	 * pretty printing and UTF 8)
	 * 
	 * @param configClazz the config class
	 * @param options the config options
	 * @throws IllegalArgumentException if {@code configClazz} is not an interface
	 * @throws IllDefinedConfigException if a configuration entry in {@code configClazz} is not defined properly
	 * @deprecated Use {@link #create(Class, ConfigurationOptions)}. Subclassing should not be used;
	 * prefer the delegation pattern instead. See the class javadoc for more info.
	 */
	@Deprecated
	public GsonConfigurationFactory(Class<C> configClazz, ConfigurationOptions options) {
		this(configClazz, options, new GsonOptions.Builder().build());
	}

	/**
	 * Creates from a configuration class, config options, and {@link GsonOptions}
	 *
	 * @param configClass the config class
	 * @param options the config options
	 * @param gsonOptions the gson options
	 * @param <C> the configuration type
	 * @return the configuration factory
	 * @throws IllegalArgumentException if {@code configClass} is not an interface
	 * @throws IllDefinedConfigException if a configuration entry in {@code configClass} is not defined properly
	 */
	public static <C> ConfigurationFactory<C> create(Class<C> configClass, ConfigurationOptions options,
													 GsonOptions gsonOptions) {
		return new GsonConfigurationFactory<>(configClass, options, gsonOptions);
	}

	/**
	 * Creates from a configuration class and config options. <br>
	 * <br>
	 * Uses the default gson options, which include pretty printing, UTF 8, and disabled html escaping.
	 *
	 * @param configClass the config class
	 * @param options the config options
	 * @param <C> the configuration type
	 * @return the configuration factory
	 * @throws IllegalArgumentException if {@code configClass} is not an interface
	 * @throws IllDefinedConfigException if a configuration entry in {@code configClass} is not defined properly
	 */
	public static <C> ConfigurationFactory<C> create(Class<C> configClass, ConfigurationOptions options) {
		return create(configClass, options, new GsonOptions.Builder().build());
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
		} catch (EOFException ex) {
			// JsonReader throws EOFException on empty documents
			return Collections.emptyMap();
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
	protected String pseudoCommentsSuffix() {
		return gsonOptions.pseudoCommentsSuffix();
	}

}
