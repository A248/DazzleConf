/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
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
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.backend.DefaultKeyMapper;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.reflect.DefaultInstantiator;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationBuildTest {

    public interface Config<A> {}

    private final TypeToken<Config<String>> configType = new TypeToken<>() {};

    @Test
    public void keyMapper() {
        assertNull(Configuration.defaultBuilder(configType).build().getKeyMapper());
        KeyMapper keyMapper = new DefaultKeyMapper();
        assertEquals(keyMapper, Configuration.defaultBuilder(configType).keyMapper(keyMapper).build().getKeyMapper());
    }

    @Test
    public void instantiator() {
        assertInstanceOf(DefaultInstantiator.class, Configuration.defaultBuilder(configType).build().getInstantiator());
        Instantiator instantiator = new DefaultInstantiator(getClass().getClassLoader());
        assertSame(instantiator, Configuration.defaultBuilder(configType).instantiator(instantiator).build().getInstantiator());
    }

    @Test
    public void type() {
        assertSame(configType, Configuration.defaultBuilder(configType).build().getType());
        assertThrows(NullPointerException.class, () -> Configuration.defaultBuilder((Class<?>) null));
        assertThrows(NullPointerException.class, () -> Configuration.defaultBuilder((TypeToken<? extends Object>) null));
        assertThrows(DeveloperMistakeException.class, () -> Configuration.defaultBuilder(Config.class));
    }
}
