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
import space.arim.dazzleconf2.engine.KeyMapper;
import space.arim.dazzleconf2.engine.KeyPath;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.reflect.*;
import space.arim.dazzleconf2.translation.LibraryLang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class Definition<C> implements ConfigurationReadWrite<C> {

    private final TypeToken<C> configType;
    /**
     * Includes both this type and all its super types
     */
    private final Map<Class<?>, TypeSkeleton> superTypes;
    private final LibraryLang libraryLang;
    final Instantiator instantiator;

    private static final InvokeDefaultValue INVOKE_DEFAULT_VALUE = new InvokeDefaultValue();

    Definition(TypeToken<C> configType, Map<Class<?>, TypeSkeleton> superTypes, LibraryLang libraryLang, Instantiator instantiator) {
        this.configType = Objects.requireNonNull(configType);
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
        MethodYield.Builder methodYield = new MethodYield.Builder();

        for (Map.Entry<Class<?>, TypeSkeleton> superTypeEntry : superTypes.entrySet()) {
            Class<?> superType = superTypeEntry.getKey();
            TypeSkeleton typeSkeleton = superTypeEntry.getValue();

            for (TypeSkeleton.MethodNode methodNode : typeSkeleton.methodNodes) {

                Object value;
                String mappedPath = keyMapper.methodNameToKey(methodNode.methodId.name()).toString();
                DataTree.Entry dataEntry = dataTree.get(mappedPath);
                if (dataEntry == null) {
                    value = methodNode.makeMissingValue(superType);
                    KeyPath keyPath = new KeyPath();
                    keyPath.addFront(mappedPath);
                    loadListener.updatedMissingPath(keyPath);
                } else {
                    Operable operable = new Operable(
                            dataEntry.getValue(), mappedPath, new Operable.Context(libraryLang, loadListener, keyMapper)
                    );
                    LoadResult<?> valueResult = methodNode.serializer.deserialize(operable);
                    if (valueResult.isFailure()) {
                        return LoadResult.failure(valueResult.getError().orElseThrow());
                    }
                    value = valueResult.getValue();
                    if (methodNode.optional) value = Optional.of(value);
                }
                methodYield.addValue(superType, methodNode.methodId, value);
            }
        }
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
                Serialization serialization = new Serialization(keyMapper);
                serialization.forceFeed(methodNode.serializer, value);
                Object output = serialization.output;
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

    private static final class Serialization implements SerializeDeserialize.SerializeOutput {

        private Object output;
        private final KeyMapper keyMapper;

        Serialization(KeyMapper keyMapper) {
            this.keyMapper = keyMapper;
        }

        @SuppressWarnings("unchecked")
        private <V> void forceFeed(SerializeDeserialize<V> serializer, Object value) {
            serializer.serialize((V) value, this);
        }

        @Override
        public KeyMapper keyMapper() {
            return keyMapper;
        }

        @Override
        public void outString(String value) {
            output = Objects.requireNonNull(value);
        }

        @Override
        public void outBoolean(boolean value) {
            output = value;
        }

        @Override
        public void outByte(byte value) {
            output = value;
        }

        @Override
        public void outChar(char value) {
            output = value;
        }

        @Override
        public void outShort(short value) {
            output = value;
        }

        @Override
        public void outInt(int value) {
            output = value;
        }

        @Override
        public void outLong(long value) {
            output = value;
        }

        @Override
        public void outFloat(float value) {
            output = value;
        }

        @Override
        public void outDouble(double value) {
            output = value;
        }

        @Override
        public void outList(List<?> value) {
            // Canonical check is performed in Entry constructor
            output = Objects.requireNonNull(value);
        }

        @Override
        public void outDataTree(DataTree value) {
            output = Objects.requireNonNull(value);
        }
    }
}
