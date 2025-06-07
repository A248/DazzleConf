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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;

@ExtendWith(MockitoExtension.class)
public class NonStringKeysTest {

    @Test
    public void integerKeysSupported(@Mock Backend.ReadRequest readRequest) {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set(1, new DataEntry("hello"));
        dataTree.set("option", new DataEntry(false));
        DataTree.Mut subTree = new DataTree.Mut();
        dataTree.set(-5, new DataEntry(subTree));
        subTree.set("hi", new DataEntry("yes"));
        subTree.set(1, new DataEntry("no"));

    }

    @Test
    public void butListsAsKeysAreNot() {

    }
}
