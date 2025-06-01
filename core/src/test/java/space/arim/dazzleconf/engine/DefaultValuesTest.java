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

package space.arim.dazzleconf.engine;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.engine.DefaultValues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultValuesTest {

    @Test
    public void simple() {
        String value = "val";
        DefaultValues<String> defaultValues = DefaultValues.simple(value);
        assertEquals(value, defaultValues.defaultValue());
        assertEquals(value, defaultValues.ifMissing());
        assertThrows(NullPointerException.class, () -> DefaultValues.simple(null));
    }
}
