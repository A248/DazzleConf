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
import java.util.List;
import java.util.Map;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.internal.AbstractConfigurationFactoryImpl;
import space.arim.dazzleconf.internal.ConfigurationInfo;
import space.arim.dazzleconf.internal.deprocessor.AddCommentStringBeforeDeprocessor;
import space.arim.dazzleconf.internal.deprocessor.CommentedDeprocessor;
import space.arim.dazzleconf.internal.deprocessor.MapDeprocessor;

/**
 * Abstract implementation of {@link ConfigurationFactory} which takes care of IO boilerplate as well as
 * configuration loading from a map. Only the required methods need be implemented.
 * 
 * @author A248
 *
 * @param <C> the type of the configuration
 */
public abstract class AbstractConfigurationFactory<C> extends DelegatingConfigurationFactory<C> {
	
	private final ConfigFactoryDelegate delegate;

	/**
	 * Creates from a config class and config options
	 * 
	 * @param configClass the config class
	 * @param options configuration options
	 * @throws NullPointerException if {@code configClass} or {@code options} is null
	 * @throws IllegalArgumentException if {@code configClass} is not an interface
	 * @throws IllDefinedConfigException if the configuration entries defined in the config class are invalid
	 */
	protected AbstractConfigurationFactory(Class<C> configClass, ConfigurationOptions options) {
		delegate = new ConfigFactoryDelegate(configClass, options);
	}
	
	// Extension
	
	/**
	 * The charset used by this factory
	 * 
	 * @return the charset to use
	 */
	protected abstract Charset charset();
	
	/**
	 * Loads a raw map of nested values from the specified reader. The map returned is a hierarchical
	 * map of nested maps.
	 * 
	 * @param reader the reader
	 * @return the raw map of values
	 * @throws IOException if an I/O error occurs
	 * @throws ConfigFormatSyntaxException if the configuration format syntax is incorrect
	 */
	protected abstract Map<String, Object> loadMapFromReader(Reader reader) throws IOException, ConfigFormatSyntaxException;
	
	/**
	 * Writes a raw map of nested values to the specified writer. Inverse operation of {@link #loadMapFromReader(Reader)}. <br>
	 * <br>
	 * For implementations returning {@code true} in {@link #supportsCommentsThroughWrapper()}, special treatment is required
	 * in the implementation of this method. Namely, configuration values may be commented or uncommented. If uncommented,
	 * they are an everyday object. If commented, they are wrapped in {@link CommentedWrapper}, which includes the config value
	 * itself as well as the comments which are placed before it. Comments may also apply to config sections, therefore
	 * {@code CommentWrapper} may also wrap nested maps.
	 * 
	 * @param config the raw map of nested values (and possibly comments)
	 * @param writer the writer
	 * @throws IOException if an I/O error occurs
	 */
	protected abstract void writeMapToWriter(Map<String, Object> config, Writer writer) throws IOException;
	
	/**
	 * Whether this implementation actively supports comments by recognising {@link CommentedWrapper} in config values.
	 * It is insufficient for the underlying format to support comments; the implementation of this config factory must
	 * also recognise {@code CommentedWrapper} values. See {@link #writeMapToWriter(Map, Writer)} for more information.
	 * 
	 * @return true if comments supported by recognising {@code CommentedWrapper}, false otherwise
	 */
	protected boolean supportsCommentsThroughWrapper() {
		return false;
	}
	
	/**
	 * If the configuration format does not natively support comments, it is possible to attach a "comment"
	 * as a string value before the configuration entry which is to be commented, with the use of some key suffix. <br>
	 * <br>
	 * If this method returns a non empty string, this feature is enabled. For example, if this returns
	 * {@literal '-comment'}, using the Json configuration language, comments will be written as shown:
	 * <pre>
	 *   retries-comment: "determines the amount of retries"
     *   retries: 3
     * </pre>
	 * 
	 * @return an empty string if disabled, otherwise the suffix to append to form the keys of comments
	 */
	protected String pseudoCommentsSuffix() {
		return "";
	}
	
	/**
	 * Gets the comment header on the top level configuration
	 * 
	 * @return the comment header
	 */
	protected List<String> getHeader() {
		return delegate().getDefinition().getHeader();
	}
	
	// Delegation
	
	@Override
	ConfigFactoryDelegate delegate() {
		return delegate;
	}
	
	// Implementation
	
	private class ConfigFactoryDelegate extends AbstractConfigurationFactoryImpl<C> {

		protected ConfigFactoryDelegate(Class<C> configClazz, ConfigurationOptions options) {
			super(configClazz, options);
		}
		
		@Override
		protected ConfigurationInfo<C> getDefinition() {
			return super.getDefinition();
		}

		@Override
		protected Charset charset() {
			return AbstractConfigurationFactory.this.charset();
		}

		@Override
		protected Map<String, Object> loadMapFromReader(Reader reader) throws IOException, ConfigFormatSyntaxException {
			return AbstractConfigurationFactory.this.loadMapFromReader(reader);
		}

		@Override
		protected void writeMapToWriter(Map<String, Object> config, Writer writer) throws IOException {
			AbstractConfigurationFactory.this.writeMapToWriter(config, writer);
		}
		
		@Override
		protected MapDeprocessor<C> createMapDeprocessor(C configData) {
			if (AbstractConfigurationFactory.this.supportsCommentsThroughWrapper()) {
				return new CommentedDeprocessor<>(getDefinition(), configData);
			}
			String pseudoCommentsSuffix = AbstractConfigurationFactory.this.pseudoCommentsSuffix();
			if (!pseudoCommentsSuffix.isEmpty()) {
				return new AddCommentStringBeforeDeprocessor<>(getDefinition(), configData, pseudoCommentsSuffix);
			}
			return super.createMapDeprocessor(configData);
		}
		
	}
	
}
