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

package space.arim.dazzleconf.backend.yaml;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.TestingErrorSource;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneratedDataTest {

    private final StringRoot stringRoot = new StringRoot("");
    private final YamlBackend yamlBackend = new YamlBackend(stringRoot);
    private final ErrorContext.Source errorSource = new TestingErrorSource().makeErrorSource();

    @Test
    public void testCase() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set(false, new DataEntry(new DataTree.Mut()));
        dataTree.set(96.0, new DataEntry(new DataTree.Mut()));
        yamlBackend.write(Backend.Document.simple(dataTree));
        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();
        assertEquals(dataTree, reloaded);
    }
}
