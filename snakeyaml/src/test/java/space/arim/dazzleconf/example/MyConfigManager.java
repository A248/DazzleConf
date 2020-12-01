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
package space.arim.dazzleconf.example;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;
import space.arim.dazzleconf.helper.ConfigurationHelper;

public class MyConfigManager<C> extends ConfigurationHelper<C> {

	private volatile C configData;

	private MyConfigManager(Path configFolder, String fileName, ConfigurationFactory<C> factory) {
		super(configFolder, fileName, factory);
	}

	public static <C> MyConfigManager<C> create(Path configFolder, String fileName, Class<C> configClass) {
		// SnakeYaml example
		SnakeYamlOptions yamlOptions = new SnakeYamlOptions.Builder()
				.useCommentingWriter(true) // Enables writing YAML comments
				.build();
		return new MyConfigManager<>(configFolder, fileName,
				new SnakeYamlConfigurationFactory<>(configClass, ConfigurationOptions.defaults(), yamlOptions));
	}

	public void reloadConfig() {
		try {
			reloadConfigData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);

		} catch (ConfigFormatSyntaxException ex) {
			configData = getFactory().loadDefaults();
			System.err.println("Uh-oh! The syntax of your configuration are invalid. "
					+ "Check your YAML syntax with a tool such as https://yaml-online-parser.appspot.com/");
			ex.printStackTrace();

		} catch (InvalidConfigException ex) {
			configData = getFactory().loadDefaults();
			System.err.println("Uh-oh! The values in your configuration are invalid. "
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

}
