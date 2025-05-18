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

package space.arim.dazzleconf.engine.liaison;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.liaison.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumbersTest {

    public interface Config {

        default byte byteValue() {
            return 1;
        }

        default short shortValue() {
            return 2;
        }

        default int intValue() {
            return 3;
        }

        default long longValue() {
            return 4L;
        }

        default float floatValue() {
            return 5.0f;
        }

        default double doubleValue() {
            return 6.0;
        }
    }

    @Test
    public void loadDefaults() {
        Config defaults = Configuration.defaultBuilder(Config.class).build().loadDefaults();
        assertEquals(1, defaults.byteValue());
        assertEquals(2, defaults.shortValue());
        assertEquals(3, defaults.intValue());
        assertEquals(4, defaults.longValue());
        assertEquals(5.0, defaults.floatValue());
        assertEquals(6.0, defaults.doubleValue());
    }

    @Test
    public void loadDefaultsMissingValues() {
        Config defaultsIfMissing = Configuration
                .defaultBuilder(Config.class)
                .build()
                .readFrom(new DataTree.Immut())
                .getOrThrow();
        assertEquals(1, defaultsIfMissing.byteValue());
        assertEquals(2, defaultsIfMissing.shortValue());
        assertEquals(3, defaultsIfMissing.intValue());
        assertEquals(4, defaultsIfMissing.longValue());
        assertEquals(5.0, defaultsIfMissing.floatValue());
        assertEquals(6.0, defaultsIfMissing.doubleValue());
    }

    public interface AnnotatedDefaults {
        @ByteDefault(value = 1, ifMissing = -1)
        byte byteValue();

        @ShortDefault(value = 2, ifMissing = -2)
        short shortValue();

        @IntegerDefault(value = 3, ifMissing = -3)
        int intValue();

        @LongDefault(value = 4, ifMissing = -4L)
        long longValue();

        @FloatDefault(value = 5.0f, ifMissing = -5.0f)
        float floatValue();

        @DoubleDefault(value = 6.0, ifMissing = -6.0)
        double doubleValue();

    }

    @Test
    public void loadDefaultsAnnotated() {
        AnnotatedDefaults defaults = Configuration.defaultBuilder(AnnotatedDefaults.class).build().loadDefaults();
        assertEquals(1, defaults.byteValue());
        assertEquals(2, defaults.shortValue());
        assertEquals(3, defaults.intValue());
        assertEquals(4, defaults.longValue());
        assertEquals(5.0, defaults.floatValue());
        assertEquals(6.0, defaults.doubleValue());
    }

    @Test
    public void loadDefaultsMissingValuesAnnotated() {
        AnnotatedDefaults defaultsIfMissing = Configuration
                .defaultBuilder(AnnotatedDefaults.class)
                .build()
                .readFrom(new DataTree.Immut())
                .getOrThrow();
        assertEquals(-1, defaultsIfMissing.byteValue());
        assertEquals(-2, defaultsIfMissing.shortValue());
        assertEquals(-3, defaultsIfMissing.intValue());
        assertEquals(-4, defaultsIfMissing.longValue());
        assertEquals(-5.0, defaultsIfMissing.floatValue());
        assertEquals(-6.0, defaultsIfMissing.doubleValue());
    }
}
