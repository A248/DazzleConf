/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.Configuration;
import space.arim.dazzleconf.ConfigurationDefinition;
import space.arim.dazzleconf.LoadResult;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.DefaultKeyMapper;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.Interprocessor;
import space.arim.dazzleconf.engine.UpdateReason;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.*;

public class OptionalLiaisonTest {

    public interface Config {

        default Optional<String> presentByDefault() {
            return Optional.of("present");
        }

        default Optional<String> missingByDefault() {
            return Optional.empty();
        }

        default OptionalInt presentByDefaultInt() {
            return OptionalInt.of(4);
        }

        default OptionalInt missingByDefaultInt() {
            return OptionalInt.empty();
        }

        default OptionalLong presentByDefaultLong() {
            return OptionalLong.of(Long.MAX_VALUE);
        }

        default OptionalLong missingByDefaultLong() {
            return OptionalLong.empty();
        }

        default OptionalDouble presentByDefaultDouble() {
            return OptionalDouble.of(-3.3);
        }

        default OptionalDouble missingByDefaultDouble() {
            return OptionalDouble.empty();
        }
    }

    private Configuration<Config> config;

    @BeforeEach
    public void setConfig() {
        config = Configuration.defaultBuilder(Config.class).build();
    }

    @Test
    public void loadDefaults() {
        Config defaults = config.loadDefaults();
        assertEquals(Optional.of("present"), defaults.presentByDefault());
        assertEquals(Optional.empty(), defaults.missingByDefault());
        assertEquals(OptionalInt.of(4), defaults.presentByDefaultInt());
        assertEquals(OptionalInt.empty(), defaults.missingByDefaultInt());
        assertEquals(OptionalLong.of(Long.MAX_VALUE), defaults.presentByDefaultLong());
        assertEquals(OptionalLong.empty(), defaults.missingByDefaultLong());
        assertTrue(defaults.presentByDefaultDouble().orElse(1.0) < 0.0);
        assertFalse(defaults.missingByDefaultDouble().isPresent());
    }

    @Test
    public void loadMissingValues() {
        Config loaded = config.readFrom(new DataTree.Immut()).getOrThrow();
        assertEquals(Optional.empty(), loaded.presentByDefault());
        assertEquals(Optional.empty(), loaded.missingByDefault());
        assertEquals(OptionalInt.empty(), loaded.presentByDefaultInt());
        assertEquals(OptionalInt.empty(), loaded.missingByDefaultInt());
        assertEquals(OptionalLong.empty(), loaded.presentByDefaultLong());
        assertEquals(OptionalLong.empty(), loaded.missingByDefaultLong());
        assertFalse(loaded.presentByDefaultDouble().isPresent());
        assertFalse(loaded.missingByDefaultDouble().isPresent());
    }

    @Test
    public void loadPresentValue() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.put("missingByDefault", new DataEntry("set here"));
        dataTree.put("missingByDefaultInt", new DataEntry(6));
        dataTree.put("missingByDefaultLong", new DataEntry(Long.MIN_VALUE));
        dataTree.put("missingByDefaultDouble", new DataEntry(14.7d));
        Config loaded = config.readFrom(dataTree).getOrThrow();
        assertEquals(Optional.empty(), loaded.presentByDefault());
        assertEquals(Optional.of("set here"), loaded.missingByDefault());
        assertEquals(OptionalInt.empty(), loaded.presentByDefaultInt());
        assertEquals(OptionalInt.of(6), loaded.missingByDefaultInt());
        assertEquals(OptionalLong.empty(), loaded.presentByDefaultLong());
        assertEquals(OptionalLong.of(Long.MIN_VALUE), loaded.missingByDefaultLong());
        assertFalse(loaded.presentByDefaultDouble().isPresent());
        assertTrue(loaded.missingByDefaultDouble().orElse(-1.0) > 0.0);
    }

    @Test
    public void writePresentValue() {
        DataTree.Mut output = new DataTree.Mut();
        config.writeTo(new Config() {
            @Override
            public Optional<String> presentByDefault() {
                return Optional.empty();
            }

            @Override
            public Optional<String> missingByDefault() {
                return Optional.of("set here");
            }

            @Override
            public OptionalInt presentByDefaultInt() {
                return OptionalInt.empty();
            }

            @Override
            public OptionalInt missingByDefaultInt() {
                return OptionalInt.of(6);
            }

            @Override
            public OptionalLong presentByDefaultLong() {
                return OptionalLong.empty();
            }

            @Override
            public OptionalLong missingByDefaultLong() {
                return OptionalLong.of(Long.MIN_VALUE);
            }

            @Override
            public OptionalDouble presentByDefaultDouble() {
                return OptionalDouble.empty();
            }

            @Override
            public OptionalDouble missingByDefaultDouble() {
                return OptionalDouble.of(14.7);
            }
        }, output);
        assertEquals(4, output.size(), () -> "Received " + output);
        assertNull(output.get("presentByDefault"));
        assertEquals(new DataEntry("set here"), output.get("missingByDefault"));
        assertNull(output.get("presentByDefaultInt"));
        assertEquals(new DataEntry(6), output.get("missingByDefaultInt"));
        assertNull(output.get("presentByDefaultLong"));
        assertEquals(new DataEntry(Long.MIN_VALUE), output.get("missingByDefaultLong"));
        assertNull(output.get("presentByDefaultDouble"));
        DataEntry missingByDefaultDouble = output.get("missingByDefaultDouble");
        assertNotNull(missingByDefaultDouble);
        assertTrue(((double) missingByDefaultDouble.getValue()) > 0.0);
    }

    @Test
    public void readUpdateSkipAbsent() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.put("missingByDefault", new DataEntry("set here"));
        dataTree.put("presentByDefaultInt", new DataEntry(-2));
        dataTree.put("missingByDefaultLong", new DataEntry(Long.MIN_VALUE));
        DataTree.Immut dataSnapshot = dataTree.intoImmut();
        dataTree.put("presentByDefaultDouble", new DataEntry(9.7f));
        var readUpdateOptions = new ConfigurationDefinition.ReadWithUpdateOptions() {

            @Override
            public void notifyUpdate(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {}

            @Override
            public @NonNull KeyMapper keyMapper() {
                return new DefaultKeyMapper();
            }

            @Override
            public @NonNull KeyPath keyPath() {
                return KeyPath.empty();
            }

            @Override
            public @NonNull Interprocessor getInterprocessor() {
                return Interprocessor.DEFAULT;
            }
        };
        LoadResult<Config> loadResult = config.readWithUpdate(dataTree, readUpdateOptions);
        assertDoesNotThrow(loadResult::getOrThrow);
        assertEquals(4, dataTree.size());
        assertNotNull(dataTree.get("presentByDefaultDouble"));
        dataTree.remove("presentByDefaultDouble");
        assertEquals(dataSnapshot, dataTree.intoImmut());
    }
}
