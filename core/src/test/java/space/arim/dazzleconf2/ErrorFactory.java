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

package space.arim.dazzleconf2;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.Locale;

public class ErrorFactory extends LoadError.Factory {

    private final LibraryLang libraryLang = LibraryLang.loadLang(Locale.ENGLISH);

    @Override
    public @NonNull ErrorContext buildError(@NonNull Printable message) {
        return new LoadError(message, libraryLang);
    }

    @Override
    public @NonNull LibraryLang getLibraryLang() {
        return libraryLang;
    }
}
