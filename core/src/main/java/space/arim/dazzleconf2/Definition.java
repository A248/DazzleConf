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
import space.arim.dazzleconf2.internals.ImmutableCollections;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DataTreeMut;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.reflect.*;
import space.arim.dazzleconf2.internals.LibraryLang;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

final class Definition<C> implements ConfigurationDefinition<C> {

    private final TypeToken<C> configType;
    private final KeyPath pathPrefix;
    /**
     * Includes both this type and all its super types
     */
    private final Map<Class<?>, TypeSkeleton> superTypes;
    private final LibraryLang libraryLang;
    private final MethodMirror methodMirror;
    private final Instantiator instantiator;

    private static final InvokeDefaultValue INVOKE_DEFAULT_VALUE = new InvokeDefaultValue();

    Definition(TypeToken<C> configType, KeyPath pathPrefix, Map<Class<?>, TypeSkeleton> superTypes,
               LibraryLang libraryLang, MethodMirror methodMirror, Instantiator instantiator) {
        this.configType = Objects.requireNonNull(configType);
        this.pathPrefix = Objects.requireNonNull(pathPrefix);
        this.superTypes = ImmutableCollections.mapOf(superTypes);
        this.libraryLang = Objects.requireNonNull(libraryLang);
        this.methodMirror = Objects.requireNonNull(methodMirror);
        this.instantiator = Objects.requireNonNull(instantiator);
    }

    private C instantiate(MethodYield.Builder methodYield) {
        // Add callable default methods
        superTypes.forEach((type, typeSkeleton) -> {
            typeSkeleton.callableDefaultMethods.forEach(callableMethod -> {
               methodYield.addValue(type, callableMethod, INVOKE_DEFAULT_VALUE);
            });
        });
        // Instantiate
        Class<C> rawConfigType = configType.getRawType();
        return rawConfigType.cast(instantiator.generate(
                rawConfigType.getClassLoader(), superTypes.keySet(), methodYield.build()
        ));
    }

    @Override
    public @NonNull TypeToken<C> getType() {
        return configType;
    }

    @Override
    public @NonNull C loadDefaults() {
        MethodYield.Builder methodYield = new MethodYield.Builder();
        superTypes.forEach((superType, typeSkeleton) -> {
            typeSkeleton.methodNodes.forEach(methodNode -> {
                Object defaultValue = methodNode.makeDefaultValue(superType);
                methodYield.addValue(superType, methodNode.methodId, defaultValue);
            });
        });
        return instantiate(methodYield);
    }

    private <D extends DataTree, S> @NonNull LoadResult<@NonNull C> readingNexus(
            @NonNull D dataTree, @NonNull ReadOptions readOptions, Definition.@NonNull HowToUpdate<D, S> howToUpdate
    ) {
        // Where we're located - mapped
        // TODO Add key mapper support to KeyPath
        //KeyPath pathPrefix = new KeyPath(this.pathPrefix, readOptions.keyMapper());

        // What we're building (an instance) and how we're doing that
        MethodYield.Builder methodYield = new MethodYield.Builder();
        DeserInput.Context deserContext = new DeserInput.Context(libraryLang, readOptions, pathPrefix);

        // Collected errors - get a certain maximum before quitting
        ErrorContext[] collectedErrors = new ErrorContext[readOptions.maximumErrorCollect()];
        int errorCount = 0;

        // For each type in the hierarchy
        for (Map.Entry<Class<?>, TypeSkeleton> superTypeEntry : superTypes.entrySet()) {
            Class<?> currentType = superTypeEntry.getKey();
            TypeSkeleton typeSkeleton = superTypeEntry.getValue();

            // For each method in that type
            for (TypeSkeleton.MethodNode methodNode : typeSkeleton.methodNodes) {

                Object value;
                String mappedKey = readOptions.keyMapper().methodNameToKey(methodNode.methodId.name()).toString();
                DataTree.Entry dataEntry = dataTree.get(mappedKey);
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
                        KeyPath keyPath = new KeyPath();
                        keyPath.addFront(mappedKey);
                        readOptions.loadListener().updatedMissingPath(keyPath);
                        howToUpdate.insertMissingValue(dataTree, mappedKey, methodNode, value);
                    } else {
                        // 3.
                        LoadError loadError = new LoadError(libraryLang.missingValue(), libraryLang);
                        KeyPath entryPath = new KeyPath(pathPrefix);
                        entryPath.addBack(mappedKey);
                        loadError.addDetail(ErrorContext.ENTRY_PATH, entryPath.intoPartsList());

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

                    // Updatable output if needed
                    S outputForUpdate = howToUpdate.makeOutputForUpdate(readOptions);

                    // Deserialization
                    LoadResult<?> valueResult = howToUpdate.deserialize(methodNode.serializer,  new DeserInput(
                            dataEntry.getValue(), new DeserInput.Source(dataEntry, mappedKey), deserContext
                    ), outputForUpdate);

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
                    howToUpdate.updateIfDesired(dataTree, mappedKey, dataEntry, methodNode, outputForUpdate);

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

    private interface HowToUpdate<D extends DataTree, S> {

        void insertMissingValue(D dataTree, String mappedKey, TypeSkeleton.MethodNode methodNode, Object value);

        S makeOutputForUpdate(ReadOptions readOpts);

        <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize,
                                      DeserializeInput deser, S outputForUpdate);

        void updateIfDesired(D dataTree, String mappedKey, DataTree.Entry sourceEntry,
                             TypeSkeleton.MethodNode methodNode, S outputForUpdate);

    }

    @Override
    public @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull ReadOptions readOptions) {
        return readingNexus(dataTree, readOptions, new HowToUpdate<DataTree, Void>() {
            @Override
            public void insertMissingValue(DataTree dataTree, String mappedKey, TypeSkeleton.MethodNode methodNode,
                                           Object value) {}

            @Override
            public Void makeOutputForUpdate(ReadOptions readOpts) {
                return null;
            }

            @Override
            public <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize,
                                                 DeserializeInput deser, Void outputForUpdate) {
                return serializeDeserialize.deserialize(deser);
            }

            @Override
            public void updateIfDesired(DataTree dataTree, String mappedKey, DataTree.Entry sourceEntry,
                                        TypeSkeleton.MethodNode methodNode, Void outputForUpdate) {}
        });
    }

    @Override
    public @NonNull LoadResult<@NonNull C> readWithUpdate(@NonNull DataTreeMut dataTree, @NonNull ReadOptions readOptions) {
        return readingNexus(dataTree, readOptions, new HowToUpdate<DataTreeMut, SerOutput>() {
            @Override
            public void insertMissingValue(DataTreeMut dataTree, String mappedKey, TypeSkeleton.MethodNode methodNode,
                                           Object value) {
                dataTree.set(mappedKey, methodNode.addComments(new DataTree.Entry(value)));
            }

            @Override
            public SerOutput makeOutputForUpdate(ReadOptions readOpts) {
                return new SerOutput(readOpts.keyMapper());
            }

            @Override
            public <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser,
                                                 SerOutput outputForUpdate) {
                return serializeDeserialize.deserializeUpdate(deser, outputForUpdate);
            }

            @Override
            public void updateIfDesired(DataTreeMut dataTree, String mappedKey, DataTree.Entry sourceEntry,
                                        TypeSkeleton.MethodNode methodNode, SerOutput outputForUpdate) {
                if (outputForUpdate.output != null) {
                    dataTree.set(mappedKey, methodNode.addComments(sourceEntry.withValue(outputForUpdate.output)));
                }
            }
        });
    }

    @Override
    public void writeTo(@NonNull C config, @NonNull DataTreeMut dataTree, @NonNull WriteOptions writeOptions) {

        for (Map.Entry<Class<?>, TypeSkeleton> superTypeEntry : superTypes.entrySet()) {
            Class<?> currentType = superTypeEntry.getKey();
            TypeSkeleton typeSkeleton = superTypeEntry.getValue();
            MethodMirror.Invoker invoker = methodMirror.makeInvoker(config, currentType);

            for (TypeSkeleton.MethodNode methodNode : typeSkeleton.methodNodes) {

                Object value;
                try {
                    value = invoker.invokeMethod(methodNode.methodId);
                } catch (InvocationTargetException e) {
                    throw new DeveloperMistakeException("Configuration methods must not throw exceptions", e);
                }
                if (value == null) {
                    throw new DeveloperMistakeException(
                            "Configuration method " + methodNode.methodId + " must not return null"
                    );
                }
                if (methodNode.optional && (value = ((Optional<?>) value).orElse(null)) == null) {
                    continue;
                }
                SerOutput serOutput = new SerOutput(writeOptions.keyMapper());
                serOutput.forceFeed(methodNode.serializer, value);

                if (serOutput.output == null) {
                    throw new DeveloperMistakeException(
                            "Serializer " + methodNode.serializer + " did not produce any output for " + value
                    );
                }
                dataTree.set(
                        writeOptions.keyMapper().methodNameToKey(methodNode.methodId.name()).toString(),
                        methodNode.addComments(new DataTree.Entry(serOutput.output))
                );
            }
        }
    }
}
