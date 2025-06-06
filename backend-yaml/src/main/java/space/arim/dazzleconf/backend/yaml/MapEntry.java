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

import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;

final class MapEntry extends ContainerEntry<DataTree.Mut> {

    final Object key;
    Object value;

    MapEntry(DataTree.Mut bucket, int indentLevel, Integer lineNumber, Object key) {
        super(bucket, indentLevel, lineNumber);
        this.key = key;
    }

    @Override
    public void finish() {
        if (value == null) {
            // Can only happen for implicit nulls - see ReadEvents and Backend javadoc
            return;
        }
        DataEntry dataEntry = new DataEntry(value).withComments(commentData);
        if  (lineNumber != null) dataEntry = dataEntry.withLineNumber(lineNumber);
        bucket.set(key, dataEntry);
    }
}
