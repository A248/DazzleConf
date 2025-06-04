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
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OptionalTest {

    public interface Config {

        default Optional<String> presentByDefault() {
            return Optional.of("present");
        }

        default Optional<String> missingByDefault() {
            return Optional.empty();
        }
    }

    @Test
    public void loadDefaults() {
        Config defaults = Configuration.defaultBuilder(Config.class).build().loadDefaults();
        assertEquals(Optional.of("present"), defaults.presentByDefault());
        assertEquals(Optional.empty(), defaults.missingByDefault());
    }

    @Test
    public void loadMissingValues() {
        Configuration<Config> config = Configuration.defaultBuilder(Config.class).build();
        Config loaded = config.readFrom(new DataTree.Immut()).getOrThrow();
        assertEquals(Optional.empty(), loaded.presentByDefault());
        assertEquals(Optional.empty(), loaded.missingByDefault());
    }

    @Test
    public void loadPresentValue() {
        Configuration<Config> config = Configuration.defaultBuilder(Config.class).build();
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("missingByDefault", new DataEntry("set here"));
        Config loaded = config.readFrom(dataTree).getOrThrow();
        assertEquals(Optional.empty(), loaded.presentByDefault());
        assertEquals(Optional.of("set here"), loaded.missingByDefault());
    }

    @Test
    public void writePresentValue() {
        Configuration<Config> config = Configuration.defaultBuilder(Config.class).build();

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
        }, output);
        assertNull(output.get("presentByDefault"));
        assertEquals(new DataEntry("set here"), output.get("missingByDefault"));

        DataTree.Mut expected = new DataTree.Mut();
        expected.set("missingByDefault", new DataEntry("set here"));
        assertEquals(expected, output);
    }
}
