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
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.backend.Printable;
import space.arim.dazzleconf.engine.DeserializeContext;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.internals.lang.LibraryLang;

abstract class DeserContext extends LoadError.Factory implements DeserializeContext {

    final LibraryLang libraryLang;
    final ConfigurationDefinition.ReadOptions readOptions;

    DeserContext(LibraryLang libraryLang, ConfigurationDefinition.ReadOptions readOptions) {
        this.libraryLang = libraryLang;
        this.readOptions = readOptions;
    }

    static final class Standalone extends DeserContext {

        private final String mappedKey;

        Standalone(LibraryLang libraryLang, ConfigurationDefinition.ReadOptions readOptions, String mappedKey) {
            super(libraryLang, readOptions);
            this.mappedKey = mappedKey;
        }

        @Override
        KeyPath.Mut getPathContribution() {
            return new KeyPath.Mut(mappedKey);
        }
    }

    abstract KeyPath.Mut getPathContribution();

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
}
