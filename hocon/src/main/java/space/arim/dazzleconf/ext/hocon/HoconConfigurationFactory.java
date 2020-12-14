package space.arim.dazzleconf.ext.hocon;

import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.factory.AbstractConfigurationFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

public class HoconConfigurationFactory<C> extends AbstractConfigurationFactory<C> {

  private final HoconOptions hoconOptions;

  /**
   * Creates from a config class and config options
   *
   * @param configClass  the config class
   * @param options      configuration options
   * @param hoconOptions parsing & reading options, can not be null
   * @throws NullPointerException      if {@code configClass} or {@code options} is null
   * @throws IllegalArgumentException  if {@code configClass} is not an interface
   * @throws IllDefinedConfigException if the configuration entries defined in the config class are invalid
   */
  public HoconConfigurationFactory(Class<C> configClass, ConfigurationOptions options, HoconOptions hoconOptions) {
    super(configClass, options);
    this.hoconOptions = Objects.requireNonNull(hoconOptions);
  }

  @Override
  protected Charset charset() {
    return hoconOptions.charset();
  }

  @Override
  protected Map<String, Object> loadMapFromReader(Reader reader) throws IOException, ConfigFormatSyntaxException {
    try {
      return ConfigFactory.parseReader(reader).root().unwrapped();
    } catch (ConfigException e) {
      if (e.getCause() instanceof ConfigException.Parse) {
        throw new ConfigFormatSyntaxException(e);
      }
      throw new IOException(e);
    }
  }

  @Override
  protected void writeMapToWriter(Map<String, Object> config, Writer writer) throws IOException {
    writer.write(ConfigFactory.parseMap(config).root().render());
  }
}
