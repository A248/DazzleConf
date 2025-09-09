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

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.backend.Printable;

import java.net.URL;

/// All translatable messages are defined by this interface
public interface LanguageMessages {

    @NonNull String location();

    @NonNull String line();

    @NonNull String backendMessage();

    @NonNull String syntaxLinter();

    @NonNull String failed();

    @NonNull String missingValue();

    @NonNull Printable wrongTypeForValue(Object value, String expectedType, String actualType);

    @NonNull Printable mustBeBetween(String value, Number min, Number max);

    @NonNull Printable notAccepted(@NonNull String value, @NonNull String[] permitted);

    @NonNull String forExample();

    @NonNull String badValue();

    @NonNull String more(String what);

    @NonNull String errorIntro();

    @NonNull String errorContext();

    @NonNull String trueFalse();

    @NonNull String text();

    @NonNull String smallInteger();

    @NonNull String integer();

    @NonNull String character();

    @NonNull String decimal();

    @NonNull String list();

    @NonNull String configurationSection();

    @NonNull String syntaxInvalidPleaseTryAt(@NonNull URL url);

    @NonNull String syntax();

    @NonNull String otherReason();

    @NonNull String yamlNotAMap();

    @NonNull String tomlDateType();

}
