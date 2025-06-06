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

package space.arim.dazzleconf.backend.hocon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class NonAsciiCharsTest {

    private final Backend backend = new HoconBackend(new StringRoot(""));
    private final ErrorContext.Source errorSource;

    public NonAsciiCharsTest(@Mock ErrorContext.Source errorSource) {
        this.errorSource = errorSource;
    }

    @Test
    public void writeReadHanzi() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("hello", new DataEntry("篡"));
        dataTree.set("there", new DataEntry("㖗"));
        backend.write(Backend.Document.simple(dataTree));
        assertEquals(dataTree, backend.read(errorSource).getOrThrow().data());
    }

    @Test
    public void writeReadOther() {
        // Please, someone rename the test if they know what language or script these characters come from
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("unknown", new DataEntry("뗖"));
        dataTree.set("unknown2", new DataEntry("췒"));
        backend.write(Backend.Document.simple(dataTree));
        assertEquals(dataTree, backend.read(errorSource).getOrThrow().data());
    }
}
