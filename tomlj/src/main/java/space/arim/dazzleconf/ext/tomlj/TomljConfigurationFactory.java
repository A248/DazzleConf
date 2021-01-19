/*
 * DazzleConf
 * Copyright © 2021 Anand Beh
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

package space.arim.dazzleconf.ext.tomlj;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.IllDefinedConfigException;

import java.util.Objects;

/**
 * Allows obtaining a {@link ConfigurationFactory} implementation using Tomlj to load a toml configuration.
 *
 */
public final class TomljConfigurationFactory {

	private TomljConfigurationFactory() {}

	/**
	 * Creates from a configuration class, config options, and {@link TomljOptions}
	 *
	 * @param configClass the config class
	 * @param options the config options
	 * @param tomlOptions the toml options
	 * @param <C> the configuration type
	 * @return the configuration factory
	 * @throws IllegalArgumentException if {@code configClass} is not an interface
	 * @throws IllDefinedConfigException if a configuration entry in {@code configClass} is not defined properly
	 */
	public static <C> ConfigurationFactory<C> create(Class<C> configClass, ConfigurationOptions options,
													 TomljOptions tomlOptions) {
		return new TomljConfigurationFactoryImpl<>(configClass, options,
				Objects.requireNonNull(tomlOptions, "tomlOptions"));
	}

	/**
	 * Creates from a configuration class and config options. <br>
	 * <br>
	 * Uses the default toml options, which include the latest toml spec version and UTF 8 encoding
	 *
	 * @param configClass the config class
	 * @param options the toml options
	 * @param <C> the configuration type
	 * @return the configuration factory
	 * @throws IllegalArgumentException if {@code configClass} is not an interface
	 * @throws IllDefinedConfigException if a configuration entry in {@code configClass} is not defined properly
	 */
	public static <C> ConfigurationFactory<C> create(Class<C> configClass, ConfigurationOptions options) {
		return create(configClass, options, new TomljOptions.Builder().build());
	}

}
