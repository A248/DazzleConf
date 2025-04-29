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

package space.arim.dazzleconf;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.factory.DefaultsOnlyFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class V1EqualityTest {

	private <C> ConfigurationFactory<C> factoryFor(Class<C> configClass) {
		return new DefaultsOnlyFactory<>(configClass, ConfigurationOptions.defaults());
	}

	@Test
	public void equals() {
		Config config = factoryFor(Config.class).loadDefaults();
		assertEquals(config, config);
	}

	@Test
	public void notEquals() {
		Config config = factoryFor(Config.class).loadDefaults();
		Config configAlso = factoryFor(Config.class).loadDefaults();
		assertNotEquals(config, configAlso);
	}

	@Test
	public void notEqualsSimilarValues() {
		Config config = factoryFor(Config.class).loadDefaults();
		ConfigTwo configTwo = factoryFor(ConfigTwo.class).loadDefaults();
		assertNotEquals(config, configTwo);
	}

	public interface Config {
		@ConfDefault.DefaultString("default value")
		String value();
	}

	public interface ConfigTwo {
		@ConfDefault.DefaultString("default value")
		String value();
	}

}
