package space.arim.dazzleconf.ext.hocon;

import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.error.MissingValueException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import space.arim.dazzleconf.factory.HumanReadableConfigurationFactory;

public class HoconConfigurationFactory<C> extends HumanReadableConfigurationFactory<C> {

  private final HoconOptions hoconOptions;

  /**
   * Creates from a config class and config options
   *
   * @param configClass  the config class
   * @param options      configuration options
   * @param hoconOptions parsing and reading options, can not be null
   * @throws NullPointerException      if {@code configClass} or {@code options} is null
   * @throws IllegalArgumentException  if {@code configClass} is not an interface
   * @throws IllDefinedConfigException if the configuration entries defined in the config class are invalid
   */
  public HoconConfigurationFactory(Class<C> configClass, ConfigurationOptions options, HoconOptions hoconOptions) {
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
      return ConfigFactory.parseReader(reader).root().unwrapped();
    } catch (ConfigException e) {
      if (e instanceof ConfigException.BadValue) {
        throw new BadValueException.Builder().cause(e).build();
      }

      if (e instanceof ConfigException.IO) {
        throw new IOException(e);
      }

      if (e instanceof ConfigException.Null) {
        throw new BadValueException.Builder().cause(e.getCause()).message(e.getMessage()).build();
      }

      if (e instanceof ConfigException.Missing){
        throw MissingValueException.forKeyAndMessage("unknown", e.getMessage());
      }

      if (e instanceof ConfigException.Parse) {
        throw new ConfigFormatSyntaxException(e);
      }

      throw new IOException("Unknown ConfigException subclass", e);
    }
  }

  @Override
  public void writeMap(Map<String, Object> config, Writer writer) throws IOException {
    writer.write(ConfigFactory.parseMap(config).root().render());
  }
}
