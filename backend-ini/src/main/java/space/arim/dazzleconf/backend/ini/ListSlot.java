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

package space.arim.dazzleconf.backend.ini;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataList;

final class ListSlot implements ContainerSlot {

    private final DataList.Mut dataList;
    private final int index;

    ListSlot(DataList.Mut dataList, int index) {
        this.dataList = dataList;
        this.index = index;
    }

    @Override
    public @Nullable DataEntry getIfPresent() {
        if (index < dataList.size()) {
            return dataList.get(index);
        }
        return null;
    }

    private static final DataEntry UNSET = new DataEntry("unset");

    @Override
    public void set(@NonNull DataEntry element) {
        while (dataList.size() <= index) {
            dataList.add(UNSET);
        }
        dataList.set(index, element);
    }
}
