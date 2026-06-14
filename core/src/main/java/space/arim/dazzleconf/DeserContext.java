/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

package space.arim.dazzleconf;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataList;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.backend.Printable;
import space.arim.dazzleconf.engine.DeserializeContext;
import space.arim.dazzleconf.engine.DeserializeInput;
import space.arim.dazzleconf.engine.Interprocessor;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.internals.lang.LibraryLang;

import java.util.Objects;

abstract class DeserContext extends LoadError.Factory implements DeserializeContext {

    final LibraryLang libraryLang;
    final ConfigurationDefinition.ReadOptions readOptions;

    DeserContext(LibraryLang libraryLang, ConfigurationDefinition.ReadOptions readOptions) {
        this.libraryLang = libraryLang;
        this.readOptions = readOptions;
    }

    abstract KeyPath.Mut getPathContribution();

    @Override
    public @NonNull LibraryLang getLibraryLang() {
        return libraryLang;
    }

    @Override
    public @NonNull KeyMapper keyMapper() {
        return readOptions.keyMapper();
    }

    @Override
    public @NonNull KeyPath keyPath() {
        KeyPath.Mut path = readOptions.keyPath().intoMut();
        path.addPath(KeyPath.SequenceBoundary.BACK, getPathContribution());
        return path;
    }

    @Override
    public @NonNull Interprocessor getInterprocessor() {
        return readOptions.getInterprocessor();
    }

    @Override
    public void notifyUpdate(@NonNull KeyPath keyPath, @NonNull UpdateReason updateReason) {
        KeyPath.Mut keyPathMut = keyPath.intoMut();
        keyPathMut.addPath(KeyPath.SequenceBoundary.FRONT, getPathContribution());
        readOptions.notifyUpdate(keyPathMut, updateReason);
    }

    @Override
    public @NonNull ErrorContext buildError(@NonNull Printable message) {
        LoadError loadError = new LoadError(message, libraryLang);
        loadError.addDetail(ErrorContext.ENTRY_PATH, keyPath());
        return loadError;
    }

    @Override
    public @NonNull DeserializeContext deriveContext(@NonNull Object locIdentifier) {
        return new ChildContext(locIdentifier);
    }

    @Override
    public @NonNull DeserializeInput newInputHere(@NonNull DataEntry entry) {
        return new AsInput(entry);
    }

    static final class AtKey extends DeserContext {

        private final String mappedKey;

        AtKey(LibraryLang libraryLang, ConfigurationDefinition.ReadOptions readOptions, String mappedKey) {
            super(libraryLang, readOptions);
            this.mappedKey = mappedKey;
        }

        @Override
        KeyPath.Mut getPathContribution() {
            return new KeyPath.Mut(mappedKey);
        }
    }

    final class ChildContext extends DeserContext {

        private final Object locIdentifier;

        ChildContext(Object locIdentifier) {
            super(DeserContext.this.libraryLang, DeserContext.this.readOptions);
            this.locIdentifier = Objects.requireNonNull(locIdentifier, "locIdentifier");
        }

        @Override
        KeyPath.Mut getPathContribution() {
            KeyPath.Mut path = DeserContext.this.getPathContribution();
            path.addBack(locIdentifier.toString());
            return path;
        }
    }

    final class AsInput extends DeserContext implements DeserializeInput {

        private final DataEntry entry;

        AsInput(DataEntry entry) {
            super(DeserContext.this.libraryLang, DeserContext.this.readOptions);
            this.entry = Objects.requireNonNull(entry, "entry");
        }

        @Override
        KeyPath.Mut getPathContribution() {
            return DeserContext.this.getPathContribution();
        }

        @Override
        public @NonNull DataEntry entry() {
            return entry;
        }

        private <V> @NonNull LoadResult<@NonNull V> requireAs(Class<V> typeClass) {
            Object object = object();
            if (typeClass.isInstance(object)) {
                return LoadResult.of(typeClass.cast(object));
            }
            return throwError(libraryLang.wrongTypeForValue(object, typeClass));
        }

        @Override
        public @NonNull LoadResult<@NonNull String> requireString() {
            return requireAs(String.class);
        }

        @Override
        public @NonNull LoadResult<@NonNull DataTree> requireDataTree() {
            return requireAs(DataTree.class);
        }

        @Override
        public @NonNull LoadResult<@NonNull DataList> requireDataList() {
            return requireAs(DataList.class);
        }

        @Override
        public @NonNull ErrorContext buildError(@NonNull Printable message) {
            ErrorContext errorContext = super.buildError(message);
            Integer lineNumber = entry.getLineNumber();
            if (lineNumber != null) {
                errorContext.addDetail(ErrorContext.LINE_NUMBER, lineNumber);
            }
            return errorContext;
        }
    }
}
