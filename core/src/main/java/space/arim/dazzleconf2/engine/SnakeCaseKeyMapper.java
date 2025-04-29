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

package space.arim.dazzleconf2.engine;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A key mapper which converts lower camel case patterns to lower snake case.
 * <p>
 * This is intended for use with YAML and TOML and other formats that use lower snake case. For example, a method
 * named "myConfOption" will become "my-conf-option" using this key mapper.
 *
 */
public final class SnakeCaseKeyMapper implements KeyMapper {
    @Override
    public @NonNull CharSequence methodNameToKey(@NonNull String methodName) {
        StringBuilder builder = new StringBuilder();

        int startIndex = 0;
        boolean upperLeadingChar = false;

        for (int n = 0; n < methodName.length(); n++) {
            if (Character.isUpperCase(methodName.charAt(n))) {
                // Found a segment: append everything before us
                if (upperLeadingChar) {
                    builder.append(Character.toLowerCase(methodName.charAt(startIndex)));
                    builder.append(methodName, startIndex + 1, n);
                } else {
                    builder.append(methodName, startIndex, n);
                }
                if (n != 0) {
                    builder.append('-');
                }
                // Prepare for next run
                upperLeadingChar = true;
                startIndex = n;
            }
        }
        if (upperLeadingChar) {
            builder.append(Character.toLowerCase(methodName.charAt(startIndex)));
            builder.append(methodName, startIndex + 1, methodName.length());
        } else {
            builder.append(methodName, startIndex, methodName.length());
        }
        return builder.toString();
    }
}
