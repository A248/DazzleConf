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
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.engine.DefinedLayout;
import space.arim.dazzleconf.engine.DeserializeContext;
import space.arim.dazzleconf.engine.DeserializeInput;
import space.arim.dazzleconf.engine.NoOutput;
import space.arim.dazzleconf.engine.SerializeDeserialize;
import space.arim.dazzleconf.engine.SerializeOutput;
import space.arim.dazzleconf.internals.lang.LibraryLang;
import space.arim.dazzleconf.reflect.MethodYield;
import space.arim.dazzleconf.reflect.ReflectionProvider;
import space.arim.dazzleconf.reflect.TypeToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class Definition<C> implements ConfigurationDefinition<C> {

    private final TypeToken<C> configType;
    private final CommentData topLevelComments;
    final LibraryLang libraryLang;
    private final ReflectionProvider<C> reflectionProvider;
    private final TypeSkeleton<?>[] typeSkeletons;

    Definition(TypeToken<C> configType, CommentData topLevelComments, List<TypeSkeleton<?>> typeSkeletons,
               LibraryLang libraryLang, ReflectionProvider<C> reflectionProvider) {
        this.configType = Objects.requireNonNull(configType);
        this.topLevelComments = Objects.requireNonNull(topLevelComments);
        this.libraryLang = Objects.requireNonNull(libraryLang);
        this.reflectionProvider = Objects.requireNonNull(reflectionProvider);

        // Sort skeletons from the back: This means we read/write parent types first
        TypeSkeleton<?>[] skeletonsArray = new TypeSkeleton[typeSkeletons.size()];
        for (int n = 0; n < skeletonsArray.length; n++) {
            skeletonsArray[n] = typeSkeletons.get(skeletonsArray.length - n - 1);
        }
        this.typeSkeletons = skeletonsArray;
    }

    @Override
    public @NonNull TypeToken<C> getType() {
        return configType;
    }

    @Override
    public @NonNull DefinedLayout<C> getDefinedLayout() {
        class DefinedLayoutImpl implements DefinedLayout<C> {
            @Override
            public @NonNull TypeToken<C> getType() {
                return configType;
            }

            @Override
            public @NonNull CommentData getComments() {
                return topLevelComments;
            }

            @Override
            public @NonNull ReflectionProvider<C> getReflectionProvider() {
                return reflectionProvider;
            }

            @Override
            public @NonNull Collection<@NonNull ? extends Branch<?>> getBranches() {
                return Collections.unmodifiableList(Arrays.asList(typeSkeletons));
            }

            @Override
            public void forEachBranch(@NonNull BranchCallback<C> branchCallback) {
                for (TypeSkeleton<?> typeSkeleton : typeSkeletons) {
                    @SuppressWarnings("unchecked")
                    TypeSkeleton<? super C> castSkeleton = (TypeSkeleton<? super C>) typeSkeleton;
                    branchCallback.runFor(castSkeleton);
                }
            }
        }
        return new DefinedLayoutImpl();
    }

    @Override
    public @NonNull C loadDefaults() {
        try (MethodYield methodYield = reflectionProvider.newMethodYield()) {
            for (TypeSkeleton<?> typeSkeleton : typeSkeletons) {
                typeSkeleton.implLoadDefaults(methodYield);
            }
            return reflectionProvider.generate(methodYield);
        }
    }

    private <UPD, DT extends DataTree> @NonNull LoadResult<@NonNull C> readingNexus(
            @NonNull DT dataTree, @NonNull ReadOptions readOptions, TypeSkeleton.@NonNull HowToUpdate<UPD, DT> howToUpdate
    ) {
        // Collected errors - get a certain maximum before quitting, becomes non-null if we find at least 1 error
        ErrorContext[] collectedErrors = null;
        int errorCount = 0;
        try (MethodYield methodYield = reflectionProvider.newMethodYield()) {
            for (TypeSkeleton<?> typeSkeleton : typeSkeletons) {
                TypeSkeleton<?>.ImplRead<UPD, DT> implRead = typeSkeleton.new ImplRead<>(
                        dataTree, howToUpdate, readOptions, this
                );
                implRead.collectedErrors = collectedErrors;
                implRead.errorCount = errorCount;
                implRead.readNodes(methodYield);
                collectedErrors = implRead.collectedErrors;
                errorCount = implRead.errorCount;
                if (collectedErrors != null && errorCount == collectedErrors.length) {
                    return LoadResult.failure(collectedErrors);
                }
            }
            // Error handling
            if (collectedErrors != null) {
                return LoadResult.failure(Arrays.copyOf(collectedErrors, errorCount));
            }
            // No errors - success
            return LoadResult.of(reflectionProvider.generate(methodYield));
        }
    }

    @Override
    public @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull ReadOptions readOptions) {
        return readingNexus(dataTree, readOptions, new TypeSkeleton.HowToUpdate<@Nullable Void, DataTree>() {
            @Override
            public @Nullable Void makeUpdater(String mappedKey) {
                return null;
            }

            @Override
            public <V, B> void insertMissingValue(DataTree dataTree, String mappedKey, SkeletonNode.Val<V, B> valNode,
                                                  V missingValue, DefinedLayout.Branch<B> branchOfNode) {}

            @Override
            public <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser,
                                                 @Nullable Void updater) {
                return serializeDeserialize.deserialize(deser);
            }

            @Override
            public <B> void updateIfDesired(DataTree dataTree, String mappedKey, DataEntry sourceEntry,
                                            SkeletonNode.Val<?, B> valNode, DeserializeContext deserContext,
                                            DefinedLayout.Branch<B> branchOfNode, @Nullable Void updater) {}
        });
    }

    @Override
    public @NonNull LoadResult<@NonNull C> readWithUpdate(DataTree.@NonNull Mut dataTree, @NonNull ReadWithUpdateOptions readUpdateOptions) {
        DefinedLayout<C> definedLayout = getDefinedLayout();
        LoadResult<@NonNull C> result = readingNexus(dataTree, readUpdateOptions, new TypeSkeleton.HowToUpdate<SerializeOutput, DataTree.Mut>() {
            @Override
            public SerializeOutput makeUpdater(String mappedKey) {
                return new SerContext.AtKey(readUpdateOptions, mappedKey).newOutput();
            }

            @Override
            public <V, B> void insertMissingValue(DataTree.Mut dataTree, String mappedKey, SkeletonNode.Val<V, B> valNode,
                                                  V missingValue, DefinedLayout.Branch<B> branchOfNode) {
                SerializeOutput missingOutput = makeUpdater(mappedKey);
                DataEntry newEntry = valNode.serialize(missingValue, missingOutput);
                newEntry = readUpdateOptions.getInterprocessor().getHook(DefinedLayout.WRITE_STRUCTURED_ENTRY)
                        .writeNew(newEntry, missingOutput, definedLayout, valNode, branchOfNode);
                if (newEntry != null) {
                    dataTree.put(mappedKey, newEntry);
                }
            }

            @Override
            public <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser,
                                                 SerializeOutput updater) {
                return serializeDeserialize.deserializeUpdate(deser, updater);
            }

            @Override
            public <B> void updateIfDesired(DataTree.Mut dataTree, String mappedKey, DataEntry sourceEntry,
                                            SkeletonNode.Val<?, B> valNode, DeserializeContext deserContext,
                                            DefinedLayout.Branch<B> branchOfNode, SerializeOutput updater) {
                Object update = updater.getAndClearLastOutput();
                if (update == null) {
                    return;
                }
                DataEntry newEntry;
                if (update == NoOutput.INSTANCE) {
                    newEntry = null;
                } else if (sourceEntry.getValue().equals(update)) {
                    return;
                } else {
                    newEntry = sourceEntry.withValue(update);
                }
                newEntry = readUpdateOptions.getInterprocessor()
                        .getHook(DefinedLayout.UPDATE_STRUCTURED_ENTRY)
                        .insertUpdate(newEntry, sourceEntry, deserContext, updater, definedLayout, valNode, branchOfNode);
                if (newEntry == null) {
                    dataTree.remove(mappedKey);
                } else {
                    dataTree.put(mappedKey, newEntry);
                }
            }
        });
        if (result.isSuccess()) {
            readUpdateOptions.getInterprocessor().getHook(DefinedLayout.WRITE_STRUCTURED_TREE)
                    .writeTree(dataTree, new SerContext.Standalone(readUpdateOptions), definedLayout);
        }
        return result;
    }

    @Override
    public void writeTo(@NonNull C config, DataTree.@NonNull Mut dataTree, @NonNull WriteOptions writeOptions) {
        DefinedLayout<C> definedLayout = getDefinedLayout();
        definedLayout.forEachBranch(branch -> {
            TypeSkeleton<? super C> typeSkeleton = (TypeSkeleton<? super C>) branch;
            typeSkeleton.implWriteNodes(reflectionProvider, config, definedLayout, writeOptions, dataTree);
        });
        writeOptions.getInterprocessor().getHook(DefinedLayout.WRITE_STRUCTURED_TREE)
                .writeTree(dataTree, new SerContext.Standalone(writeOptions), definedLayout);
    }
}
