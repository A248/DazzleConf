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
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CachedBackend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DefaultKeyMapper;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.engine.UpdateListener;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.migration.MigrateContext;
import space.arim.dazzleconf2.migration.Migration;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

final class BuiltConfig<C> implements Configuration<C> {

    private final ConfigurationDefinition<C> definition;
    private final Locale locale;
    private final LibraryLang libraryLang;
    private final List<TypeLiaison> typeLiaisons;
    private final KeyMapper keyMapper;
    private final List<Migration<?, C>> migrations;

    BuiltConfig(ConfigurationDefinition<C> definition, Locale locale, LibraryLang libraryLang,
                List<TypeLiaison> typeLiaisons, KeyMapper keyMapper, List<Migration<?, C>> migrations) {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.locale = Objects.requireNonNull(locale, "locale");
        this.libraryLang = libraryLang;
        this.typeLiaisons = typeLiaisons;
        this.keyMapper = keyMapper;
        this.migrations = Objects.requireNonNull(migrations, "migrations");
    }

    @Override
    public @NonNull Locale getLocale() {
        return locale;
    }

    @Override
    public @NonNull List<@NonNull TypeLiaison> getTypeLiaisons() {
        return typeLiaisons;
    }

    @Override
    public @Nullable KeyMapper getKeyMapper() {
        return keyMapper;
    }

    @Override
    public @NonNull List<@NonNull Migration<?, C>> getMigrations() {
        return migrations;
    }

    @Override
    public @NonNull TypeToken<C> getType() {
        return definition.getType();
    }

    @Override
    public @NonNull Layout getLayout() {
        return definition.getLayout();
    }

    @Override
    public @NonNull Instantiator getInstantiator() {
        return definition.getInstantiator();
    }

    @Override
    public @NonNull C loadDefaults() {
        return definition.loadDefaults();
    }

    @Override
    public @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull ReadOptions readOptions) {
        Objects.requireNonNull(dataTree, "dataTree");
        Objects.requireNonNull(readOptions, "readOptions");
        return definition.readFrom(dataTree, readOptions);
    }

    @Override
    public @NonNull LoadResult<@NonNull C> readWithUpdate(DataTree.@NonNull Mut dataTree, @NonNull ReadWithUpdateOptions readOptions) {
        Objects.requireNonNull(dataTree, "dataTree");
        Objects.requireNonNull(readOptions, "readOptions");
        return definition.readWithUpdate(dataTree, readOptions);
    }

    @Override
    public void writeTo(@NonNull C config, DataTree.@NonNull Mut dataTree, @NonNull WriteOptions writeOptions) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(dataTree, "dataTree");
        Objects.requireNonNull(writeOptions, "writeOptions");
        definition.writeTo(config, dataTree, writeOptions);
    }

    @Override
    public @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree) {
        return readFrom(dataTree, (entryPath, updateReason) -> {});
    }

    @Override
    public @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull LoadListener loadListener) {
        KeyMapper keyMapper = this.keyMapper != null ? this.keyMapper : new DefaultKeyMapper();
        return readFrom(dataTree, new ReadOptions() {
            @Override
            public @NonNull LoadListener loadListener() {
                return loadListener;
            }

            @Override
            public @NonNull KeyMapper keyMapper() {
                return keyMapper;
            }
        });
    }

    @Override
    public void writeTo(@NonNull C config, DataTree.@NonNull Mut dataTree) {
        KeyMapper keyMapper = this.keyMapper != null ? this.keyMapper : new DefaultKeyMapper();
        writeTo(config, dataTree, () -> keyMapper);
    }

    @Override
    public @NonNull LoadResult<@NonNull C> configureWith(@NonNull Backend backend) {
        return configureWith0(new CachedBackend(backend), null);
    }

    @Override
    public @NonNull LoadResult<@NonNull C> configureWith(@NonNull Backend backend, @NonNull UpdateListener updateListener) {
        Objects.requireNonNull(updateListener, "updateListener");
        return configureWith0(new CachedBackend(backend), updateListener);
    }

    private static class RecordUpdates implements UpdateListener {

        boolean updated;

        @Override
        public void wroteDefaults() {}

        @Override
        public void migratedFrom(@NonNull Migration<?, ?> migration) {}

        @Override
        public void migrationSkip(@NonNull Migration<?, ?> migration, @NonNull List<@NonNull ErrorContext> errorContexts) {}

        @Override
        public void updatedPath(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {
            updated = true;
        }

        static class WithDelegate extends RecordUpdates {

            private final UpdateListener delegate;

            WithDelegate(UpdateListener delegate) {
                this.delegate = delegate;
            }

            @Override
            public void wroteDefaults() {
                delegate.wroteDefaults();
            }

            @Override
            public void migratedFrom(@NonNull Migration<?, ?> migration) {
                delegate.migratedFrom(migration);
            }

            @Override
            public void migrationSkip(@NonNull Migration<?, ?> migration,
                                      @NonNull List<@NonNull ErrorContext> errorContexts) {
                delegate.migrationSkip(migration, errorContexts);
            }

            @Override
            public void updatedPath(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {
                updated = true;
                delegate.updatedPath(entryPath, updateReason);
            }
        }
    }

    private LoadResult<C> configureWith0(@NonNull CachedBackend backend, @Nullable UpdateListener updateListener) {
        // 0. Setup
        Layout layout = getLayout();
        RecordUpdates recordUpdates = (updateListener == null) ?
                new RecordUpdates() : new RecordUpdates.WithDelegate(updateListener);
        KeyMapper keyMapper = (this.keyMapper != null) ? this.keyMapper : backend.recommendKeyMapper();
        Objects.requireNonNull(keyMapper, "Backend returned null key mapper");
        ErrorContext.Source errorSource = makeErrorSource();
        // 1. Try to migrate if possible
        if (!migrations.isEmpty()) {
            // Build migraton context
            class MigrateCtx implements MigrateContext {

                @Override
                public @NonNull Backend mainBackend() {
                    return backend;
                }

                @Override
                public @NonNull LoadListener loadListener() {
                    return recordUpdates;
                }

                @Override
                public ErrorContext.@NonNull Source errorSource() {
                    return errorSource;
                }
            }
            MigrateCtx migrateCtx = new MigrateCtx();
            // Try all migrations
            for (Migration<?, C> migration : migrations) {
                LoadResult<C> attempt = migration.tryMigrate(migrateCtx);
                if (attempt.isSuccess()) {
                    C migrated = attempt.getOrThrow();
                    // Update the backend with the migrated value
                    DataTree.Mut migratedData = new DataTree.Mut();
                    writeTo(migrated, migratedData, () -> keyMapper);
                    backend.write(new Backend.Document() {
                        @Override
                        public @NonNull CommentData comments() {
                            return layout.getComments();
                        }

                        @Override
                        public @NonNull DataTree data() {
                            return migratedData;
                        }
                    });
                    // Now signal completion, and finish
                    migration.onCompletion();
                    recordUpdates.migratedFrom(migration);
                    return attempt;
                }
                recordUpdates.migrationSkip(migration, attempt.getErrorContexts());
                // Keep going
            }
            // Reset if necessary
            recordUpdates.updated = false;
        }
        // 2. Load configuration
        LoadResult<Backend.@Nullable Document> read = backend.read(errorSource);
        if (read.isFailure()) {
            return LoadResult.failure(read.getErrorContexts());
        }
        Backend.Document document = read.getOrThrow();
        if (document == null) {
            // 3. Write defaults if necessary
            C defaults = loadDefaults();
            DataTree.Mut defaultData = new DataTree.Mut();
            writeTo(defaults, defaultData, () -> keyMapper);
            backend.write(new Backend.Document() {
                @Override
                public @NonNull CommentData comments() {
                    return layout.getComments();
                }

                @Override
                public @NonNull DataTree data() {
                    return defaultData;
                }
            });
            recordUpdates.wroteDefaults();
            return LoadResult.of(defaults);
        }
        DataTree.Mut updatableTree = document.data().intoMut();
        Backend.Meta backendMeta = backend.meta();
        LoadResult<C> loadResult = readWithUpdate(updatableTree, new ReadWithUpdateOptions() {

            @Override
            public @NonNull LoadListener loadListener() {
                return recordUpdates;
            }

            @Override
            public @NonNull KeyMapper keyMapper() {
                return keyMapper;
            }

            @Override
            public boolean writeEntryComments(@NonNull CommentLocation location) {
                return refreshComments(backendMeta, false, location);
            }
        });
        if (loadResult.isSuccess() && recordUpdates.updated) {
            // 4. Update if necessary
            backend.write(new Backend.Document() {
                // Preserve comments if possible, or refresh them from the layout
                @Override
                public @NonNull CommentData comments() {
                    CommentData fromLayout = getLayout().getComments();
                    CommentData current = document.comments();
                    for (CommentLocation location : CommentLocation.values()) {
                        if (refreshComments(backendMeta, true, location)) {
                            current = current.setAt(location, fromLayout.getAt(location));
                        }
                    }
                    return current;
                }

                @Override
                public @NonNull DataTree data() {
                    if (backendMeta.supportsOrder(true)) {
                        // Preserves order thanks to DataTree's order guarantees
                    } else if (backendMeta.supportsOrder(false)) {
                        // Supports writing order, but not reading it. So, we need to sort the data tree
                        // TODO: Figure out how to do this with nested sub-sections
                    }
                    return updatableTree;
                }
            });
        }
        return loadResult;
    }

    private static boolean refreshComments(Backend.Meta backendMeta, boolean documentLevel, CommentLocation location) {
        boolean supportsReading = backendMeta.supportsComments(documentLevel, true, location);
        boolean supportsWriting = backendMeta.supportsComments(documentLevel, false, location);
        return !supportsReading && supportsWriting;
    }

    @Override
    public @NonNull C configureOrFallback(@NonNull Backend backend, @NonNull ErrorPrint errorPrint) {
        return handleErrors(configureWith(backend), errorPrint);
    }

    @Override
    public @NonNull C configureOrFallback(@NonNull Backend backend, @NonNull UpdateListener updateListener,
                                          @NonNull ErrorPrint errorPrint) {
        return handleErrors(configureWith(backend, updateListener), errorPrint);
    }

    private C handleErrors(LoadResult<C> result, ErrorPrint errorPrint) {
        if (result.isSuccess()) {
            return result.getOrThrow();
        }
        errorPrint.onError(result.getErrorContexts());
        return loadDefaults();
    }

    @Override
    public @NonNull ReloadShell<C> makeReloadShell(@Nullable C initialValue) {
        ReloadShell<C> reloadShell = getInstantiator().generateShell(getType().getRawType());
        reloadShell.setCurrentDelegate(initialValue);
        return reloadShell;
    }

    @Override
    public ErrorContext.@NonNull Source makeErrorSource() {
        return new LoadError.Factory() {
            @Override
            public @NonNull ErrorContext buildError(@NonNull Printable message) {
                return new LoadError(message, libraryLang);
            }

            @Override
            public @NonNull LibraryLang getLibraryLang() {
                return libraryLang;
            }
        };
    }
}
