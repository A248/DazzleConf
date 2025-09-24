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

import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataList;

final class ListEntry extends ContainerEntry<DataList.Mut> {

    private final int index;
    private final Object value;

    ListEntry(DataList.Mut bucket, int indentLevel, Integer lineNumber, int index, Object value) {
        super(bucket, indentLevel, lineNumber);
        this.index = index;
        this.value = value;
    }

    @Override
    public void finish() {
        DataEntry dataEntry = new DataEntry(value).withComments(commentData);
        if (lineNumber != null) dataEntry = dataEntry.withLineNumber(lineNumber);
        bucket.set(index, dataEntry);
    }
}
