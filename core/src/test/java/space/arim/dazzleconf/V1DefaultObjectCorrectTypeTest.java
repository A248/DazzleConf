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
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.factory.DefaultsOnlyFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class V1DefaultObjectCorrectTypeTest {

	@Test
	public void processDefaultObject() {
		assertThrows(IllDefinedConfigException.class, () -> {
			new DefaultsOnlyFactory<>(Config.class, ConfigurationOptions.defaults()).loadDefaults();
		});
	}

	/*
	 * This used to work in a previous snapshot version (1.2.0-SNAPSHOT)
	 * It should not be allowed as it defeats the purpose of @DefaultObject and
	 * makes interactions between the static method and the framework confusing.
	 */

	public static String defaultTrue() {
		return "true";
	}

	public interface Config {

		@ConfDefault.DefaultObject("space.arim.dazzleconf.DefaultObjectCorrectTypeTest.defaultTrue")
		boolean toggle();
	}
}
