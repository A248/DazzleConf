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
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.reflect.InvokeDefaultFunction;
import space.arim.dazzleconf2.reflect.MethodId;
import space.arim.dazzleconf2.reflect.MethodMirror;
import space.arim.dazzleconf2.reflect.MethodYield;
import space.arim.dazzleconf2.reflect.ReifiedType;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class Definition<C> implements ConfigurationDefinition<C> {

    private final TypeToken<C> configType;
    private final CommentData topLevelComments;

    private final LibraryLang libraryLang;
    private final Instantiator instantiator;
    private final MethodMirror methodMirror;

    /**
     * Includes both this type and all its super types. These arrays have equal length
     */
    private final Class<?>[] superTypesArray;
    private final TypeSkeleton[] skeletonArray;

    private static final InvokeDefaultFunction INVOKE_DEFAULT_VALUE = new InvokeDefaultFunction();

    Definition(TypeToken<C> configType, CommentData topLevelComments,
               LinkedHashMap<Class<?>, TypeSkeleton> typeSkeletons, LibraryLang libraryLang,
               Instantiator instantiator, MethodMirror methodMirror) {
        this.configType = Objects.requireNonNull(configType);
        this.topLevelComments = Objects.requireNonNull(topLevelComments);
        this.libraryLang = Objects.requireNonNull(libraryLang);
        this.instantiator = Objects.requireNonNull(instantiator);
        this.methodMirror = Objects.requireNonNull(methodMirror);

        int numberOfSkeletons = typeSkeletons.size();
        Class<?>[] superTypesArray = new Class[numberOfSkeletons];
        TypeSkeleton[] skeletonArray = new TypeSkeleton[numberOfSkeletons];
        // Start from the back: This means we read/write parent types first
        int index = numberOfSkeletons;
        for (Map.Entry<Class<?>, TypeSkeleton> typeEntry : typeSkeletons.entrySet()) {
            superTypesArray[--index] = typeEntry.getKey();
            skeletonArray[index] = typeEntry.getValue();
        }
        this.superTypesArray = superTypesArray;
        this.skeletonArray = skeletonArray;
    }

    @Override
    public @NonNull TypeToken<C> getType() {
        return configType;
    }

    @Override
    public @NonNull Layout getLayout() {
        return new Layout() {

            @Override
            public ReifiedType.@NonNull Annotated getReifiedType() {
                return configType.getReifiedType();
            }

            @Override
            public @NonNull CommentData getComments() {
                return topLevelComments;
            }

            @Override
            public @NonNull Instantiator getInstantiator() {
                return instantiator;
            }

            @Override
            public @NonNull MethodMirror getMethodMirror() {
                return methodMirror;
            }
        };
    }

    @Override
    public @NonNull C loadDefaults() {
        MethodYield methodYield = new MethodYield();
        for (int n = 0; n < superTypesArray.length; n++) {
            Class<?> currentType = superTypesArray[n];
            TypeSkeleton typeSkeleton = skeletonArray[n];
            // Add callable default methods
            for (MethodId callableMethod : typeSkeleton.callableDefaultMethods) {
                methodYield.addEntry(currentType, callableMethod, INVOKE_DEFAULT_VALUE);
            }
            // Add default values
            for (TypeSkeleton.MethodNode<?> methodNode : typeSkeleton.methodNodes) {
                Object defaultValue = methodNode.makeDefaultValue(currentType);
                methodYield.addEntry(currentType, methodNode.methodId, defaultValue);
            }
        }
        return instantiator.generate(configType.getRawType(), methodYield);
    }

    private <DT extends DataTree> @NonNull LoadResult<@NonNull C> readingNexus(
            @NonNull DT dataTree, @NonNull ReadOptions readOptions, @NonNull HowToUpdate<DT> howToUpdate
    ) {
        // What we're building (an instance) and how we're doing that
        MethodYield methodYield = new MethodYield();
        DeserInput.Context deserContext = new DeserInput.Context(libraryLang, readOptions);

        // Collected errors - get a certain maximum before quitting
        ErrorContext[] collectedErrors = new ErrorContext[readOptions.maximumErrorCollect()];
        int errorCount = 0;

        // For each type in the hierarchy
        for (int n = 0; n < superTypesArray.length; n++) {

            Class<?> currentType = superTypesArray[n];
            TypeSkeleton typeSkeleton = skeletonArray[n];
            // Add callable default methods
            for (MethodId callableMethod : typeSkeleton.callableDefaultMethods) {
                methodYield.addEntry(currentType, callableMethod, INVOKE_DEFAULT_VALUE);
            }

            // Add values for each method
            for (TypeSkeleton.MethodNode<?> methodNode : typeSkeleton.methodNodes) {

                ErrorContext[] errorContexts = readingNexusForEntry(
                        methodYield, deserContext, currentType, methodNode, dataTree, readOptions, howToUpdate
                );
                if (errorContexts != null) {
                    for (ErrorContext errorToAppend : errorContexts) {
                        // Append this error
                        collectedErrors[errorCount++] = errorToAppend;
                        // Check if maxed out
                        if (errorCount == collectedErrors.length) {
                            return LoadResult.failure(collectedErrors);
                        }
                    }
                }
            }
        }
        // Error handling
        if (errorCount > 0) {
            ErrorContext[] trimmedToSize = Arrays.copyOf(collectedErrors, errorCount);
            return LoadResult.failure(trimmedToSize);
        }
        // No errors - success
        return LoadResult.of(instantiator.generate(configType.getRawType(), methodYield));
    }

    private <DT extends DataTree, V> @NonNull ErrorContext @Nullable [] readingNexusForEntry(
            @NonNull MethodYield methodYield, DeserInput.@NonNull Context deserContext, @NonNull Class<?> currentType,
            TypeSkeleton.@NonNull MethodNode<V> methodNode, @NonNull DT dataTree, @NonNull ReadOptions readOptions,
            @NonNull HowToUpdate<DT> howToUpdate) {

        Object value;
        V missingValue;
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
            } else if ((value = missingValue = methodNode.makeMissingValue(currentType)) != null) {
                // 2.
                readOptions.notifyUpdate(new KeyPath.Mut(mappedKey), UpdateReason.MISSING);
                howToUpdate.insertMissingValue(dataTree, mappedKey, methodNode, missingValue);
            } else {
                // 3.
                LoadError loadError = new LoadError(Printable.preBuilt(libraryLang.missingValue()), libraryLang);
                KeyPath.Mut entryPath = new KeyPath.Mut(readOptions.keyPath());
                entryPath.addBack(mappedKey);
                loadError.addDetail(ErrorContext.ENTRY_PATH, entryPath);
                return new ErrorContext[] {loadError};
            }
        } else {
            //
            // Main deserialization route - most cases go here
            //

            // Deserialization
            LoadResult<V> valueResult = howToUpdate.deserialize(
                    methodNode.serializer, new DeserInput.Base(dataEntry, mappedKey, deserContext)
            );
            // Error handling
            if (valueResult.isFailure()) {
                return valueResult.getErrorContexts().toArray(new ErrorContext[0]);
            }
            // Update if desired
            howToUpdate.updateIfDesired(dataTree, mappedKey, dataEntry, methodNode);

            // No errors - all good
            value = valueResult.getOrThrow();
            if (methodNode.optional) value = Optional.of(value);
        }
        methodYield.addEntry(currentType, methodNode.methodId, value);
        return null;
    }

    private interface HowToUpdate<DT extends DataTree> {

        <V> void insertMissingValue(DT dataTree, String mappedKey, TypeSkeleton.MethodNode<V> methodNode, V missingValue);

        <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser);

        void updateIfDesired(DT dataTree, String mappedKey, DataEntry sourceEntry,
                             TypeSkeleton.MethodNode<?> methodNode);

    }

    @Override
    public @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull ReadOptions readOptions) {
        return readingNexus(dataTree, readOptions, new HowToUpdate<DataTree>() {
            @Override
            public <V> void insertMissingValue(DataTree dataTree, String mappedKey,
                                               TypeSkeleton.MethodNode<V> methodNode, V missingValue) {}

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
    public @NonNull LoadResult<@NonNull C> readWithUpdate(DataTree.@NonNull Mut dataTree, @NonNull ReadWithUpdateOptions readOptions) {

        // Updating comments, based on read options, usually depends on whether the backend needs it
        ModifyComments modifyComments = new ModifyComments(readOptions);
        // Updating values is based on calls to deserializeUpdate
        SerializeOutput outputForUpdate = new SerOutput(readOptions.keyMapper(), modifyComments);

        return readingNexus(dataTree, readOptions, new HowToUpdate<DataTree.Mut>() {
            @Override
            public <V> void insertMissingValue(DataTree.Mut dataTree, String mappedKey,
                                               TypeSkeleton.MethodNode<V> methodNode, V missingValue) {
                DataEntry serializedMissingValue = methodNode.serialize(missingValue, outputForUpdate);
                dataTree.set(mappedKey, serializedMissingValue.withComments(methodNode.comments));
            }

            @Override
            public <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser) {
                return serializeDeserialize.deserializeUpdate(deser, outputForUpdate);
            }

            @Override
            public void updateIfDesired(DataTree.Mut dataTree, String mappedKey, DataEntry sourceEntry,
                                        TypeSkeleton.MethodNode<?> methodNode) {
                boolean changed = false;
                Object update = outputForUpdate.getAndClearLastOutput();
                if (update != null && !sourceEntry.getValue().equals(update)) {
                    sourceEntry = sourceEntry.withValue(update);
                    changed = true;
                }
                if (modifyComments.isAnyLocationEnabled()) {
                    sourceEntry = modifyComments.applyTo(sourceEntry, methodNode.comments);
                    changed = true;
                }
                if (changed) {
                    dataTree.set(mappedKey, sourceEntry);
                }
            }
        });
    }

    @Override
    public void writeTo(@NonNull C config, DataTree.@NonNull Mut dataTree, @NonNull WriteOptions writeOptions) {

        ModifyComments modifyComments = new ModifyComments(writeOptions);
        SerializeOutput serOutput = new SerOutput(writeOptions.keyMapper(), modifyComments);

        for (int n = 0; n < superTypesArray.length; n++) {
            MethodMirror.Invoker invoker = methodMirror.makeInvoker(config, superTypesArray[n]);

            for (TypeSkeleton.MethodNode<?> methodNode : skeletonArray[n].methodNodes) {
                String mappedKey = writeOptions.keyMapper().labelToKey(methodNode.methodId.name()).toString();
                DataEntry entry = methodNode.serialize(invoker, serOutput);
                if (entry != null && modifyComments.isAnyLocationEnabled()) {
                    entry = modifyComments.applyTo(entry, methodNode.comments);
                }
                dataTree.set(mappedKey, entry);
            }
        }
    }
}
