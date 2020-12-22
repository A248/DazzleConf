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
package space.arim.dazzleconf.ext.snakeyaml;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.yaml.snakeyaml.error.YAMLException;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.factory.AbstractConfigurationFactory;

/**
 * A {@link ConfigurationFactory} implementation using SnakeYAML to load a yaml configuration
 * 
 * @author A248
 *
 * @param <C> the configuration type
 */
@SuppressWarnings("deprecation")
public class SnakeYamlConfigurationFactory<C> extends AbstractConfigurationFactory<C> {

	private final SnakeYamlOptions yamlOptions;
	
	/**
	 * Creates from a configuration class, config options, and {@link SnakeYamlOptions}
	 * 
	 * @param configClazz the config class
	 * @param options the config options
	 * @param yamlOptions the snake yaml options
	 * @throws NullPointerException if {@code configClazz}, {@code options}, or {@code yamlOptions} is null
	 * @throws IllegalArgumentException if {@code configClazz} is not an interface
	 * @throws IllDefinedConfigException if a configuration entry in {@code configClazz} is not defined properly
	 */
	public SnakeYamlConfigurationFactory(Class<C> configClazz, ConfigurationOptions options, SnakeYamlOptions yamlOptions) {
		super(configClazz, options);
		this.yamlOptions = Objects.requireNonNull(yamlOptions, "yamlOptions");
	}
	
	/**
	 * Creates from a configuration class and config options, using the default {@link SnakeYamlOptions} (including
	 * "block" flow style and UTF 8)
	 * 
	 * @param configClazz the config class
	 * @param options the config options
	 * @throws NullPointerException if {@code configClazz} or {@code options} is null
	 * @throws IllegalArgumentException if {@code configClazz} is not an interface
	 * @throws IllDefinedConfigException if a configuration entry in {@code configClazz} is not defined properly
	 */
	public SnakeYamlConfigurationFactory(Class<C> configClazz, ConfigurationOptions options) {
		this(configClazz, options, new SnakeYamlOptions.Builder().build());
	}
	
	@Override
	protected boolean supportsCommentsThroughWrapper() {
		return yamlOptions.useCommentingWriter();
	}
	
	@Override
	protected Charset charset() {
		return yamlOptions.charset();
	}
	
	@Override
	protected Map<String, Object> loadMapFromReader(Reader reader) throws IOException, ConfigFormatSyntaxException {
		Map<String, Object> map;
		try {
			map = yamlOptions.yamlSupplier().get().load(reader);
		} catch (YAMLException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			}
			throw new ConfigFormatSyntaxException(ex);
		}
		// SnakeYAML returns a null object for an empty document
		return (map == null) ? Collections.emptyMap() : map;
	}

	@Override
	protected void writeMapToWriter(Map<String, Object> rawMap, Writer writer) throws IOException {
		CommentedWriter commentedWriter = new CommentedWriter(writer, yamlOptions.commentFormat());
		commentedWriter.writeComments(getHeader());

		if (yamlOptions.useCommentingWriter()) {
			commentedWriter.writeMap(rawMap);

		} else {
			try {
				yamlOptions.yamlSupplier().get().dump(rawMap, writer);
			} catch (YAMLException ex) {
				Throwable cause = ex.getCause();
				if (cause instanceof IOException) {
					throw (IOException) cause;
				}
				throw new IOException("Unexpected YAMLException while writing to stream", ex);
			}
		}
	}

}
