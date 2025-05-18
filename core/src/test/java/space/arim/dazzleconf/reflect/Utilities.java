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

package space.arim.dazzleconf.reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public final class Utilities {

    private Utilities() {}

    public static <T> void assertEqualsBothWays(T val1, T val2) {
        assertEquals(val1, val2);
        assertEquals(val2, val1);
        assertEquals(val1.hashCode(), val2.hashCode());
    }

    static <T> void assertNotEqualsBothWays(T val1, T val2) {
        assertNotEquals(val1, val2);
        assertNotEquals(val2, val1);
        assertNotEquals(val1.toString(), val2.toString(), "toString should be implemented reasonably");
    }
}
