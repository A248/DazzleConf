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

package space.arim.dazzleconf;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataList;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.backend.Printable;
import space.arim.dazzleconf.engine.DeserializeInput;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.internals.lang.LibraryLang;

import java.util.Objects;

abstract class DeserInput extends LoadError.Factory implements DeserializeInput {

    private final DataEntry entry;
    private final LibraryLang libraryLang;
    private final ConfigurationDefinition.ReadOptions readOptions;

    DeserInput(DataEntry entry, LibraryLang libraryLang, ConfigurationDefinition.ReadOptions readOptions) {
        Objects.requireNonNull(entry, "entry");
        this.entry = entry;
        this.libraryLang = libraryLang;
        this.readOptions = readOptions;
    }

    abstract KeyPath.Mut getPathContribution();

    @Override
    public @NonNull DataEntry entry() {
        return entry;
    }

    static class Base extends DeserInput {

        private final String mappedKey;

        Base(DataEntry entry, LibraryLang libraryLang, ConfigurationDefinition.ReadOptions readOptions,
             String mappedKey) {
            super(entry, libraryLang, readOptions);
            this.mappedKey = mappedKey;
        }

        @Override
        KeyPath.Mut getPathContribution() {
            return new KeyPath.Mut(mappedKey);
        }
    }

    private static final class Child extends DeserInput {

        private final DeserInput parent;
        private final Object locIdentifier;

        Child(DataEntry entry, LibraryLang libraryLang, ConfigurationDefinition.ReadOptions readOptions,
              DeserInput parent, Object locIdentifier) {
            super(entry, libraryLang, readOptions);
            this.parent = parent;
            this.locIdentifier = locIdentifier;
        }

        @Override
        KeyPath.Mut getPathContribution() {
            KeyPath.Mut contribution = parent.getPathContribution();
            contribution.addBack(locIdentifier.toString());
            return contribution;
        }
    }

    @Override
    public @NonNull LibraryLang getLibraryLang() {
        return libraryLang;
    }

    @Override
    public @NonNull KeyPath keyPath() {
        KeyPath.Mut path = readOptions.keyPath().intoMut();
        path.addPath(KeyPath.SequenceBoundary.BACK, getPathContribution());
        return path;
    }

    @Override
    public @NonNull KeyMapper keyMapper() {
        return readOptions.keyMapper();
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
    public void notifyUpdate(@NonNull KeyPath keyPath, @NonNull UpdateReason updateReason) {
        KeyPath.Mut keyPathMut = keyPath.intoMut();
        keyPathMut.addPath(KeyPath.SequenceBoundary.FRONT, getPathContribution());
        readOptions.notifyUpdate(keyPathMut, updateReason);
    }

    @Override
    // 2.0.0-M3: Remove this section
    public @NonNull DeserializeInput makeChild(@NonNull Object value) {
        return makeChild(new DataEntry(value), childIdx++);
    }
    private int childIdx;

    @Override
    public @NonNull DeserializeInput makeChild(@NonNull DataEntry entry, @NonNull Object locIdentifier) {
        Objects.requireNonNull(locIdentifier, "locIdentifier");
        return new Child(entry, libraryLang, readOptions, this, locIdentifier);
    }

    @Override
    public @NonNull ErrorContext buildError(@NonNull Printable message) {
        LoadError loadError = new LoadError(message, libraryLang);
        // Add entry path
        loadError.addDetail(ErrorContext.ENTRY_PATH, keyPath());
        // Add line number
        Integer lineNumber = entry.getLineNumber();
        if (lineNumber != null) {
            loadError.addDetail(ErrorContext.LINE_NUMBER, lineNumber);
        }
        return loadError;
    }
}
