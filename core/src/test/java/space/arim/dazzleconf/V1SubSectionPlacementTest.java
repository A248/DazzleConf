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
import space.arim.dazzleconf.annote.SubSection;
import space.arim.dazzleconf.factory.DefaultsOnlyFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class V1SubSectionPlacementTest {

	@Test
	public void subSectionPlacement() {
		Config config = new DefaultsOnlyFactory<>(Config.class, ConfigurationOptions.defaults()).loadDefaults();
		assertEquals("val", config.value());
		assertEquals("nestedVal", config.subConfig().nestedValue());
	}

	public interface Config {

		@ConfDefault.DefaultString("val")
		String value();

		SubConfig subConfig();
	}

	@SubSection
	public interface SubConfig {

		@ConfDefault.DefaultString("nestedVal")
		String nestedValue();
	}
}
