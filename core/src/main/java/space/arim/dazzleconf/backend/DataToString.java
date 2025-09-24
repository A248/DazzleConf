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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class DataToString {

    private final StringBuilder output;
    private final Set<Object> parentContainers = Collections.newSetFromMap(new IdentityHashMap<>());

    DataToString(StringBuilder output) {
        this.output = output;
    }

    static String implToString(Object implementor) {
        StringBuilder output = new StringBuilder();
        DataToString.Scope topScope = new DataToString(output).new Scope(0);
        if (implementor instanceof DataEntry) {
            ((DataEntry) implementor).toString(topScope);
        } else {
            topScope.valueToString(implementor);
        }
        return output.toString();
    }

    final class Scope {

        private final int indent;

        Scope(int indent) {
            this.indent = indent;
        }

        private void writeNewLine() {
            output.append('\n');
            for (int n = 0; n < indent; n++) {
                output.append(' ');
            }
        }

        void valueToString(Object value) {
            if (value instanceof String) {
                new EscapeString((String) value).printTo(output);

            } else if (value instanceof DataList) {
                DataList dataList = (DataList) value;
                if (parentContainers.add(dataList)) {
                    try {
                        dataList.toString(this);
                    } finally {
                        parentContainers.remove(dataList);
                    }
                } else {
                    output.append("<circular list>");
                }

            } else if (value instanceof DataTree) {
                DataTree dataTree = (DataTree) value;
                if (parentContainers.add(dataTree)) {
                    try {
                        dataTree.toString(this);
                    } finally {
                        parentContainers.remove(dataTree);
                    }
                } else {
                    output.append("<circular map>");
                }

            } else {
                output.append(value);
            }
        }

        void listToString(List<DataEntry> list) {
            output.append('[');
            if (!list.isEmpty()) {
                new Scope(indent + 2).listElementsToString(list);
                writeNewLine();
            }
            output.append(']');
        }

        private void listElementsToString(List<DataEntry> list) {
            for (DataEntry elem : list) {
                writeNewLine();
                elem.toString(this);
                output.append(',');
            }
        }

        void mapToString(Map<Object, DataEntry> map) {
            output.append('{');
            if (!map.isEmpty()) {
                new Scope(indent + 2).mapElementsToString(map);
                writeNewLine();
            }
            output.append('}');
        }

        private void mapElementsToString(Map<Object, DataEntry> map) {
            for (Map.Entry<Object, DataEntry> mapEntry : map.entrySet()) {
                Object key = mapEntry.getKey();
                DataEntry entry = mapEntry.getValue();

                writeNewLine();
                output.append(key);
                output.append('=');
                entry.toString(this);
                output.append(',');
            }
        }

        void append(String str) {
            output.append(str);
        }

        void append(char ch) {
            output.append(ch);
        }
    }
}
