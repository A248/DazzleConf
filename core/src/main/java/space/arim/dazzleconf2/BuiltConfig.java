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

import space.arim.dazzleconf.internal.util.ImmutableCollections;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CachedBackend;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DataTreeMut;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.migration.Migration;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.translation.LibraryLang;

import java.util.*;

final class BuiltConfig<C> implements Configuration<C> {

    private final Definition<C> definition;
    private final Locale locale;
    private final LibraryLang libraryLang;
    private final List<TypeLiaison> typeLiaisons;
    private final KeyMapper keyMapper;
    private final List<Migration<?, C>> migrations;

    BuiltConfig(Definition<C> definition, Locale locale, LibraryLang libraryLang, List<TypeLiaison> typeLiaisons,
                KeyMapper keyMapper, List<Migration<?, C>> migrations) {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.locale = Objects.requireNonNull(locale, "locale");
        this.libraryLang = libraryLang;
        this.typeLiaisons = ImmutableCollections.listOf(typeLiaisons);
        this.keyMapper = keyMapper;
        this.migrations = ImmutableCollections.listOf(migrations);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<TypeLiaison> getTypeLiaisons() {
        return typeLiaisons;
    }

    @Override
    public Optional<KeyMapper> getKeyMapper() {
        return Optional.ofNullable(keyMapper);
    }

    @Override
    public Instantiator getInstantiator() {
        return definition.instantiator;
    }

    @Override
    public List<Migration<?, C>> getMigrations() {
        return migrations;
    }

    @Override
    public C loadDefaults() {
        return definition.loadDefaults();
    }

    @Override
    public LoadResult<C> readWithKeyMapper(DataTree dataTree, LoadListener loadListener, KeyMapper keyMapper) {
        Objects.requireNonNull(dataTree, "dataTree");
        Objects.requireNonNull(loadListener, "loadListener");
        Objects.requireNonNull(keyMapper, "keyMapper");
        return definition.readWithKeyMapper(dataTree, loadListener, keyMapper);
    }

    @Override
    public void writeWithKeyMapper(C config, DataTreeMut dataTree, KeyMapper keyMapper) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(dataTree, "dataTree");
        Objects.requireNonNull(keyMapper, "keyMapper");
        definition.writeWithKeyMapper(config, dataTree, keyMapper);
    }

    @Override
    public LoadResult<C> readFrom(DataTree dataTree) {
        return readFrom(dataTree, entryPath -> {});
    }

    @Override
    public LoadResult<C> readFrom(DataTree dataTree, LoadListener loadListener) {
        return readWithKeyMapper(
                dataTree, loadListener, Objects.requireNonNullElseGet(this.keyMapper, DefaultKeyMapper::new)
        );
    }

    @Override
    public void writeTo(C config, DataTreeMut dataTree) {
        writeWithKeyMapper(config, dataTree, Objects.requireNonNullElseGet(this.keyMapper, DefaultKeyMapper::new));
    }

    @Override
    public LoadResult<C> configureWith(Backend backend) {
        return configureWith0(new CachedBackend(backend), null);
    }

    @Override
    public LoadResult<C> configureWith(Backend backend, UpdateListener updateListener) {
        Objects.requireNonNull(updateListener, "updateListener");
        return configureWith0(new CachedBackend(backend), updateListener);
    }

    private static class RecordUpdates implements UpdateListener {

        boolean updated;

        @Override
        public void loadedDefaults() {}

        @Override
        public void migratedFrom(Migration<?, ?> migration) {}

        @Override
        public void migrationSkip(Migration<?, ?> migration, ErrorContext failureContext) {}

        @Override
        public void updatedMissingPath(KeyPath entryPath) {
            updated = true;
        }

        static class WithDelegate extends RecordUpdates {

            private final UpdateListener delegate;

            WithDelegate(UpdateListener delegate) {
                this.delegate = delegate;
            }

            @Override
            public void loadedDefaults() {
                delegate.loadedDefaults();
            }

            @Override
            public void migratedFrom(Migration<?, ?> migration) {
                delegate.migratedFrom(migration);
            }

            @Override
            public void migrationSkip(Migration<?, ?> migration, ErrorContext failureContext) {
                delegate.migrationSkip(migration, failureContext);
            }

            @Override
            public void updatedMissingPath(KeyPath entryPath) {
                delegate.updatedMissingPath(entryPath);
                updated = true;
            }
        }
    }

    private LoadResult<C> configureWith0(
            CachedBackend backend, // Save on read/write operations
            UpdateListener updateListener // Null for unset
    ) {

        ClassLoader cl = null;
        getClass().getResource("");
        RecordUpdates recordUpdates;
        if (updateListener == null) {
            recordUpdates = new RecordUpdates();
        } else {
            recordUpdates = new RecordUpdates.WithDelegate(updateListener);
        }
        KeyMapper keyMapper = Objects.requireNonNullElse(this.keyMapper, backend.recommendKeyMapper());

        // 1. Try to migrate if possible
        if (!migrations.isEmpty()) {
            // Try all migrations
            for (Migration<?, C> migration : migrations) {
                LoadResult<C> attempt = migration.tryMigrate(backend);
                if (attempt.isSuccess()) {
                    // Update the backend with the migrated value
                    C migrated = attempt.getOrThrow();
                    DataTreeMut writeBack = new DataTreeMut();
                    writeWithKeyMapper(migrated, writeBack, keyMapper);
                    backend.writeTree(writeBack);
                    // Now signal completion, and finish
                    migration.onCompletion();
                    recordUpdates.migratedFrom(migration);
                    return attempt;
                }
                recordUpdates.migrationSkip(migration, attempt.getError().orElseThrow());
                // Keep going
            }
        }
        // 2. Load configuration
        return backend.readTree().flatMap((loadedTree) -> {
            LoadResult<C> loadResult = readWithKeyMapper(loadedTree, recordUpdates, keyMapper);
            loadResult.ifSuccess((loaded) -> {
                // 3. Update if necessary
                if (recordUpdates.updated) {
                    DataTreeMut loadedTreeMut = loadedTree.makeMut();
                    writeWithKeyMapper(loaded, loadedTreeMut, keyMapper);
                    backend.writeTree(loadedTreeMut);
                }
            });
            return loadResult;
        });
    }

}
