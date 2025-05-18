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
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.reflect.*;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.*;
import java.util.stream.Stream;

final class Definition<C> implements ConfigurationDefinition<C> {

    private final TypeToken<C> configType;
    private final KeyPath.Immut pathPrefix;
    private final CommentData topLevelComments;
    private final String[] labels;

    private final LibraryLang libraryLang;
    private final Instantiator instantiator;
    private final MethodMirror methodMirror;

    /**
     * Includes both this type and all its super types. These arrays have equal length
     */
    private final Class<?>[] superTypesArray;
    private final TypeSkeleton[] skeletonArray;

    private static final InvokeDefaultFunction INVOKE_DEFAULT_VALUE = new InvokeDefaultFunction();

    Definition(TypeToken<C> configType, KeyPath pathPrefix, CommentData topLevelComments,
               List<String> labels, LinkedHashMap<Class<?>, TypeSkeleton> typeSkeletons,
               LibraryLang libraryLang, Instantiator instantiator, MethodMirror methodMirror) {
        this.configType = Objects.requireNonNull(configType);
        this.pathPrefix = pathPrefix.intoImmut();
        this.topLevelComments = Objects.requireNonNull(topLevelComments);
        this.labels = labels.toArray(new String[0]);
        this.libraryLang = Objects.requireNonNull(libraryLang);
        this.instantiator = Objects.requireNonNull(instantiator);
        this.methodMirror = Objects.requireNonNull(methodMirror);

        int numberOfSkeletons = typeSkeletons.size();
        Class<?>[] superTypesArray = new Class[numberOfSkeletons];
        TypeSkeleton[] skeletonArray = new TypeSkeleton[numberOfSkeletons];
        int index = 0;
        for (Map.Entry<Class<?>, TypeSkeleton> typeEntry : typeSkeletons.entrySet()) {
            superTypesArray[index] = typeEntry.getKey();
            skeletonArray[index++] = typeEntry.getValue();
        }
        this.superTypesArray = superTypesArray;
        this.skeletonArray = skeletonArray;
    }

    private C instantiate(MethodYield methodYield) {
        return configType.cast(instantiator.generate(superTypesArray, methodYield));
    }

    @Override
    public @NonNull TypeToken<C> getType() {
        return configType;
    }

    @Override
    public @NonNull Layout getLayout() {
        return new Layout() {

            @Override
            public @NonNull CommentData getTopLevelComments() {
                return topLevelComments;
            }

            @Override
            public @NonNull Collection<@NonNull String> getLabels() {
                return Collections.unmodifiableList(Arrays.asList(labels));
            }

            @Override
            public @NonNull Stream<@NonNull String> getLabelsAsStream() {
                return Arrays.stream(labels);
            }
        };
    }

    @Override
    public @NonNull Instantiator getInstantiator() {
        return instantiator;
    }

    @Override
    public @NonNull C loadDefaults() {
        MethodYield methodYield = new MethodYield();
        for (int n = 0; n < superTypesArray.length; n++) {
            Class<?> currentType = superTypesArray[n];
            TypeSkeleton typeSkeleton = skeletonArray[n];
            // Add callable default methods
            for (MethodId callableMethod : typeSkeleton.callableDefaultMethods) {
                methodYield.addValue(currentType, callableMethod, INVOKE_DEFAULT_VALUE);
            }
            // Add default values
            for (TypeSkeleton.MethodNode<?> methodNode : typeSkeleton.methodNodes) {
                Object defaultValue = methodNode.makeDefaultValue(currentType);
                methodYield.addValue(currentType, methodNode.methodId, defaultValue);
            }
        }
        return instantiate(methodYield);
    }

    private <DT extends DataTree> @NonNull LoadResult<@NonNull C> readingNexus(
            @NonNull DT dataTree, @NonNull ReadOptions readOptions, Definition.@NonNull HowToUpdate<DT> howToUpdate
    ) {
        // Where we're located - mapped
        KeyPath.Immut mappedPathPrefix;
        {
            KeyPath.Mut mutPathPrefix = this.pathPrefix.intoMut();
            mutPathPrefix.applyKeyMapper(readOptions.keyMapper());
            mappedPathPrefix = mutPathPrefix.intoImmut();
        }
        // What we're building (an instance) and how we're doing that
        MethodYield methodYield = new MethodYield();
        DeserInput.Context deserContext = new DeserInput.Context(libraryLang, readOptions, mappedPathPrefix);

        // Collected errors - get a certain maximum before quitting
        ErrorContext[] collectedErrors = new ErrorContext[readOptions.maximumErrorCollect()];
        int errorCount = 0;

        // For each type in the hierarchy
        for (int n = 0; n < superTypesArray.length; n++) {

            Class<?> currentType = superTypesArray[n];
            TypeSkeleton typeSkeleton = skeletonArray[n];
            // Add callable default methods
            for (MethodId callableMethod : typeSkeleton.callableDefaultMethods) {
                methodYield.addValue(currentType, callableMethod, INVOKE_DEFAULT_VALUE);
            }

            // Add values for each method
            for (TypeSkeleton.MethodNode<?> methodNode : typeSkeleton.methodNodes) {

                Object value;
                String mappedKey = readOptions.keyMapper().labelToKey(methodNode.methodId.name()).toString();
                DataEntry dataEntry = dataTree.get(mappedKey);
                if (dataEntry == null) {

                    // Missing value. Three possibilities for the method:
                    // 1. Optional -> okay
                    // 2. Mandatory, so fill in the missing value -> okay, signal updated path
                    // 3. Mandatory, and no missing value -> error

                    if (methodNode.optional) {
                        // 1.
                        value = Optional.empty();
                    } else if ((value = methodNode.makeMissingValue(currentType)) != null) {
                        // 2.
                        readOptions.loadListener().updatedPath(
                                new KeyPath.Mut(mappedKey), UpdateReason.MISSING
                        );
                        howToUpdate.insertMissingValue(dataTree, mappedKey, methodNode, value);
                    } else {
                        // 3.
                        LoadError loadError = new LoadError(libraryLang.missingValue(), libraryLang);
                        KeyPath.Mut entryPath = new KeyPath.Mut(mappedPathPrefix);
                        entryPath.addBack(mappedKey);
                        loadError.addDetail(ErrorContext.ENTRY_PATH, entryPath);

                        collectedErrors[errorCount++] = loadError;
                        // Check if errors maxed out
                        if (errorCount == collectedErrors.length) {
                            return LoadResult.failure(Arrays.asList(collectedErrors));
                        }
                        continue;
                    }

                } else {
                    //
                    // Main deserialization route - most cases go here
                    //

                    // Deserialization
                    LoadResult<?> valueResult = howToUpdate.deserialize(methodNode.serializer,  new DeserInput(
                            dataEntry.getValue(), new DeserInput.Source(dataEntry, mappedKey), deserContext
                    ));

                    // Error handling
                    if (valueResult.isFailure()) {
                        // Append all error contexts
                        List<ErrorContext> errorsToAppend = valueResult.getErrorContexts();
                        for (ErrorContext errorToAppend : errorsToAppend) {
                            // Append this error
                            collectedErrors[errorCount++] = errorToAppend;
                            // Check if maxed out
                            if (errorCount == collectedErrors.length) {
                                return LoadResult.failure(Arrays.asList(collectedErrors));
                            }
                        }
                    }
                    // Update if desired
                    howToUpdate.updateIfDesired(dataTree, mappedKey, dataEntry, methodNode);

                    // Bail out if there are errors
                    if (errorCount > 0) continue;
                    // No errors - all good
                    value = valueResult.getOrThrow();
                    if (methodNode.optional) value = Optional.of(value);
                }
                methodYield.addValue(currentType, methodNode.methodId, value);
            }
        }
        // Error handling
        if (errorCount > 0) {
            ErrorContext[] trimmedToSize = Arrays.copyOf(collectedErrors, errorCount);
            return LoadResult.failure(trimmedToSize);
        }
        // No errors - success
        return LoadResult.of(instantiate(methodYield));
    }

    private interface HowToUpdate<DT extends DataTree> {

        void insertMissingValue(DT dataTree, String mappedKey, TypeSkeleton.MethodNode<?> methodNode, Object value);

        <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser);

        void updateIfDesired(DT dataTree, String mappedKey, DataEntry sourceEntry,
                             TypeSkeleton.MethodNode<?> methodNode);

    }

    @Override
    public @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull ReadOptions readOptions) {
        return readingNexus(dataTree, readOptions, new HowToUpdate<DataTree>() {
            @Override
            public void insertMissingValue(DataTree dataTree, String mappedKey, TypeSkeleton.MethodNode<?> methodNode,
                                           Object value) {}

            @Override
            public <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser) {
                return serializeDeserialize.deserialize(deser);
            }

            @Override
            public void updateIfDesired(DataTree dataTree, String mappedKey, DataEntry sourceEntry,
                                        TypeSkeleton.MethodNode<?> methodNode) {}
        });
    }

    @Override
    public @NonNull LoadResult<@NonNull C> readWithUpdate(DataTree.@NonNull Mut dataTree, @NonNull ReadOptions readOptions) {

        SerializeOutput outputForUpdate = new SerOutput(readOptions.keyMapper());
        return readingNexus(dataTree, readOptions, new HowToUpdate<DataTree.Mut>() {
            @Override
            public void insertMissingValue(DataTree.Mut dataTree, String mappedKey, TypeSkeleton.MethodNode<?> methodNode,
                                           Object value) {
                dataTree.set(mappedKey, new DataEntry(value).withComments(methodNode.comments));
            }

            @Override
            public <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser) {
                return serializeDeserialize.deserializeUpdate(deser, outputForUpdate);
            }

            @Override
            public void updateIfDesired(DataTree.Mut dataTree, String mappedKey, DataEntry sourceEntry,
                                        TypeSkeleton.MethodNode<?> methodNode) {
                Object update = outputForUpdate.getAndClearLastOutput();
                if (update != null && !sourceEntry.getValue().equals(update)) {
                    readOptions.loadListener().updatedPath(new KeyPath.Mut(mappedKey), UpdateReason.UPDATED);
                    dataTree.set(mappedKey, sourceEntry.withValue(update));
                }
            }
        });
    }

    @Override
    public void writeTo(@NonNull C config, DataTree.@NonNull Mut dataTree, @NonNull WriteOptions writeOptions) {

        SerializeOutput serOutput = new SerOutput(writeOptions.keyMapper());

        for (int n = 0; n < superTypesArray.length; n++) {
            MethodMirror.Invoker invoker = methodMirror.makeInvoker(config, superTypesArray[n]);

            for (TypeSkeleton.MethodNode<?> methodNode : skeletonArray[n].methodNodes) {
                String mappedKey = writeOptions.keyMapper().labelToKey(methodNode.methodId.name()).toString();
                DataEntry entry = methodNode.serialize(invoker, serOutput);
                dataTree.set(mappedKey, entry);
            }
        }
    }
}
