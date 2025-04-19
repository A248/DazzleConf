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

package space.arim.dazzleconf2.translation;

public class LibraryLangEn implements LibraryLang {

    LibraryLangEn() {}

    @Override
    public String entryPath() {
        return "entry path";
    }

    @Override
    public String lineNumber() {
        return "line number";
    }

    @Override
    public String backendMessage() {
        return "backend message";
    }

    @Override
    public String auxiliaryErrors() {
        return "auxiliary errors";
    }

    @Override
    public String wrongTypeForValue(String expected, String actual) {
        return "This value is supposed to be a " + expected + ", but it was actually " + actual;
    }

    @Override
    public String malformattedValue(String reason) {
        return "This value is not formatted correctly because " + reason + '.';
    }

    @Override
    public String errorIntro() {
        return "Encountered a user problem loading the configuration. Reason:";
    }

    @Override
    public String errorContext() {
        return "Where or how the error happened:";
    }
}
