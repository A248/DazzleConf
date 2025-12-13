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

package space.arim.dazzleconf2.backend;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A key mapper which converts lower camel case patterns to kebab case.
 * <p>
 * This is intended for use with YAML and TOML and other formats that use kebab case. For example, a method
 * named "myConfOption" will become "my-conf-option" using this key mapper.
 *
 */
public final class KebabCaseKeyMapper implements KeyMapper {

    /**
     * Creates
     */
    public KebabCaseKeyMapper() {}

    @Override
    public @NonNull CharSequence labelToKey(@NonNull CharSequence label) {
        return CamelCaseMap.toCasedLower(label, '-');
    }
}
