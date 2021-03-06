package space.arim.dazzleconf.ext.hocon;

import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class HoconOptions {
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

  @Override
  public String toString() {
    return "HoconOptions{" +
           "configParseOptions=" + configParseOptions +
           ", configRenderOptions=" + configRenderOptions +
           ", charset=" + charset +
           '}';
  }

  /**
   * Builder of {@code HoconOptions}
   *
   * @author CDFN
   */
  public final static class Builder {
    ConfigParseOptions configParseOptions = ConfigParseOptions.defaults();
    ConfigRenderOptions configRenderOptions = ConfigRenderOptions.defaults()
        .setOriginComments(false)
        .setJson(false);

    Charset charset = StandardCharsets.UTF_8;

    /**
     * Sets parse options. Reading config uses them.
     *
     * @param options the options
     * @return this builder
     */
    public Builder configParseOptions(ConfigParseOptions options) {
      this.configParseOptions = Objects.requireNonNull(options);
      return this;
    }

    /**
     * Sets rendering options. Writing config uses them.
     *
     * @param options the options
     * @return this builder
     */
    public Builder configRenderOptions(ConfigRenderOptions options) {
      this.configRenderOptions = Objects.requireNonNull(options);
      return this;
    }

    /**
     * Sets the charset used by the factory. Default is UTF 8
     *
     * @param charset the charset
     * @return this builder
     */
    public Builder charset(Charset charset) {
      this.charset = Objects.requireNonNull(charset);
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

    @Override
    public String toString() {
      return "HoconOptions.Builder{" +
             "configParseOptions=" + configParseOptions +
             ", configRenderOptions=" + configRenderOptions +
             ", charset=" + charset +
             '}';
    }
  }
}
