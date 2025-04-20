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

package space.arim.dazzleconf.internal;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.factory.FixedLoaderFactory;
import space.arim.dazzleconf.factory.SerialisationFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Credits to Auriium for discovering this bug and providing the test
 */
public class ExplosiveConfigTest {

	/*
	Note: These tests can exercise different code paths depending on
	the undefined ordering of the return value of Class#getMethods

	These tests are also closely tied to the implementation of NestedMapHelper
	 */

	private ConfigurationOptions options() {
		return new ConfigurationOptions.Builder().setDottedPathInConfKey(true).build();
	}

	@Test
	public void explosiveConfigWrite() {
		var factory = new SerialisationFactory<>(ExplosiveConfig.class, options());
		ExplosiveConfig config = factory.loadDefaults();
		assertThrows(IllDefinedConfigException.class, () -> factory.write(config, OutputStream.nullOutputStream()));
	}

	@Test
	public void explosiveConfigLoad() {
		Map<String, Object> source = Map.of("section", "not really a section");
		var factory = new FixedLoaderFactory<>(ExplosiveConfig.class, options(), source);
		assertThrows(IllDefinedConfigException.class, () -> factory.load(InputStream.nullInputStream()));
	}

	public interface ExplosiveConfig {

		@ConfKey("section")
		@ConfDefault.DefaultString("some value")
		String value();

		@ConfKey("section.subKey")
		@ConfDefault.DefaultString("another value")
		String other();
	}
}
