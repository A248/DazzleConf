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

public class LibraryLangEn implements LibraryLang {

    LibraryLangEn() {}

    @Override
    public String entryPath() {
        return "entry path";
    }

    @Override
    public String line() {
        return "line";
    }

    @Override
    public String backendMessage() {
        return "backend message";
    }

    @Override
    public String causalErrors() {
        return "causal errors";
    }

    @Override
    public String moreErrors() {
        return "more error(s) ...";
    }

    @Override
    public String missingValue() {
        return "No value is configured at this path.";
    }

    @Override
    public String wrongTypeForValue(Object value, String expectedType, String actualType) {
        return "The value < " + value + " > is not a " + expectedType + ", but actually " + actualType;
    }

    @Override
    public String malformattedValue(String reason) {
        return "This value is not formatted correctly because " + reason + '.';
    }

    @Override
    public String errorIntro() {
        return "We found a problem loading the configuration. Reason:";
    }

    @Override
    public String errorContext() {
        return "Where or how the error happened:";
    }
}
