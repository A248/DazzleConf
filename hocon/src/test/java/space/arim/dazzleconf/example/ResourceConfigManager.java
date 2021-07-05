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

import space.arim.dazzleconf.AuxiliaryKeys;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.hocon.HoconConfigurationFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class ResourceConfigManager<C> {

	private final Path configFile;
	private final String resource;
	private final ConfigurationFactory<C> factory;
	private volatile C configData;

	public ResourceConfigManager(Path configFile, String resource, ConfigurationFactory<C> factory) {
		this.configFile = configFile;
		this.resource = resource;
		this.factory = factory;
	}

	public static <C> ResourceConfigManager<C> create(Path configFile, String resource, Class<C> configClass) {
		// Hocon example
		ConfigurationFactory<C> configFactory = HoconConfigurationFactory.create(
				configClass,
				ConfigurationOptions.defaults());
		return new ResourceConfigManager<>(configFile, resource, configFactory);
	}

	private InputStream obtainDefaultResource() {
		return getClass().getResourceAsStream("/" + resource);
	}

	private C loadDefaultsFromResource() {
		try (InputStream resourceStream = obtainDefaultResource()) {
			return factory.load(resourceStream);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (InvalidConfigException ex) {
			throw new IllDefinedConfigException("Default config resource is broken", ex);
		}
	}

	private C reloadConfigData() throws IOException, InvalidConfigException {
		// Create parent directory if it does not exist
		Path parentDir = configFile.toAbsolutePath().getParent();
		if (parentDir != null) {
			Files.createDirectories(parentDir);
		}

		C defaults = loadDefaultsFromResource();

		if (!Files.exists(configFile)) {

			// Copy default config data
			try (FileChannel fileChannel = FileChannel.open(configFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
				 InputStream defaultResource = obtainDefaultResource();
				 ReadableByteChannel defaultResourceChannel = Channels.newChannel(defaultResource)) {

				fileChannel.transferFrom(defaultResourceChannel, 0, Long.MAX_VALUE);
			}
			return defaults;
		}

		C loadedData;
		try (FileChannel fileChannel = FileChannel.open(configFile, StandardOpenOption.READ)) {
			loadedData = factory.load(fileChannel, defaults);
		}
		if (loadedData instanceof AuxiliaryKeys) {
			// Update config with latest keys
			try (FileChannel fileChannel = FileChannel.open(configFile,
					StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
				factory.write(loadedData, fileChannel);
			}
		}
		return loadedData;
	}

	public void reloadConfig() {
		try {
			configData = reloadConfigData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);

		} catch (ConfigFormatSyntaxException ex) {
			configData = loadDefaultsFromResource();
			System.err.println("The HOCON syntax in your configuration is invalid.");
			ex.printStackTrace();

		} catch (InvalidConfigException ex) {
			configData = loadDefaultsFromResource();
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
		ResourceConfigManager<Config> configManager = ResourceConfigManager.create(configFolder, fileName, Config.class);
		configManager.reloadConfig();
		Config config = configManager.getConfigData();
		return config;
	}

}
