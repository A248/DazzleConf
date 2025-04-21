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

package space.arim.dazzleconf2.internals;

import java.util.Locale;

/**
 * Language translation for the library itself. If you want to implement this and provide a translation for your
 * locale, <b>please</b> PR it back to the main repository.
 */
public interface LibraryLang {

    String entryPath();

    String line();

    String backendMessage();

    @Deprecated
    String causalErrors();

    @Deprecated
    String moreErrors();

    String missingValue();

    String wrongTypeForValue(Object value, String expectedType, String actualType);

    String malformattedValue(String reason);

    String errorIntro();

    String errorContext();

    static LibraryLang loadLang(Locale locale) {
        return new LibraryLangEn();
    }

}
