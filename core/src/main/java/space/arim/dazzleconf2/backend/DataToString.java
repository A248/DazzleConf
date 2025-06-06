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

package space.arim.dazzleconf2.backend;

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
            } else if (value instanceof DataTree) {
                ((DataTree) value).toString(this);
            } else if (value instanceof List) {
                listToString((List<?>) value);
            } else {
                output.append(value);
            }
        }

        void listToString(List<?> list) {
            output.append('[');
            if (!list.isEmpty()) {
                Scope subScope = new Scope(indent + 2);
                if (parentContainers.add(list)) {
                    try {
                        subScope.listElementsToString(list);
                    } finally {
                        parentContainers.remove(list);
                    }
                    writeNewLine(); // Place trailing ']' on appropriate line
                } else {
                    output.append("<circular list>");
                }
            }
            output.append(']');
        }

        private void listElementsToString(List<?> list) {
            for (Object elem : list) {
                writeNewLine();
                if (elem instanceof DataEntry) {
                    ((DataEntry) elem).toString(this);
                } else {
                    // Shouldn't happen, but maybe callers are being funny
                    output.append("ILLEGAL");
                    output.append('{');
                    valueToString(elem);
                    output.append('}');
                }
                output.append(',');
            }
        }

        void mapToString(Map<Object, DataEntry> map) {
            output.append('{');
            if (!map.isEmpty()) {
                Scope subScope = new Scope(indent + 2);
                if (parentContainers.add(map)) {
                    try {
                        subScope.mapElementsToString(map);
                    } finally {
                        parentContainers.remove(map);
                    }
                    writeNewLine(); // Place trailing '}' on appropriate line
                } else {
                    output.append("<circular map>");
                }
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
