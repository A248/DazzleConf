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

final class CamelCaseMap {

    private CamelCaseMap() {}

    static CharSequence toCasedLower(CharSequence label, char separator) {
        StringBuilder builder = new StringBuilder();
        int startAppend = 0;

        for (int n = 0; n < label.length(); n++) {
            char currentChar = label.charAt(n);
            if (Character.isUpperCase(currentChar)) {
                // Found a segment: append everything before us
                builder.append(label, startAppend, n);
                if (n != 0) {
                    builder.append(separator);
                }
                builder.append(Character.toLowerCase(currentChar));
                startAppend = n + 1;
            }
        }
        builder.append(label, startAppend, label.length());
        return builder.toString();
    }
}
