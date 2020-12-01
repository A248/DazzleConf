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
package space.arim.dazzleconf.helper;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import space.arim.dazzleconf.AuxiliaryKeys;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.error.InvalidConfigException;

/**
 * Simple helper class designed to assist in reloading a configuration
 * 
 * @author A248
 *
 * @param <C> the type of the configuration
 */
public class ConfigurationHelper<C> {

	private final Path configFolder;
	private final String fileName;
	private final ConfigurationFactory<C> factory;

	/**
	 * Creates from an enclosing directory, filename within that directory, and {@code ConfigurationFactory}. <br>
	 * <br>
	 * The configuration path will be located at <code>configFolder.resolve(fileName)</code>
	 * 
	 * @param configFolder the enclosing directory
	 * @param fileName the filename within the directory
	 * @param factory the configuration factory
	 */
	public ConfigurationHelper(Path configFolder, String fileName, ConfigurationFactory<C> factory) {
		this.configFolder = configFolder;
		this.fileName = fileName;
		this.factory = factory;
	}

	/**
	 * Gets the configuration factory
	 * 
	 * @return the configuration factory
	 */
	public ConfigurationFactory<C> getFactory() {
		return factory;
	}

	/**
	 * Loads/reloads the configuration data. If necessary, updates the config on the
	 * filesystem with the latest keys
	 * 
	 * @throws IOException if an I/O exception occurred
	 * @throws InvalidConfigException if the loaded configuration was not valid (user error)
	 * @return the loaded config data
	 */
	public C reloadConfigData() throws IOException, InvalidConfigException {

		// Create parent directory if it does not exist
		Files.createDirectories(configFolder);

		C defaults = factory.loadDefaults();

		Path configPath = configFolder.resolve(fileName);
		if (!Files.exists(configPath)) {

			// Copy default config data
			try (FileChannel fileChannel = FileChannel.open(configPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
				factory.write(defaults, fileChannel);
			}
			// We just wrote the default values; loading them again would be pointless
			return defaults;
		}

		C loadedData;
		try (FileChannel fileChannel = FileChannel.open(configPath, StandardOpenOption.READ)) {
			loadedData = factory.load(fileChannel, defaults);
		}
		if (loadedData instanceof AuxiliaryKeys) {
			// Update config with latest keys
			try (FileChannel fileChannel = FileChannel.open(configPath,
					StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
				factory.write(loadedData, fileChannel);
			}
		}
		return loadedData;
	}

}
