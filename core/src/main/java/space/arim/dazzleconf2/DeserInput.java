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

package space.arim.dazzleconf2;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataList;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.Objects;

abstract class DeserInput extends DeserContext implements DeserializeInput {

    private final DataEntry entry;

    DeserInput(DataEntry entry, LibraryLang libraryLang, ConfigurationDefinition.ReadOptions readOptions) {
        super(libraryLang, readOptions);
        this.entry = Objects.requireNonNull(entry, "entry");
    }

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
        ErrorContext errorContext = super.buildError(message);
        Integer lineNumber = entry.getLineNumber();
        if (lineNumber != null) {
            errorContext.addDetail(ErrorContext.LINE_NUMBER, lineNumber);
        }
        return errorContext;
    }
}
