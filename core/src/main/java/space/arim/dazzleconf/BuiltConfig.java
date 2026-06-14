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
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.backend.Backend;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.DefaultKeyMapper;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.backend.Printable;
import space.arim.dazzleconf.engine.CommentLocation;
import space.arim.dazzleconf.engine.DefinedLayout;
import space.arim.dazzleconf.engine.DefinedNode;
import space.arim.dazzleconf.engine.DeserializeContext;
import space.arim.dazzleconf.engine.Interprocessor;
import space.arim.dazzleconf.engine.SerializeContext;
import space.arim.dazzleconf.engine.UpdateListener;
import space.arim.dazzleconf.engine.TypeLiaison;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.internals.lang.LibraryLang;
import space.arim.dazzleconf.migration.MigrateContext;
import space.arim.dazzleconf.migration.Migration;
import space.arim.dazzleconf.reflect.TypeToken;

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
        this.definition = Objects.requireNonNull(definition);
        this.locale = Objects.requireNonNull(locale);
        this.libraryLang = Objects.requireNonNull(libraryLang);
        this.keyMapper = keyMapper;
        this.typeLiaisons = Objects.requireNonNull(typeLiaisons);
        this.migrations = Objects.requireNonNull(migrations);
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
    public @NonNull DefinedLayout<C> getDefinedLayout() {
        return definition.getDefinedLayout();
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

    private void writeWithKeyMapper(@NonNull C config, DataTree.@NonNull Mut dataTree, @NonNull KeyMapper keyMapper) {
        writeTo(config, dataTree, new WriteOptions() {
            @Override
            public @NonNull KeyMapper keyMapper() {
                return keyMapper;
            }

            @Override
            public @NonNull KeyPath keyPath() {
                return KeyPath.empty();
            }

            @Override
            public @NonNull Interprocessor getInterprocessor() {
                return Interprocessor.DEFAULT;
            }
        });
    }

    @Override
    public @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree) {
        return readFrom(dataTree, (entryPath, updateReason) -> {});
    }

    @Override
    public @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull UpdateListener updateListener) {
        KeyMapper keyMapper = this.keyMapper != null ? this.keyMapper : new DefaultKeyMapper();
        return readFrom(dataTree, new ReadOptions() {
            @Override
            public void notifyUpdate(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {
                updateListener.notifyUpdate(entryPath, updateReason);
            }

            @Override
            public @NonNull KeyMapper keyMapper() {
                return keyMapper;
            }

            @Override
            public @NonNull KeyPath keyPath() {
                return KeyPath.empty();
            }

            @Override
            public @NonNull Interprocessor getInterprocessor() {
                return Interprocessor.DEFAULT;
            }
        });
    }

    @Override
    public void writeTo(@NonNull C config, DataTree.@NonNull Mut dataTree) {
        KeyMapper keyMapper = this.keyMapper != null ? this.keyMapper : new DefaultKeyMapper();
        writeWithKeyMapper(config, dataTree, keyMapper);
    }

    @Override
    public @NonNull LoadResult<@NonNull C> configureWith(@NonNull Backend backend) {
        return configureWith0(backend, new ConfigureListener() {
            @Override
            public void wroteDefaults() {}

            @Override
            public void migratedFrom(@NonNull Migration<?, ?> migration) {}

            @Override
            public void migrationSkip(@NonNull Migration<?, ?> migration, @NonNull List<@NonNull ErrorContext> errorContexts) {}

            @Override
            public void notifyUpdate(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {}
        });
    }

    @Override
    public @NonNull LoadResult<@NonNull C> configureWith(@NonNull Backend backend, @NonNull ConfigureListener configureListener) {
        Objects.requireNonNull(configureListener, "configureListener");
        return configureWith0(backend, configureListener);
    }

    private LoadResult<C> configureWith0(@NonNull Backend backend, @NonNull ConfigureListener configureListener) {
        ErrorContext.Source errorSource = makeErrorSource();
        try (CachedBackend cachedBackend = new CachedBackend(backend)) {
            cachedBackend.initialLoad(errorSource);
            return implConfigureWith(cachedBackend, errorSource, configureListener);
        }
    }

    // Helps avoid redundant read/write operations to the actual backend
    private static final class CachedBackend implements Backend, AutoCloseable {

        private final Backend delegate;
        private final Backend.Meta meta;

        private List<ErrorContext> currentErrors;
        private Document currentDoc;
        private boolean changed;

        CachedBackend(Backend delegate) {
            this.delegate = delegate;
            meta = delegate.meta();
        }

        void initialLoad(ErrorContext.@NonNull Source errorSource) {
            LoadResult<Document> initialDoc = delegate.read(errorSource);
            if (initialDoc.isSuccess()) {
                currentDoc = initialDoc.getOrThrow();
            } else {
                currentErrors = initialDoc.getErrorContexts();
            }
        }

        @Override
        // Not actually called by us -- might be invoked by migrations
        public @NonNull LoadResult<@Nullable Document> read(ErrorContext.@NonNull Source errorSource) {
            if (currentErrors != null) {
                return LoadResult.failure(currentErrors);
            }
            Document readDoc = currentDoc;
            if (readDoc == null) {
                return LoadResult.of(null);
            }
            // We return an immutable data tree for use by migrations
            return LoadResult.of(new Document() {
                @Override
                public @NonNull CommentData comments() {
                    return readDoc.comments();
                }

                @Override
                public @NonNull DataTree data() {
                    return readDoc.data().intoImmut();
                }
            });
        }

        // Used by us, skips intoImmut() on data tree
        LoadResult<@Nullable Document> readOwned() {
            if (currentErrors != null) {
                return LoadResult.failure(currentErrors);
            }
            return LoadResult.of(/* nullable */ currentDoc);
        }

        @Override
        public void write(@NonNull Document document) {
            changed = true;
            currentErrors = null;
            currentDoc = document;
        }

        @Override
        public void close() {
            if (changed) {
                delegate.write(currentDoc);
            }
        }

        // Meta-related

        @Override
        public @NonNull KeyMapper recommendKeyMapper() {
            return delegate.recommendKeyMapper();
        }

        @Override
        public @NonNull Meta meta() {
            return meta;
        }

        boolean shouldRefreshComments(boolean documentLevel, CommentLocation location) {
            boolean supportsReading = meta.supportsComments(documentLevel, true, location);
            boolean supportsWriting = meta.supportsComments(documentLevel, false, location);
            return !supportsReading && supportsWriting;
        }
    }

    private LoadResult<C> implConfigureWith(@NonNull CachedBackend backend, ErrorContext.@NonNull Source errorSource,
                                            @NonNull ConfigureListener configureListener) {
        // 0. Setup
        DefinedLayout<C> layout = getDefinedLayout();
        KeyMapper keyMapper = (this.keyMapper != null) ? this.keyMapper : backend.recommendKeyMapper();
        Objects.requireNonNull(keyMapper, "Backend returned null key mapper");
        // 1. Try to migrate if possible
        if (!migrations.isEmpty()) {
            // Build migraton context
            class MigrateCtx implements MigrateContext {

                @Override
                public @NonNull Backend mainBackend() {
                    return backend;
                }

                @Override
                public void notifyUpdate(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {
                    configureListener.notifyUpdate(entryPath, UpdateReason.MIGRATED);
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
                    writeWithKeyMapper(migrated, migratedData, keyMapper);
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
                    configureListener.migratedFrom(migration);
                    return attempt;
                }
                configureListener.migrationSkip(migration, attempt.getErrorContexts());
                // Keep going
            }
        }
        // 2. Load configuration
        // It should already exist in memory, and calling #readOwned should give us a copy-free mutable tree
        LoadResult<Backend.@Nullable Document> read = backend.readOwned();
        if (read.isFailure()) {
            return LoadResult.failure(read.getErrorContexts());
        }
        Backend.Document document = read.getOrThrow();
        if (document == null) {
            // 3. Write defaults if necessary
            C defaults = loadDefaults();
            DataTree.Mut defaultData = new DataTree.Mut();
            writeWithKeyMapper(defaults, defaultData, keyMapper);
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
            configureListener.wroteDefaults();
            return LoadResult.of(defaults);
        }
        CommentLocation[] commentLocations = CommentLocation.values();
        Interprocessor interprocessor = new Interprocessor.Builder()
                .addHook(DefinedLayout.UPDATE_STRUCTURED_ENTRY, commentRefreshUpdater(commentLocations, backend))
                .addHook(DefinedLayout.WRITE_STRUCTURED_TREE, dataTreeResorter(backend.meta))
                .build();
        DataTree.Mut updatableTree = document.data().intoMut();
        class ReadWithUpdateOpts implements ReadWithUpdateOptions {
            boolean updated;

            @Override
            public void notifyUpdate(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {
                updated = true;
                configureListener.notifyUpdate(entryPath, updateReason);
            }

            @Override
            public @NonNull KeyMapper keyMapper() {
                return keyMapper;
            }

            @Override
            public @NonNull KeyPath keyPath() {
                return KeyPath.empty();
            }

            @Override
            public @NonNull Interprocessor getInterprocessor() {
                return interprocessor;
            }
        }
        ReadWithUpdateOpts readWithUpdateOpts = new ReadWithUpdateOpts();
        LoadResult<C> loadResult = readWithUpdate(updatableTree, readWithUpdateOpts);
        if (loadResult.isSuccess() && readWithUpdateOpts.updated) {
            // 4. Update if necessary
            backend.write(new Backend.Document() {
                // Preserve comments if possible, or refresh them from the layout
                @Override
                public @NonNull CommentData comments() {
                    CommentData fromLayout = layout.getComments();
                    CommentData current = document.comments();
                    for (CommentLocation location : commentLocations) {
                        if (backend.shouldRefreshComments(true, location)) {
                            current = current.setAt(location, fromLayout.getAt(location));
                        }
                    }
                    return current;
                }

                @Override
                public @NonNull DataTree data() {
                    return updatableTree;
                }
            });
        }
        return loadResult;
    }

    private static DefinedLayout.@NonNull OnUpdateStructuredEntry commentRefreshUpdater(
            CommentLocation[] commentLocations, CachedBackend backend
    ) {
        boolean[] refreshCommentsOnEntry = new boolean[commentLocations.length];
        boolean anywhere = false;
        boolean anywhereNot = false;
        for (int n = 0; n < commentLocations.length; n++) {
            refreshCommentsOnEntry[n] = backend.shouldRefreshComments(false, commentLocations[n]);
            anywhere |= refreshCommentsOnEntry[n];
            anywhereNot |= !refreshCommentsOnEntry[n];
        }
        if (!anywhere) { // nowhere
            return DefinedLayout.UPDATE_STRUCTURED_ENTRY.defaultValue();
        }
        if (!anywhereNot) { // everywhere
            return new DefinedLayout.OnUpdateStructuredEntry() {
                @Override
                public @Nullable <R, B> DataEntry insertUpdate(@Nullable DataEntry entry, @NonNull DataEntry oldEntry,
                                                               @NonNull DeserializeContext deserializeContext,
                                                               @NonNull SerializeContext serializeContext,
                                                               @NonNull DefinedLayout<?> definedLayout,
                                                               DefinedNode.@NonNull Value<R, B> valueNode,
                                                               DefinedLayout.@NonNull Branch<B> branchOfNode) {
                    if (entry != null) {
                        entry = entry.withComments(valueNode.comments());
                    }
                    return entry;
                }
            };
        }
        return new DefinedLayout.OnUpdateStructuredEntry() {
            @Override
            public @Nullable <R, B> DataEntry insertUpdate(@Nullable DataEntry entry, @NonNull DataEntry oldEntry,
                                                           @NonNull DeserializeContext deserializeContext,
                                                           @NonNull SerializeContext serializeContext,
                                                           @NonNull DefinedLayout<?> definedLayout,
                                                           DefinedNode.@NonNull Value<R, B> valueNode,
                                                           DefinedLayout.@NonNull Branch<B> branchOfNode) {
                if (entry != null) {
                    CommentData nodeComments = valueNode.comments();
                    if (!nodeComments.isEmpty()) {
                        CommentData entryComments = entry.getComments();
                        for (CommentLocation refreshLocation : commentLocations) {
                            if (refreshCommentsOnEntry[refreshLocation.ordinal()]) {
                                entryComments = entryComments.setAt(refreshLocation, nodeComments.getAt(refreshLocation));
                            }
                        }
                        entry = entry.withComments(entryComments);
                    }
                }
                return entry;
            }
        };
    }

    private static DefinedLayout.@NonNull OnWriteStructuredTree dataTreeResorter(Backend.Meta meta) {
        if (!meta.preservesOrder(true) && meta.preservesOrder(false)) {
            // By re-sorting the data tree, we can rely on the backend to write ordered data
            return new DefinedLayout.OnWriteStructuredTree() {
                @Override
                public void writeTree(DataTree.@NonNull Mut dataTree, @NonNull SerializeContext treeContext,
                                      @NonNull DefinedLayout<?> definedLayout) {
                    DataTree.Mut sorted = new DataTree.Mut();
                    definedLayout.forEachBranch(branch -> {
                        for (DefinedNode<?, ?> node : branch.getNodes()) {
                            if (!(node instanceof DefinedNode.Value)) {
                                continue;
                            }
                            DefinedNode.Value<?, ?> valueNode = (DefinedNode.Value<?, ?>) node;
                            String mappedKey = treeContext.keyMapper().labelToKey(valueNode.label()).toString();
                            DataEntry entry = dataTree.remove(mappedKey);
                            if (entry != null) {
                                sorted.put(mappedKey, entry);
                            }
                        }
                    });
                    // Remaining entries (unsorted, not connected to definition) get pushed to the end
                    sorted.putAll(dataTree);
                    dataTree.setAll(sorted);
                }
            };
        }
        return DefinedLayout.WRITE_STRUCTURED_TREE.defaultValue();
    }

    @Override
    public @NonNull C configureOrFallback(@NonNull Backend backend, @NonNull ErrorPrint errorPrint) {
        return handleErrors(configureWith(backend), errorPrint);
    }

    @Override
    public @NonNull C configureOrFallback(@NonNull Backend backend, @NonNull ConfigureListener configureListener,
                                          @NonNull ErrorPrint errorPrint) {
        return handleErrors(configureWith(backend, configureListener), errorPrint);
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
        ReloadShell<C> reloadShell = getDefinedLayout().getReflectionProvider().generateShell();
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

    @Override
    public String toString() {
        return "BuiltConfig{" +
                "definition=" + definition +
                ", locale=" + locale +
                ", libraryLang=" + libraryLang +
                ", typeLiaisons=" + typeLiaisons +
                ", keyMapper=" + keyMapper +
                ", migrations=" + migrations +
                '}';
    }
}
