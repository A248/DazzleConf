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
import space.arim.dazzleconf2.backend.DataTree;

import java.util.List;
import java.util.Locale;

public final class LibraryLangEn implements LibraryLang {

    LibraryLangEn() {}

    @Override
    public @NonNull Locale getLocale() {
        return Locale.ENGLISH;
    }

    @Override
    public @NonNull String location() {
        return "location";
    }

    @Override
    public @NonNull String line() {
        return "line";
    }

    @Override
    public @NonNull String backendMessage() {
        return "backend message";
    }

    @Override
    public @NonNull String missingValue() {
        return "No value is configured here.";
    }

    @Override
    public @NonNull String wrongTypeForValue(Object value, String expectedType, String actualType) {
        return "The value < " + value + " > is not a " + expectedType + ", but actually " + actualType;
    }

    @Override
    public @NonNull String mustBeBetween(String value, Number min, Number max) {
        return "The value < " + value + " > must be between " + min + " and " + max;
    }

    @Override
    public @NonNull String malformattedValue(String reason) {
        return "This value is not formatted correctly because " + reason + '.';
    }

    @Override
    public @NonNull String errorIntro() {
        return "We found problems loading the configuration.";
    }

    @Override
    public @NonNull String errorContext() {
        return "Where or how the error happened:";
    }

    @Override
    public @NonNull String moreErrors(int howMany) {
        if (howMany == 1) {
            return "1 more error ...";
        }
        return howMany + " more errors ...";
    }

    @Override
    public @NonNull String text() {
        return "text/string";
    }

    @Override
    public @NonNull String smallInteger() {
        return "small integer";
    }

    @Override
    public @NonNull String integer() {
        return "integer";
    }

    @Override
    public @NonNull String character() {
        return "character";
    }

    @Override
    public @NonNull String decimal() {
        return "decimal";
    }

    @Override
    public @NonNull String list() {
        return "list";
    }

    @Override
    public @NonNull String configurationSection() {
        return "configuration section";
    }

}
