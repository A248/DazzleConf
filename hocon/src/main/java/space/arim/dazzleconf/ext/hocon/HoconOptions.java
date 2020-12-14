package space.arim.dazzleconf.ext.hocon;

import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HoconOptions {
  private final ConfigParseOptions configParseOptions;
  private final ConfigRenderOptions configRenderOptions;
  private final Charset charset;

  HoconOptions(Builder builder) {
    this.configParseOptions = builder.configParseOptions;
    this.configRenderOptions = builder.configRenderOptions;
    this.charset = builder.charset;
  }

  /**
   * Gets used parse options
   *
   * @return used parse options
   */
  public ConfigParseOptions configParseOptions() {
    return configParseOptions;
  }

  /**
   * Gets used render options.
   *
   * @return used render options
   */
  public ConfigRenderOptions configRenderOptions() {
    return configRenderOptions;
  }

  /**
   * Gets used charset
   *
   * @return used charset
   */
  public Charset charset() {
    return charset;
  }

  /**
   * Builder of {@code HoconOptions}
   *
   * @author CDFN
   */
  public static class Builder {
    ConfigParseOptions configParseOptions = ConfigParseOptions.defaults();
    ConfigRenderOptions configRenderOptions = ConfigRenderOptions.defaults();
    Charset charset = StandardCharsets.UTF_8;

    /**
     * Sets parse options. Reading config uses them.
     *
     * @param options the options
     * @return this builder
     */
    public Builder configParseOptions(ConfigParseOptions options) {
      this.configParseOptions = options;
      return this;
    }

    /**
     * Sets rendering options. Writing config uses them.
     *
     * @param options the options
     * @return this builder
     */
    public Builder configRenderOptions(ConfigRenderOptions options) {
      this.configRenderOptions = options;
      return this;
    }

    /**
     * Sets the charset used by the factory. Default is UTF 8
     *
     * @param charset the charset
     * @return this builder
     */
    public Builder charset(Charset charset) {
      this.charset = charset;
      return this;
    }

    /**
     * Builds the options. May be used repeatedly without side effects
     *
     * @return the built options
     */
    public HoconOptions build() {
      return new HoconOptions(this);
    }
  }
}
