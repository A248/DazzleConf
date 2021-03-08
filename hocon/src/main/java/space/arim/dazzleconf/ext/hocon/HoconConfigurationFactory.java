package space.arim.dazzleconf.ext.hocon;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.IllDefinedConfigException;

/**
 * Allows creating a {@link ConfigurationFactory} implementation using Hocon.
 *
 */
public final class HoconConfigurationFactory {

    private HoconConfigurationFactory() {}

    /**
     * Creates from a config class, config options, and hocon options
     *
     * @param <C> the configuration type
     * @param configClass  the config class
     * @param options      configuration options
     * @param hoconOptions parsing and reading options, can not be null
     * @return the configuration factory
     * @throws NullPointerException      if any parameter is null
     * @throws IllegalArgumentException  if {@code configClass} is not an interface
     * @throws IllDefinedConfigException if the configuration entries defined in the config class are
     *                                   invalid
     */
    public static <C> ConfigurationFactory<C> create(Class<C> configClass, ConfigurationOptions options,
                                                     HoconOptions hoconOptions) {
        return new HoconConfigurationFactoryImpl<>(configClass, options, hoconOptions);
    }

    /**
     * Creates from a config class and config options. <br>
     * <br>
     * Uses the default hocon options
     *
     * @param <C> the configuration type
     * @param configClass  the config class
     * @param options      configuration options
     * @return the configuration factory
     * @throws NullPointerException      if either parameter is null
     * @throws IllegalArgumentException  if {@code configClass} is not an interface
     * @throws IllDefinedConfigException if the configuration entries defined in the config class are
     *                                   invalid
     */
    public static <C> ConfigurationFactory<C> create(Class<C> configClass, ConfigurationOptions options) {
        return create(configClass, options, new HoconOptions.Builder().build());
    }

}
