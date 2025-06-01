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
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.backend.Printable;

import java.util.Arrays;
import java.util.Locale;

import static space.arim.dazzleconf2.backend.Printable.preBuilt;

public final class LibraryLangEn extends LibraryLang.Base {

    LibraryLangEn(@Nullable Locale overrideLocale) {
        super(overrideLocale);
    }

    @Override
    protected Locale getActualLocale() {
        return Locale.ENGLISH;
    }

    @Override
    public @NonNull LibraryLang pretendToUseLocale(@NonNull Locale usingLocale) {
        return new LibraryLangEn(usingLocale);
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
        return "No value is configured here. However, a value is required.";
    }

    @Override
    public @NonNull Printable wrongTypeForValue(Object value, String expectedType, String actualType) {
        return preBuilt("The value < " + value + " > is a " + expectedType + ", but it should be a " + actualType + '.');
    }

    @Override
    public @NonNull Printable mustBeBetween(String value, Number min, Number max) {
        return preBuilt("The value must be between " + min + " and " + max + ". However, it was " + value + '.');
    }

    @Override
    public @NonNull Printable notAccepted(@NonNull String value, @NonNull String[] permitted) {
        return preBuilt("The selected value " + value + " is not accepted. It should be one of " + Arrays.toString(permitted) + '.');
    }

    @Override
    public @NonNull String forExample() {
        return "for example";
    }

    @Override
    public @NonNull String badValue() {
        return "This value is not chosen correctly.";
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
    public @NonNull String more(int howMany) {
        return howMany + " more...";
    }

    @Override
    public @NonNull String trueFalse() {
        return "true/false value";
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
