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
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.Locale;

final class DeserInput implements DeserializeInput, LibraryLang.Accessor {

    private final Object object;
    private final Source source;
    private final Context context;

    DeserInput(Object object, Source source, Context context) {
        this.object = object;
        this.source = source;
        this.context = context;
    }

    static final class Source {
        private final DataEntry dataEntry;
        private final String mappedKey;

        Source(DataEntry dataEntry, String mappedKey) {
            this.dataEntry = dataEntry;
            this.mappedKey = mappedKey;
        }
    }

    static final class Context {
        private final LibraryLang libraryLang;
        private final ConfigurationDefinition.ReadOptions readOptions;
        private final KeyPath.Immut mappedPathPrefix;

        Context(LibraryLang libraryLang, ConfigurationDefinition.ReadOptions readOptions, KeyPath.Immut mappedPathPrefix) {
            this.libraryLang = libraryLang;
            this.readOptions = readOptions;
            this.mappedPathPrefix = mappedPathPrefix;
        }
    }

    @Override
    public @NonNull LibraryLang getLibraryLang() {
        return context.libraryLang;
    }

    @Override
    public @NonNull Locale getLocale() {
        return context.libraryLang.getLocale();
    }

    @Override
    public @NonNull Object object() {
        return object;
    }

    @Override
    public @NonNull KeyPath absoluteKeyPath() {
        KeyPath.Mut path = context.mappedPathPrefix.intoMut();
        path.addBack(source.mappedKey);
        return path.intoImmut();
    }

    @Override
    public @NonNull KeyMapper keyMapper() {
        return context.readOptions.keyMapper();
    }

    @Override
    public @NonNull LoadResult<@NonNull String> requireString() {
        Object object = object();
        if (object instanceof String) {
            return LoadResult.of((String) object);
        }
        return throwError(context.libraryLang.wrongTypeForValue(object, String.class));
    }

    @Override
    public @NonNull LoadResult<@NonNull DataTree> requireDataTree() {
        Object object = object();
        if (object instanceof DataTree) {
            return LoadResult.of((DataTree) object);
        }
        return throwError(context.libraryLang.wrongTypeForValue(object, DataTree.class));
    }

    @Override
    public void flagUpdate(@Nullable KeyPath keyPath) {
        if (keyPath == null) {
            keyPath = new KeyPath.Mut();
        }
        KeyPath.Mut keyPathMut = keyPath.intoMut();
        keyPathMut.addFront(source.mappedKey);
        context.readOptions.loadListener().updatedMissingPath(keyPathMut.intoImmut());
    }

    @Override
    public @NonNull DeserializeInput makeChild(@NonNull Object value) {
        return new DeserInput(value, source, context);
    }

    @Override
    public @NonNull ErrorContext buildError(@NonNull CharSequence message) {
        LoadError loadError = new LoadError(message, context.libraryLang);
        // Add entry path
        loadError.addDetail(ErrorContext.ENTRY_PATH, absoluteKeyPath());
        // Add line number
        Integer lineNumber = source.dataEntry.getLineNumber();
        if (lineNumber != null) {
            loadError.addDetail(ErrorContext.LINE_NUMBER, lineNumber);
        }
        return loadError;
    }

    @Override
    public <R> @NonNull LoadResult<R> throwError(@NonNull CharSequence message) {
        return LoadResult.failure(buildError(message));
    }
}
