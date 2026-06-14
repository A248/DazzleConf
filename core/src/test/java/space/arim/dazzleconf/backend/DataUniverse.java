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

package space.arim.dazzleconf.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class DataUniverse {

    static final List<Object> SCALAR_KEYS;
    static final List<DataTree.Immut> DATA_TREES;
    static final List<DataList.Immut> DATA_LISTS;
    static final List<DataEntry> DATA_ENTRIES;
    static final List<Map.Entry<Object, DataEntry>> DATA_TREE_ENTRIES;

    static {
        List<Object> scalars = List.of("hello", false, 3);
        List<DataTree.Immut> dataTrees = new ArrayList<>();
        dataTrees.add(new DataTree.Immut());
        {
            DataTree.Mut sampleTree = new DataTree.Mut();
            sampleTree.put("key1", new DataEntry("value"));
            sampleTree.put(2, new DataEntry(true));
            dataTrees.add(sampleTree.intoImmut());
        }
        List<DataList.Immut> dataLists = new ArrayList<>();
        dataLists.add(new DataList.Immut());
        {
            DataList.Mut sampleList = new DataList.Mut();
            sampleList.add(new DataEntry(4));
            sampleList.add(new DataEntry("element"));
            dataLists.add(sampleList.intoImmut());
        }
        List<DataEntry> dataEntries = new ArrayList<>();
        for (Object scalar : scalars) {
            dataEntries.add(new DataEntry(scalar));
        }
        for (DataTree.Immut dataTree : dataTrees) {
            dataEntries.add(new DataEntry(dataTree));
        }
        for (DataList.Immut dataList : dataLists) {
            dataEntries.add(new DataEntry(dataList));
        }
        List<Map.Entry<Object, DataEntry>> dataTreeEntries = new ArrayList<>();
        for (Object scalar : scalars) {
            for (DataEntry entry : dataEntries) {
                dataTreeEntries.add(Map.entry(scalar, entry));
            }
        }
        SCALAR_KEYS = scalars;
        DATA_TREES = dataTrees;
        DATA_LISTS = dataLists;
        DATA_ENTRIES = dataEntries;
        DATA_TREE_ENTRIES = dataTreeEntries;
    }

}
