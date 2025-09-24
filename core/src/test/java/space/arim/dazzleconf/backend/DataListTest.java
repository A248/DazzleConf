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

package space.arim.dazzleconf.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static space.arim.dazzleconf.Utilities.assertEqualsBothWays;
import static space.arim.dazzleconf.Utilities.assertNotEqualsBothWays;

public class DataListTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void empty(boolean mutable) {
        DataList dataList = mutable ? new DataList.Mut() : new DataList.Immut();
        assertTrue(dataList.isEmpty());
        assertEquals(0, dataList.size());
        assertThrows(IndexOutOfBoundsException.class, () -> dataList.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> dataList.get(0));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void populateEntries(boolean mutable) {
        DataList dataList;
        {
            DataList.Mut mutList = new DataList.Mut();
            mutList.add(new DataEntry(1));
            mutList.add(new DataEntry("hello"));
            dataList = mutable ? mutList : mutList.intoImmut();
        }
        assertFalse(dataList.isEmpty());
        assertEquals(2, dataList.size());
        assertEquals(new DataEntry(1), dataList.get(0));
        assertEquals(new DataEntry("hello"), dataList.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> dataList.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> dataList.get(2));
        List<Object> values = new ArrayList<>();
        dataList.forEach(entry -> values.add(entry.getValue()));
        assertEquals(List.of(1, "hello"), values);
    }

    @Test
    public void equality() {
        DataList.Mut list1 = new DataList.Mut();
        list1.add(new DataEntry(1));
        list1.add(new DataEntry("hello"));
        DataList.Mut list2 = new DataList.Mut();
        list2.add(new DataEntry(1));
        list2.add(new DataEntry("hello"));
        DataList.Immut list3 = list1.intoImmut();
        DataList.Immut list4 = list1.intoImmut();
        DataList.Mut list5 = new DataList.Mut();
        list5.add(new DataEntry(1));
        // Mut and Mut
        assertEqualsBothWays(list1, list1);
        assertEqualsBothWays(list1, list2);
        // Immut and Immut
        assertEqualsBothWays(list3, list3);
        assertEqualsBothWays(list3, list4);
        // Crossover
        assertEqualsBothWays(list1, list3);
        // Not equal
        assertNotEqualsBothWays(list5, null);
        assertNotEqualsBothWays(list5, new Object());
        assertNotEqualsBothWays(list1, list5);
        assertNotEqualsBothWays(list3, list5);
    }

    @Test
    public void modify() {
        DataList.Mut dataList = new DataList.Mut();
        dataList.add(new DataEntry(1));
        dataList.add(new DataEntry("hello"));
        // set
        dataList.set(0, new DataEntry(2));
        List<Object> values = new ArrayList<>();
        dataList.forEach(entry -> values.add(entry.getValue()));
        assertEquals(List.of(2, "hello"), values);
        // clear
        dataList.clear();
        assertTrue(dataList.isEmpty());
        assertEquals(0, dataList.size());
        List<Object> valuesNow = new ArrayList<>();
        dataList.forEach(entry -> valuesNow.add(entry.getValue()));
        assertEquals(List.of(), valuesNow);
    }
}
