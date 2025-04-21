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
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DataTreeMut;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.reflect.*;
import space.arim.dazzleconf2.translation.LibraryLang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

final class Definition<C> implements ConfigurationReadWrite<C> {

    private final TypeToken<C> configType;
    /**
     * Prefix for where this configuration or subsection is located
     */
    private final String[] pathPrefix;
    /**
     * Includes both this type and all its super types
     */
    private final Map<Class<?>, TypeSkeleton> superTypes;
    private final LibraryLang libraryLang;
    final Instantiator instantiator;

    private static final InvokeDefaultValue INVOKE_DEFAULT_VALUE = new InvokeDefaultValue();

    Definition(TypeToken<C> configType, String[] pathPrefix, Map<Class<?>, TypeSkeleton> superTypes,
               LibraryLang libraryLang, Instantiator instantiator) {
        this.configType = Objects.requireNonNull(configType);
        this.pathPrefix = pathPrefix;
        this.superTypes = ImmutableCollections.mapOf(superTypes);
        this.libraryLang = libraryLang;
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
    public C loadDefaults() {
        MethodYield.Builder methodYield = new MethodYield.Builder();
        superTypes.forEach((superType, typeSkeleton) -> {
            typeSkeleton.methodNodes.forEach(methodNode -> {
                Object defaultValue = methodNode.makeDefaultValue(superType);
                methodYield.addValue(superType, methodNode.methodId, defaultValue);
            });
        });
        return instantiate(methodYield);
    }

    @Override
    public LoadResult<C> readWithKeyMapper(DataTree dataTree, LoadListener loadListener, KeyMapper keyMapper) {
        // What we're building (an instance) and how we're doing that
        MethodYield.Builder methodYield = new MethodYield.Builder();
        DeserInput.Context deserCtx = new DeserInput.Context(libraryLang, loadListener, keyMapper);

        // Collected errors - get a maximum of 10 before quitting
        ErrorContext[] collectedErrors = new ErrorContext[10];
        int errorCount = 0;

        // For each type in the hierarchy
        for (Map.Entry<Class<?>, TypeSkeleton> superTypeEntry : superTypes.entrySet()) {
            Class<?> superType = superTypeEntry.getKey();
            TypeSkeleton typeSkeleton = superTypeEntry.getValue();

            // For each method in that type
            for (TypeSkeleton.MethodNode methodNode : typeSkeleton.methodNodes) {

                Object value;
                String mappedKey = keyMapper.methodNameToKey(methodNode.methodId.name()).toString();
                DataTree.Entry dataEntry = dataTree.get(mappedKey);
                if (dataEntry == null) {
                    // No point in continuing if there are previous errors
                    if (errorCount > 0) continue;
                    // If non-optional, fetch the missing value
                    if (methodNode.optional) {
                        value = Optional.empty();
                    } else {
                        value = methodNode.makeMissingValue(superType);
                        // And signal that path was updated
                        KeyPath keyPath = new KeyPath();
                        keyPath.addFront(mappedKey);
                        loadListener.updatedMissingPath(keyPath);
                    }
                } else {
                    // Main deserialization route - most cases go here
                    LoadResult<?> valueResult = methodNode.serializer.deserialize(new DeserInput(
                            dataEntry.getValue(), new DeserInput.Source(pathPrefix, mappedKey, dataEntry), deserCtx
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
                    // Bail out if there are errors
                    if (errorCount > 0) continue;
                    // No errors - all good
                    value = valueResult.getValue().orElseThrow();
                    if (methodNode.optional) value = Optional.of(value);
                }
                methodYield.addValue(superType, methodNode.methodId, value);
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

    @Override
    public void writeWithKeyMapper(C config, DataTreeMut dataTree, KeyMapper keyMapper) {

        for (Map.Entry<Class<?>, TypeSkeleton> superTypeEntry : superTypes.entrySet()) {
            Class<?> superType = superTypeEntry.getKey();
            TypeSkeleton typeSkeleton = superTypeEntry.getValue();

            for (TypeSkeleton.MethodNode methodNode : typeSkeleton.methodNodes) {

                Object value;
                Method method = methodNode.methodId.getMethod(superType);
                try {
                    value = method.invoke(config);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new DeveloperMistakeException("Configuration methods must not throw exceptions", e);
                }
                if (methodNode.optional && (value = ((Optional<?>) value).orElse(null)) == null) {
                    continue;
                }
                SerOutput serOutput = new SerOutput(keyMapper);
                serOutput.forceFeed(methodNode.serializer, value);
                Object output = serOutput.output;
                if (output == null) {
                    throw new DeveloperMistakeException(
                            "Serializer " + methodNode.serializer + " did not produce any output for " + value
                    );
                }
                DataTree.Entry dataEntry = new DataTree.Entry(output)
                        // TODO: Add comments here
                        .withComments(DataTree.CommentLocation.ABOVE, ImmutableCollections.emptyList());
                String mappedPath = keyMapper.methodNameToKey(methodNode.methodId.name()).toString();
                dataTree.set(mappedPath, dataEntry);
            }
        }
    }

}
