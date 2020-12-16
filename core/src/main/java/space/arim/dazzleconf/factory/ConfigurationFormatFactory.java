package space.arim.dazzleconf.factory;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.internal.ConfigurationInfo;
import space.arim.dazzleconf.internal.DefinitionReader;
import space.arim.dazzleconf.internal.deprocessor.AddCommentStringBeforeDeprocessor;
import space.arim.dazzleconf.internal.deprocessor.CommentedDeprocessor;
import space.arim.dazzleconf.internal.deprocessor.MapDeprocessor;
import space.arim.dazzleconf.internal.processor.DefaultsProcessor;
import space.arim.dazzleconf.internal.processor.MapProcessor;
import space.arim.dazzleconf.internal.processor.ProcessorBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract implementation of {@link ConfigurationFactory} which takes care of configuration loading
 * from a map. <br>
 * <br>
 * To get rid of further IO boilerplate when the format library requires {@code Reader}
 * and {@code Writer} instances, see {@link HumanReadableConfigurationFactory}. <br>
 * <br>
 * <b>Hierarchical Maps</b> <br>
 * When a method in this class refers to a "hierarchical map", it is describing a map of keys and values,
 * whose values may themselves contain further maps. This is the structure used with regards to nested
 * configuration sections. <br>
 * <br>
 * <b>Comment wrappers</b> <br>
 * For implementations returning {@code true} in {@link #supportsCommentsThroughWrapper()}, special treatment is required
 * in the implementation of {@code writeMap} methods. Namely, configuration values may be commented or uncommented. If uncommented,
 * they are a plain object. If commented, they are wrapped in {@link CommentedWrapper}, which includes the config value
 * itself as well as the comments which are placed before it. As comments may apply to config sections,
 * {@code CommentWrapper} may also wrap nested maps.
 *
 * @param <C> the type of the configuration
 * @author A248
 */
public abstract class ConfigurationFormatFactory<C> implements ConfigurationFactory<C> {

	private final Class<C> configClass;
	private final ConfigurationOptions options;
	private final ConfigurationInfo<C> definition;

	/**
	 * Creates from a config class and config options
	 *
	 * @param configClass the config class
	 * @param options     configuration options
	 * @throws NullPointerException      if {@code configClass} or {@code options} is null
	 * @throws IllegalArgumentException  if {@code configClass} is not an interface
	 * @throws IllDefinedConfigException if the configuration entries defined in the config class are invalid
	 */
	protected ConfigurationFormatFactory(Class<C> configClass, ConfigurationOptions options) {
		Objects.requireNonNull(configClass, "configClazz");
		if (!configClass.isInterface()) {
			throw new IllegalArgumentException(configClass.getName() + " is not an interface");
		}
		this.configClass = configClass;
		this.options = Objects.requireNonNull(options, "options");
		definition = new DefinitionReader<>(configClass, options).read();
	}

	@Override
	public final Class<C> getConfigClass() {
		return configClass;
	}

	@Override
	public final ConfigurationOptions getOptions() {
		return options;
	}

	/**
	 * Gets the comment header on the top level configuration. This is the
	 * document wide comment header, at the top of the file.
	 *
	 * @return the top level comment header
	 */
	public final List<String> getHeader() {
		return definition.getHeader();
	}

	/*
	 * Reading
	 */

	@Override
	public final C load(ReadableByteChannel readChannel) throws IOException, InvalidConfigException {
		return fromRawMap(loadMap(readChannel));
	}

	@Override
	public final C load(InputStream inputStream) throws IOException, InvalidConfigException {
		return fromRawMap(loadMap(inputStream));
	}

	@Override
	public final C load(ReadableByteChannel readChannel, C auxiliaryEntries) throws IOException, InvalidConfigException {
		Objects.requireNonNull(configClass.cast(auxiliaryEntries), "auxiliaryEntries");
		return fromRawMap(loadMap(readChannel), auxiliaryEntries);
	}

	@Override
	public final C load(InputStream inputStream, C auxiliaryEntries) throws IOException, InvalidConfigException {
		Objects.requireNonNull(configClass.cast(auxiliaryEntries), "auxiliaryEntries");
		return fromRawMap(loadMap(inputStream), auxiliaryEntries);
	}

	@Override
	public final C loadDefaults() {
		ProcessorBase<C> processor = new DefaultsProcessor<>(options, definition);
		try {
			return processor.createConfig();
		} catch (InvalidConfigException ex) {
			throw new IllDefinedConfigException(ex);
		}
	}

	/**
	 * Loads a map of config values from an input channel.
	 *
	 * @param readChannel the channel from which to read
	 * @return the hierarchical configuration map
	 * @throws IOException            if an I/O error occurs
	 * @throws InvalidConfigException if the configuration format dictates the data is not valid syntax.
	 *                                Usually {@code ConfigFormatSyntaxException}
	 */
	public abstract Map<String, Object> loadMap(ReadableByteChannel readChannel)
			throws IOException, InvalidConfigException;

	/**
	 * Loads a map of config values from an input stream.
	 *
	 * @param inputStream the stream from which to read
	 * @return the hierarchical configuration map
	 * @throws IOException            if an I/O error occurs
	 * @throws InvalidConfigException if the configuration format dictates the data is not valid syntax.
	 *                                Usually {@code ConfigFormatSyntaxException}
	 */
	public abstract Map<String, Object> loadMap(InputStream inputStream)
			throws IOException, InvalidConfigException;

	private C fromRawMap(Map<String, Object> rawMap) throws InvalidConfigException {
		return fromRawMap(rawMap, null);
	}

	private C fromRawMap(Map<String, Object> rawMap, C auxiliaryValues) throws InvalidConfigException {
		return new MapProcessor<>(getOptions(), definition, rawMap, auxiliaryValues).createConfig();
	}

	/*
	 * Writing
	 */

	@Override
	public final void write(C configData, WritableByteChannel writeChannel) throws IOException {
		Objects.requireNonNull(configClass.cast(configData), "configData");
		writeMap(toRawMap(configData), writeChannel);
	}

	@Override
	public final void write(C configData, OutputStream outputStream) throws IOException {
		Objects.requireNonNull(configClass.cast(configData), "configData");
		writeMap(toRawMap(configData), outputStream);
	}

	/**
	 * Writes a map of config values to an output channel
	 *
	 * @param config the hierarchical configuration map
	 * @param writeChannel the channel to which to write
	 * @throws IOException if an I/O error occurs
	 */
	public abstract void writeMap(Map<String, Object> config, WritableByteChannel writeChannel)
			throws IOException;

	/**
	 * Writes a map of config values to an output stream
	 *
	 * @param config the hierarchical configuration map
	 * @param outputStream the stream to which to write
	 * @throws IOException if an I/O error occurs
	 */
	public abstract void writeMap(Map<String, Object> config, OutputStream outputStream)
			throws IOException;

	private Map<String, Object> toRawMap(C configData) {
		MapDeprocessor<C> simpleDeprocessor = createMapDeprocessor(configData);
		return simpleDeprocessor.deprocessAndGetResult();
	}

	private MapDeprocessor<C> createMapDeprocessor(C configData) {
		if (supportsCommentsThroughWrapper()) {
			return new CommentedDeprocessor<>(definition, configData);
		}
		String pseudoCommentsSuffix = pseudoCommentsSuffix();
		if (!pseudoCommentsSuffix.isEmpty()) {
			return new AddCommentStringBeforeDeprocessor<>(definition, configData, pseudoCommentsSuffix);
		}
		return new MapDeprocessor<>(definition, configData);
	}

	/*
	 * Extra features
	 */

	/**
	 * Whether this implementation actively supports comments by recognising {@link CommentedWrapper} in config values.
	 * It is insufficient for the underlying format to support comments; the implementation of this config factory must
	 * also recognise {@code CommentedWrapper} values. See the {@code writeMap} methods for more information.
	 *
	 * @return true if comments supported by recognising {@code CommentedWrapper}, false otherwise
	 */
	public boolean supportsCommentsThroughWrapper() {
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
	public String pseudoCommentsSuffix() {
		return "";
	}

}
