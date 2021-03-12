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

package space.arim.dazzleconf.example;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.CommentMode;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;
import space.arim.dazzleconf.helper.ConfigurationHelper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public final class ConfigManager<C> {

	private final ConfigurationHelper<C> configHelper;
	private volatile C configData;

	private ConfigManager(ConfigurationHelper<C> configHelper) {
		this.configHelper = configHelper;
	}

	public static <C> ConfigManager<C> create(Path configFolder, String fileName, Class<C> configClass) {
		// SnakeYaml example
		SnakeYamlOptions yamlOptions = new SnakeYamlOptions.Builder()
				.commentMode(CommentMode.alternativeWriter()) // Enables writing YAML comments
				.build();
		ConfigurationFactory<C> configFactory = SnakeYamlConfigurationFactory.create(
				configClass,
				ConfigurationOptions.defaults(), // change this if desired
				yamlOptions);
		return new ConfigManager<>(new ConfigurationHelper<>(configFolder, fileName, configFactory));
	}

	public void reloadConfig() {
		try {
			configData = configHelper.reloadConfigData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);

		} catch (ConfigFormatSyntaxException ex) {
			configData = configHelper.getFactory().loadDefaults();
			System.err.println("The yaml syntax in your configuration is invalid. "
					+ "Check your YAML syntax with a tool such as https://yaml-online-parser.appspot.com/");
			ex.printStackTrace();

		} catch (InvalidConfigException ex) {
			configData = configHelper.getFactory().loadDefaults();
			System.err.println("One of the values in your configuration is not valid. "
					+ "Check to make sure you have specified the right data types.");
			ex.printStackTrace();
		}
	}

	public C getConfigData() {
		C configData = this.configData;
		if (configData == null) {
			throw new IllegalStateException("Configuration has not been loaded yet");
		}
		return configData;
	}

	private interface Config {}
	private static Config usage(Path configFolder, String fileName) {
		ConfigManager<Config> configManager = ConfigManager.create(configFolder, fileName, Config.class);
		configManager.reloadConfig();
		Config config = configManager.getConfigData();
		return config;
	}

}
