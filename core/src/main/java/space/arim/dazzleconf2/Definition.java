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
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.reflect.*;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.*;

final class Definition<C> implements ConfigurationDefinition<C> {

    private final TypeToken<C> configType;
    private final KeyPath.Immut pathPrefix;

    private final LibraryLang libraryLang;
    private final MethodMirror methodMirror;
    private final Instantiator instantiator;

    /**
     * Includes both this type and all its super types. These arrays have equal length
     */
    private final Class<?>[] superTypesArray;
    private final TypeSkeleton[] skeletonArray;

    private static final InvokeDefaultFunction INVOKE_DEFAULT_VALUE = new InvokeDefaultFunction();

    Definition(TypeToken<C> configType, KeyPath.Immut pathPrefix, Map<Class<?>, TypeSkeleton> typeSkeletons,
               LibraryLang libraryLang, MethodMirror methodMirror, Instantiator instantiator) {
        this.configType = Objects.requireNonNull(configType);
        this.pathPrefix = Objects.requireNonNull(pathPrefix);
        this.libraryLang = Objects.requireNonNull(libraryLang);
        this.methodMirror = Objects.requireNonNull(methodMirror);
        this.instantiator = Objects.requireNonNull(instantiator);

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

    @SuppressWarnings("unchecked")
    private C instantiate(MethodYield.Builder methodYield) {
        return (C) instantiator.generate(
                configType.getRawType().getClassLoader(), superTypesArray, methodYield.build()
        );
    }

    @Override
    public @NonNull TypeToken<C> getType() {
        return configType;
    }

    @Override
    public void walkDefinition(Visit<C> visit, WalkOptions walkOptions) {

        for (int n = 0; n < superTypesArray.length; n++) {
            Class<?> currentType = superTypesArray[n];
            TypeSkeleton typeSkeleton = skeletonArray[n];

            walkDefinitionForType(currentType, typeSkeleton, visit);
        }
    }

    // Simple cache for MethodMirror.Invoker based on the instance provided
    private final class InvocationCache {

        private final Class<?> enclosingType;

        // Build a cache of MethodMirror#makeInvoker based on receiver. If receiver changes, remake the invoker
        private MethodMirror.Invoker lastInvoker;
        private Object lastInvokerReceiver;

        private InvocationCache(Class<?> enclosingType) {
            this.enclosingType = enclosingType;
        }

        MethodMirror.Invoker invokerFor(Object receiver) {
            if (lastInvokerReceiver == receiver) {
                // Good! Found in cache
                return lastInvoker;
            }
            // Cache miss
            MethodMirror.Invoker invoker = methodMirror.makeInvoker(receiver, enclosingType);
            lastInvoker = invoker;
            lastInvokerReceiver = receiver;
            return invoker;
        }
    }

    private static class VisitedMethod<C, S, R> implements Visit.MethodElement<C, S, R> {

        private final MethodId methodId;
        private final Definition<C>.InvocationCache invocationCache;

        private VisitedMethod(MethodId methodId, Definition<C>.InvocationCache invocationCache) {
            this.methodId = methodId;
            this.invocationCache = invocationCache;
        }

        @Override
        public MethodId methodId() {
            return methodId;
        }

        @Override
        public TypeToken<R> returnValue() {
            return new TypeToken<>(methodId.returnType());
        }

        @Override
        public R invokeMethod(C configuration, Object... args) throws Throwable {
            return castReturnType(invocationCache.invokerFor(configuration).invokeMethod(methodId(), args));
        }

        @Override
        public R invokeMethodOnSuper(S implementor, Object... args) throws Throwable {
            return castReturnType(invocationCache.invokerFor(implementor).invokeMethod(methodId(), args));
        }

        private R castReturnType(Object ret) {
            return (R) ret;
        }
    }

    private <S> void walkDefinitionForType(Class<S> currentType, TypeSkeleton typeSkeleton, Visit<C> visit) {
        Visit.ElementVisitor<C, S> visitor = visit.visitTypeInHierarchy(currentType);
        InvocationCache invocationCache = new InvocationCache(currentType);

        for (MethodId callableMethod : typeSkeleton.callableDefaultMethods) {
            class Transparent extends VisitedMethod<C, S, Object> implements Visit.MethodTransparent<C, S, Object> {

                private Transparent(MethodId methodId, InvocationCache invocationCache) {
                    super(methodId, invocationCache);
                }
            }
            visitor.visitTransparent(new Transparent(callableMethod, invocationCache));
        }
        for (TypeSkeleton.MethodNode<?> methodNode : typeSkeleton.methodNodes) {
            // TODO
        }
    }

    @Override
    public @NonNull C loadDefaults() {
        MethodYield.Builder methodYield = new MethodYield.Builder();
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

    private <D extends DataTree, S> @NonNull LoadResult<@NonNull C> readingNexus(
            @NonNull D dataTree, @NonNull ReadOptions readOptions, Definition.@NonNull HowToUpdate<D, S> howToUpdate
    ) {
        // Where we're located - mapped
        KeyPath.Immut mappedPathPrefix;
        {
            KeyPath.Mut mutPathPrefix = this.pathPrefix.intoMut();
            mutPathPrefix.applyKeyMapper(readOptions.keyMapper());
            mappedPathPrefix = mutPathPrefix.intoImmut();
        }
        // What we're building (an instance) and how we're doing that
        MethodYield.Builder methodYield = new MethodYield.Builder();
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
                        KeyPath.Mut keyPath = new KeyPath.Mut();
                        keyPath.addFront(mappedKey);
                        readOptions.loadListener().updatedMissingPath(keyPath);
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

    private interface HowToUpdate<D extends DataTree, S> {

        void insertMissingValue(D dataTree, String mappedKey, TypeSkeleton.MethodNode<?> methodNode, Object value);

        <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser);

        void updateIfDesired(D dataTree, String mappedKey, DataEntry sourceEntry,
                             TypeSkeleton.MethodNode<?> methodNode);

    }

    @Override
    public @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull ReadOptions readOptions) {
        return readingNexus(dataTree, readOptions, new HowToUpdate<DataTree, Void>() {
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
        return readingNexus(dataTree, readOptions, new HowToUpdate<DataTree.Mut, SerializeOutput>() {
            @Override
            public void insertMissingValue(DataTree.Mut dataTree, String mappedKey, TypeSkeleton.MethodNode<?> methodNode,
                                           Object value) {
                dataTree.set(mappedKey, methodNode.addComments(new DataEntry(value)));
            }

            @Override
            public <V> LoadResult<V> deserialize(SerializeDeserialize<V> serializeDeserialize, DeserializeInput deser) {
                return serializeDeserialize.deserializeUpdate(deser, outputForUpdate);
            }

            @Override
            public void updateIfDesired(DataTree.Mut dataTree, String mappedKey, DataEntry sourceEntry,
                                        TypeSkeleton.MethodNode<?> methodNode) {
                Object lastOutput = outputForUpdate.getAndClearLastOutput();
                if (lastOutput != null) {
                    dataTree.set(mappedKey, methodNode.addComments(sourceEntry.withValue(lastOutput)));
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
