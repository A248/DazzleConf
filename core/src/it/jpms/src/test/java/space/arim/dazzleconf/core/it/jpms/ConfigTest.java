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

package space.arim.dazzleconf.core.it.jpms;

import java.lang.invoke.MethodHandles;
import space.arim.dazzleconf.Configuration;
import space.arim.dazzleconf.core.it.jpms.exported.CustomType;
import space.arim.dazzleconf.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ConfigTest {

	@Test
	public void loadConfig() {
        Configuration<Config> config = Configuration.defaultBuilder(Config.class)
                .lookup(MethodHandles.lookup())
                .addSimpleSerializer(new TypeToken<CustomType>() {}, new CustomType.Serializer())
                .build();
        Config defaults = config.loadDefaults();
        assertFalse(defaults.someOption());
        assertEquals("whatever", defaults.customType().value());
	}

}
