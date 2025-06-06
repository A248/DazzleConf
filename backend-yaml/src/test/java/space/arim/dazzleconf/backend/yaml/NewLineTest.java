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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import space.arim.dazzleconf.TestingErrorSource;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NewLineTest {

    private final StringRoot stringRoot = new StringRoot("");
    private final YamlBackend yamlBackend = new YamlBackend(stringRoot);
    private final ErrorContext.Source errorSource = new TestingErrorSource().makeErrorSource();

    @ParameterizedTest
    @ValueSource(strings = {
            "\n", "\n\n", "\nHi", "\n\nHi", "Hi\n", "Hi\n\n",
            "\nHi\nHo", "\n\nHi\nHo", "\nHi\n\nHo", "Hi\nHo", "Hi\nHo\n",
            "Hi\nHo\n\n", "Hi\n\nHo\n\n", "Hi\n\nHo", "Hi\n\nHo\n"
    })
    public void newLineInValue(String value) {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("Z", new DataEntry(value));
        yamlBackend.write(Backend.Document.simple(dataTree));
        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();
        DataEntry reloadedEntry = reloaded.get("Z");
        assertNotNull(reloadedEntry);
        assertEquals(value, reloadedEntry.getValue());
        assertEquals(dataTree, reloaded);
    }
}
