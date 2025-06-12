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

package space.arim.dazzleconf.backend.toml;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.TestingErrorSource;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class GeneratedDataTest {

    private final StringRoot stringRoot = new StringRoot("");
    private final TomlBackend tomlBackend = new TomlBackend(stringRoot);
    private final ErrorContext.Source errorSource = new TestingErrorSource().makeErrorSource();

    @Test
    public void testCase() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("1r1G", new DataEntry(true));
        dataTree.set("Hxh", new DataEntry(0.99000555));
        tomlBackend.write(Backend.Document.simple(dataTree));
        assertDoesNotThrow(tomlBackend.read(errorSource)::getOrThrow, stringRoot.readString());
    }
}
