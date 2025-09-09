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

import com.sshtools.jini.Data;
import com.sshtools.jini.INI;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataList;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.ArrayDeque;
import java.util.function.Consumer;

final class WriteIni {

    private final ArrayDeque<String> keyStack = new ArrayDeque<>();
    private final INI output = INI.create();
    private Data currentSection = output;

    INI write(Backend.Document document) {
        writeTree(document.data());
        output.setComments(document.comments().getAt(CommentLocation.ABOVE).toArray(new String[0]));
        return output;
    }

    private void writeTree(DataTree dataTree) {
        dataTree.forEach((keyObj, entry) -> {
            String key = keyObj.toString();
            Object value = entry.getValue();
            if (value instanceof DataTree) {
                writeTreeAt(key, (DataTree) value);
            } else if (value instanceof DataList) {
                writeListAt(key, (DataList) value);
            } else {
                // The INI library's put() methods all behave the same way
                currentSection.put(key, value.toString());
            }
        });
    }

    private void writeTreeAt(String key, DataTree dataTree) {
        // First, build the section header
        StringBuilder sectionName = new StringBuilder();
        for (String keyPart : keyStack) {
            sectionName.append(keyPart).append('.');
        }
        sectionName.append(key);

        Data currentSection = this.currentSection;
        keyStack.offerLast(key);
        this.currentSection = output.create(sectionName.toString());
        try {
            writeTree(dataTree);
        } finally {
            this.currentSection = currentSection;
            keyStack.pollLast();
        }
    }

    private void writeListAt(String key, DataList dataList) {
        class WriteList implements Consumer<DataEntry> {
            private int index;

            @Override
            public void accept(DataEntry entry) {
                int index = this.index++;
                String elemKey = key + '.' + index;

                Object elemValue = entry.getValue();
                if (elemValue instanceof DataTree) {
                    writeTreeAt(elemKey, (DataTree) elemValue);
                } else if (elemValue instanceof DataList) {
                    writeListAt(elemKey, (DataList) elemValue);
                } else {
                    currentSection.put(elemKey, elemValue.toString());
                }
            }
        }
        dataList.forEach(new WriteList());
    }

}
