package space.arim.dazzleconf.ext.hocon;

import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.error.MissingValueException;
import space.arim.dazzleconf.factory.CommentedWrapper;
import space.arim.dazzleconf.factory.HumanReadableConfigurationFactory;

final class HoconConfigurationFactoryImpl<C> extends HumanReadableConfigurationFactory<C> {

  private final HoconOptions hoconOptions;

  HoconConfigurationFactoryImpl(Class<C> configClass, ConfigurationOptions options,
                                HoconOptions hoconOptions) {
    super(configClass, options);
    this.hoconOptions = Objects.requireNonNull(hoconOptions);
  }

  @Override
  public Charset charset() {
    return hoconOptions.charset();
  }

  @Override
  public Map<String, Object> loadMap(Reader reader) throws IOException, InvalidConfigException {
    try {
      return ConfigFactory.parseReader(reader, hoconOptions.configParseOptions()).root().unwrapped();

    } catch (ConfigException.BadValue | ConfigException.Null e) {
      throw new BadValueException.Builder().cause(e).build();

    } catch (ConfigException.IO e) {
      throw new IOException(e);

    } catch (ConfigException.Missing e) {
      MissingValueException mve = MissingValueException.forKey("unknown");
      mve.initCause(e);
      throw mve;

    } catch (ConfigException.Parse e) {
      throw new ConfigFormatSyntaxException(e);

    } catch (ConfigException e) {
      throw new IOException("Unexpected ConfigException subclass", e);
    }
  }

  @Override
  public void writeMap(Map<String, Object> config, Writer writer) throws IOException {
    ConfigObject hoconConfig = convertMapToHocon(config);
    List<String> commentHeader = getHeader();
    if (!commentHeader.isEmpty()) {
      hoconConfig = hoconConfig.withOrigin(hoconConfig.origin().withComments(commentHeader));
    }

    writer.write(hoconConfig.render(hoconOptions.configRenderOptions()));
  }

  private ConfigObject convertMapToHocon(Map<String, Object> config) {
    Map<String, Object> hoconConfigMap = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : config.entrySet()) {
      hoconConfigMap.put(entry.getKey(), convertValueToHocon(entry.getValue()));
    }

    return ConfigValueFactory.fromMap(hoconConfigMap);
  }

  private ConfigValue convertValueToHocon(Object value) {
    if (value instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = (Map<String, Object>) value;

      return convertMapToHocon(map);
    }
    if (value instanceof CommentedWrapper) {
      CommentedWrapper commentedWrapper = (CommentedWrapper) value;
      ConfigValue hoconValue = convertValueToHocon(commentedWrapper.getValue());

      return hoconValue.withOrigin(hoconValue.origin().withComments(commentedWrapper.getComments()));
    }

    return ConfigValueFactory.fromAnyRef(value);
  }

  @Override
  public boolean supportsCommentsThroughWrapper() {
    return true;
  }
}
