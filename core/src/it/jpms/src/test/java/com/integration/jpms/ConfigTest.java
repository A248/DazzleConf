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

package com.integration.jpms;

import com.integration.jpms.exported.CustomType;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ConfigurationOptions;

public class ConfigTest {

	@Test
	public void loadConfig() {
		loadDefaults(Config.class);
	}

	@Test
	public void loadConfigWithDefaultMethods() {
		loadDefaults(ConfigWithDefaultMethods.class);
	}

	private <C> C loadDefaults(Class<C> configClass) {
		return new DefaultsOnlyFactory<>(configClass,
				new ConfigurationOptions.Builder().addSerialiser(new CustomType.Serialiser()).build()).loadDefaults();
	}

}
