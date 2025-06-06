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

package space.arim.dazzleconf2.internals.lang;

import space.arim.dazzleconf2.backend.DataTree;

import java.util.List;

final class ReadMe {

    private ReadMe() {}

    // ------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------

    //
    // List of supported locale implementations
    // PRs for new languages MUST add an entry here!
    //

    //
    // Instructions on adding a new locale:
    // 1. Copy one of the existing files (e.g. LibraryLangEn.java) and rename it
    // 2. Add it here, following the pattern
    //
    static LibraryLang[] availableLanguages() {
        return new LibraryLang[] {

                new LibraryLangAr(null),
                new LibraryLangEn(null)

        };
    }

    // ------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------

    //
    //
    // Private utility methods
    //
    //

    static String displayCanonicalType(LibraryLang lang, Class<?> type) {
        assert !type.isPrimitive() : "Use wrapper type";

        if (type.equals(String.class)) {
            return lang.text();
        }
        if (type.equals(Boolean.class)) {
            return lang.trueFalse();
        }
        if (type.equals(Byte.class)) {
            return lang.smallInteger();
        }
        if (type.equals(Short.class) || type.equals(Integer.class) || type.equals(Long.class)) {
            return lang.integer();
        }
        if (type.equals(Character.class)) {
            return lang.character();
        }
        if (type.equals(Float.class) || type.equals(Double.class)) {
            return lang.decimal();
        }
        if (List.class.isAssignableFrom(type)) {
            return lang.list();
        }
        if (DataTree.class.isAssignableFrom(type)) {
            return lang.configurationSection();
        }
        throw new IllegalArgumentException("Not a canonical type " + type);
    }

}
