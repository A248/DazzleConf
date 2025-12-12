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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataList;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.UpdateListener;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.reflect.TypeToken;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class SetLiaisonTest {

    interface Config<V> {

        Set<V> values();

    }

    @Test
    public void loadRegular(@Mock UpdateListener updateListener) {
        DataTree.Mut dataTree = new DataTree.Mut();
        DataList.Mut dataList = new DataList.Mut();
        dataList.add(new DataEntry("friends"));
        dataList.add(new DataEntry("are"));
        dataList.add(new DataEntry("good"));
        dataTree.put("values", new DataEntry(dataList));

        var configuration = StringType.configuration(new TypeToken<Config<String>>() {});
        Set<String> loaded = configuration.readFrom(dataTree, updateListener).getOrThrow().values();
        assertEquals(Set.of("friends", "are", "good"), loaded);
        verifyNoInteractions(updateListener);
    }

    @Test
    public void loadDuplicatesFromInput(@Mock UpdateListener updateListener) {
        DataTree.Mut dataTree = new DataTree.Mut();
        DataList.Mut dataList = new DataList.Mut();
        dataList.add(new DataEntry("whitespace"));
        dataList.add(new DataEntry("whitespace"));
        dataList.add(new DataEntry("is    "));
        dataList.add(new DataEntry("    bad "));
        dataTree.put("values", new DataEntry(dataList));

        var configuration = StringType.configuration(new TypeToken<Config<TrimOnDeser>>() {});
        Set<TrimOnDeser> loaded = configuration.readFrom(dataTree, updateListener).getOrThrow().values();
        assertEquals(Set.of(new TrimOnDeser("whitespace"), new TrimOnDeser("is"), new TrimOnDeser("bad")), loaded);
        verify(updateListener).notifyUpdate(new KeyPath.Immut("values"), UpdateReason.OTHER);
    }

    @Test
    public void loadDuplicatesOnDeser(@Mock UpdateListener updateListener) {
        DataTree.Mut dataTree = new DataTree.Mut();
        DataList.Mut dataList = new DataList.Mut();
        dataList.add(new DataEntry("whitespace"));
        dataList.add(new DataEntry("is    "));
        dataList.add(new DataEntry("bad"));
        dataList.add(new DataEntry("    bad "));
        dataTree.put("values", new DataEntry(dataList));

        var configuration = StringType.configuration(new TypeToken<Config<TrimOnDeser>>() {});
        Set<TrimOnDeser> loaded = configuration.readFrom(dataTree, updateListener).getOrThrow().values();
        verify(updateListener).notifyUpdate(new KeyPath.Immut("values"), UpdateReason.OTHER);
        assertEquals(Set.of(new TrimOnDeser("whitespace"), new TrimOnDeser("is"), new TrimOnDeser("bad")), loaded);
    }

    @Test
    public void serDuplicates() {
        Config<ClipOnSer> config = () -> {
            LinkedHashSet<ClipOnSer> set = new LinkedHashSet<>();
            set.add(new ClipOnSer("parts"));
            set.add(new ClipOnSer("par"));
            set.add(new ClipOnSer("pa"));
            return set;
        };
        DataTree.Mut dataTree = new DataTree.Mut();
        StringType.configuration(new TypeToken<Config<ClipOnSer>>() {}).writeTo(config, dataTree);
        DataEntry dataEntry = dataTree.get("values");
        assumeTrue(dataEntry != null && dataEntry.getValue() instanceof DataList);
        DataList dataList = (DataList) dataEntry.getValue();
        assertEquals(3, dataList.size());
        assertEquals("par", dataList.get(0).getValue());
        assertEquals("par", dataList.get(1).getValue());
        assertEquals("pa", dataList.get(2).getValue());
    }
}
