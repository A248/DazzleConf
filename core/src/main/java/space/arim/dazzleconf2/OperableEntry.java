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

import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.OperableObject;
import space.arim.dazzleconf2.engine.UpdateListener;
import space.arim.dazzleconf2.translation.LibraryLang;
import space.arim.dazzleconf2.translation.LibraryLangKey;

import java.util.ArrayDeque;

final class OperableEntry implements OperableObject {

    private DataTree.Entry currentEntry;

    private final LibraryLang libraryLang;
    private final UpdateListener updateListener;
    private final ArrayDeque<String> path = new ArrayDeque<>();

    OperableEntry(LibraryLang libraryLang, UpdateListener updateListener) {
        this.libraryLang = libraryLang;
        this.updateListener = updateListener;
    }

    @Override
    public Object object() {
        return null;
    }

    @Override
    public void flagUpdate() {

    }

    @Override
    public OperableObject makeChild(Object value) {
        return null;
    }

    @Override
    public ErrorContext buildError() {
        LoadError loadError = new LoadError("Error loading configuration value");
        return null;
    }

    @Override
    public <R> LoadResult<R> throwError() {
        return null;
    }

    private LoadError buildError(LibraryLangKey message) {

    }
}
