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
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.Arrays;

abstract class DeserInput extends LoadError.Factory implements DeserializeInput {

    final Source source;
    final Context context;

    private int childIdx;

    DeserInput(Source source, Context context) {
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

        Context(LibraryLang libraryLang, ConfigurationDefinition.ReadOptions readOptions) {
            this.libraryLang = libraryLang;
            this.readOptions = readOptions;
        }
    }

    abstract KeyPath getPathContribution();

    @Override
    public abstract @NonNull Object object();

    abstract @NonNull DeserializeInput makeChild(@NonNull Object value, int childIdx);

    static class Base extends DeserInput {

        Base(DataEntry dataEntry, String mappedKey, Context context) {
            super(new Source(dataEntry, mappedKey), context);
        }

        @Override
        KeyPath getPathContribution() {
            return new KeyPath.Immut(source.mappedKey);
        }

        @Override
        public @NonNull Object object() {
            return source.dataEntry.getValue();
        }

        @Override
        public @NonNull DeserializeInput makeChild(@NonNull Object value, int childIdx) {
            return new Child(source, context, value, new int[] {childIdx});
        }
    }

    static class Child extends DeserInput {

        private final Object value;
        private final int[] childIdxPath;

        Child(Source source, Context context, Object value, int[] childIdxPath) {
            super(source, context);
            this.value = value;
            this.childIdxPath = childIdxPath;
        }

        @Override
        KeyPath getPathContribution() {
            KeyPath.Mut contribution = new KeyPath.Mut();
            contribution.addBack(source.mappedKey);
            for (int childIdxPathElem : childIdxPath) {
                contribution.addBack("$" + childIdxPathElem);
            }
            return contribution;
        }

        @Override
        public @NonNull Object object() {
            return value;
        }

        @Override
        public @NonNull DeserializeInput makeChild(@NonNull Object value, int childIdx) {
            int[] newChildIdxPath = Arrays.copyOf(childIdxPath, childIdxPath.length + 1);
            newChildIdxPath[childIdxPath.length] = childIdx;
            return new Child(source, context, value, newChildIdxPath);
        }
    }

    @Override
    public @NonNull LibraryLang getLibraryLang() {
        return context.libraryLang;
    }

    @Override
    public @NonNull KeyPath keyPath() {
        KeyPath.Mut path = context.readOptions.keyPath().intoMut();
        path.addPath(KeyPath.SequenceBoundary.BACK, getPathContribution());
        return path;
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
    public void notifyUpdate(@NonNull KeyPath keyPath, @NonNull UpdateReason updateReason) {
        KeyPath.Mut keyPathMut = keyPath.intoMut();
        keyPathMut.addPath(KeyPath.SequenceBoundary.FRONT, getPathContribution());
        context.readOptions.notifyUpdate(keyPathMut, updateReason);
    }

    @Override
    public @NonNull DeserializeInput makeChild(@NonNull Object value) {
        if (!DataEntry.validateValue(value)) {
            throw new IllegalArgumentException("Not a canonical value: " + value);
        }
        return makeChild(value, childIdx++);
    }

    @Override
    public @NonNull ErrorContext buildError(@NonNull Printable message) {
        LoadError loadError = new LoadError(message, context.libraryLang);
        // Add entry path
        loadError.addDetail(ErrorContext.ENTRY_PATH, keyPath());
        // Add line number
        Integer lineNumber = source.dataEntry.getLineNumber();
        if (lineNumber != null) {
            loadError.addDetail(ErrorContext.LINE_NUMBER, lineNumber);
        }
        return loadError;
    }
}
