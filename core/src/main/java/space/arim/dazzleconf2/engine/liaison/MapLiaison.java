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

package space.arim.dazzleconf2.engine.liaison;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.reflect.ReifiedType;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Liaison for maps.
 * <p>
 * This liaison will match all {@code Map<K, V>}. It will use the relevant serializers for the key and value type,
 * respectively, to build the relevant map.
 * <p>
 * <b>Limitations</b>
 * <p>
 * This liaison does not, and <b>cannot</b>, preserve entry metadata, such as comments, on map values across separate
 * acts of deserialization and serialization. Only a {@link SerializeDeserialize#deserializeUpdate} operation can
 * preserve this entry metadata.
 * <p>
 * <b>Duplicate Keys</b>
 * <p>
 * Note that despite this liaison using a {@link space.arim.dazzleconf2.backend.DataTree} as its input, it is still
 * possible for keys to be deserialized in a way that produces duplicates. Deserialization, in general, is not a 1-to-1
 * mapping, and the key type can implement different equality semantics than the input data, in addition to mapping
 * from different input types. For example, {@code "1"} and {@code 1} might both be deserialized as {@code 1}.
 * <p>
 * The policy of this liaison is to silently skip keys that become duplicated due to deserialization. This behavior was
 * chosen because such duplication usually arises from user input. There is no sort of "merge" algorithm for
 * values; additionally, it is not defined which key will be selected.
 * <p>
 * Serialization can also produce duplicate keys. This can happen if the key serializer implementation chooses to
 * produce identical output keys, either during regular serialization or during a read-update operation via
 * {@link SerializeDeserialize#deserializeUpdate(DeserializeInput, SerializeOutput)}. This liaison considers such
 * duplication a mistake, and it will throw a {@link DeveloperMistakeException} if conflicting output keys are detected.
 * <p>
 * <b>Consistent Order</b>
 * <p>
 * This liaison provides a consistent order across deserialization and re-serialization. It is implemented internally
 * over an immutable wrapper of {@link java.util.LinkedHashSet}.
 */
public final class MapLiaison implements TypeLiaison {

    /**
     * Creates the liaison
     *
     */
    public MapLiaison() {}

    @Override
    @SideEffectFree
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        if (typeToken.getRawType().equals(Map.class)) {
            ReifiedType reifiedType = typeToken.getReifiedType();
            TypeToken<?> keyToken = new TypeToken<>(reifiedType.argumentAt(0));
            TypeToken<?> valueToken = new TypeToken<>(reifiedType.argumentAt(1));
            Agent<?> agentImpl = new AgentImpl<>(
                    handshake.getOtherSerializer(keyToken), handshake.getOtherSerializer(valueToken)
            );
            @SuppressWarnings("unchecked")
            Agent<V> castAgent = (Agent<V>) agentImpl;
            return castAgent;
        }
        return null;
    }

    private static final class AgentImpl<K, V> implements Agent<Map<K, V>>, SerializeDeserialize<Map<K, V>> {

        private final SerializeDeserialize<K> keySerializer;
        private final SerializeDeserialize<V> valueSerializer;

        private AgentImpl(SerializeDeserialize<K> keySerializer, SerializeDeserialize<V> valueSerializer) {
            this.keySerializer = keySerializer;
            this.valueSerializer = valueSerializer;
        }

        @Override
        public @NonNull SerializeDeserialize<Map<K, V>> makeSerializer() {
            return this;
        }

        private <D extends DataTree> @NonNull LoadResult<@NonNull Map<K, V>> implDeserialize(
                @NonNull DeserializeInput deser, @NonNull ImplDeserialize<D, K, V> implDeserialize
        ) {
            // In order to reduce stack depth, avoid functions like LoadResult#flatMap
            LoadResult<DataTree> dataTreeResult = deser.requireDataTree();
            if (dataTreeResult.isFailure()) {
                return LoadResult.failure(dataTreeResult.getErrorContexts());
            }
            D input = implDeserialize.prepare(dataTreeResult.getOrThrow());
            // Error handling - get a certain maximum before quitting, becomes non-null if we find at least 1 error
            ErrorContext[] collectedErrors = null;
            int errorCount = 0;

            Map<K, V> built = new LinkedHashMap<>();

            for (Object inputKey : input.keySet()) {
                // Deserialize the key
                DataEntry inputEntry = input.get(inputKey);
                if (inputEntry == null) {
                    throw new IllegalStateException(
                            "Key " + inputKey + " is part of keySet() but not in the data tree itself"
                    );
                }
                LoadResult<K> keyResult = implDeserialize.deserialize(
                        keySerializer, deser.makeChild(inputEntry.withValue(inputKey), inputKey)
                );
                if (keyResult.isFailure()) {
                    if (collectedErrors == null) {
                        collectedErrors = new ErrorContext[deser.maximumErrorCollect()];
                    }
                    for (ErrorContext errorToAppend : keyResult.getErrorContexts()) {
                        // Append this error
                        collectedErrors[errorCount++] = errorToAppend;
                        // Check if maxed out
                        if (errorCount == collectedErrors.length) {
                            return LoadResult.failure(collectedErrors);
                        }
                    }
                    continue;
                }
                K key = keyResult.getOrThrow();
                Object keyUpdate = implDeserialize.getUpdateFromDeserializeCall();

                LoadResult<V> valueResult = implDeserialize.deserialize(
                        valueSerializer, deser.makeChild(inputEntry, inputKey)
                );
                if (valueResult.isFailure()) {
                    if (collectedErrors == null) {
                        collectedErrors = new ErrorContext[deser.maximumErrorCollect()];
                    }
                    for (ErrorContext errorToAppend : valueResult.getErrorContexts()) {
                        // Append this error
                        collectedErrors[errorCount++] = errorToAppend;
                        // Check if maxed out
                        if (errorCount == collectedErrors.length) {
                            return LoadResult.failure(collectedErrors);
                        }
                    }
                    continue;
                }
                V value = valueResult.getOrThrow();
                Object valueUpdate = implDeserialize.getUpdateFromDeserializeCall();
                implDeserialize.updateIfDesired(input, inputKey, inputEntry, keyUpdate, valueUpdate);

                built.put(key, value);
            }
            // Error handling
            if (collectedErrors != null) {
                return LoadResult.failure(Arrays.copyOf(collectedErrors, errorCount));
            }
            // Finish recording updates - check if size changed
            if (input.size() != built.size()) {
                // Note that size-related updates can't happen during iteration itself (concurrent modification)
                implDeserialize.updateSizeShrunk(deser, built);
            } else {
                implDeserialize.updateMaybeOtherwise(deser, input);
            }
            return LoadResult.of(Collections.unmodifiableMap(built));
        }

        interface ImplDeserialize<D extends DataTree, K, V> {

            D prepare(DataTree dataTree);

            <E> LoadResult<E> deserialize(SerializeDeserialize<E> serializer, DeserializeInput deser);

            @Nullable Object getUpdateFromDeserializeCall();

            void updateIfDesired(D updatableInput, Object inputKey, DataEntry inputEntry,
                                 @Nullable Object keyUpdate, @Nullable Object valueUpdate);

            void updateSizeShrunk(DeserializeInput deser, Map<K, V> built);

            void updateMaybeOtherwise(DeserializeInput deser, D updatableInput);

        }
        @Override
        public @NonNull LoadResult<@NonNull Map<K, V>> deserialize(@NonNull DeserializeInput deser) {
            return implDeserialize(deser, new ImplDeserialize<DataTree, K, V>() {
                @Override
                public DataTree prepare(DataTree dataTree) {
                    return dataTree;
                }

                @Override
                public <E> LoadResult<E> deserialize(SerializeDeserialize<E> serializer, DeserializeInput deser) {
                    return serializer.deserialize(deser);
                }

                @Override
                public @Nullable Object getUpdateFromDeserializeCall() {
                    return null;
                }

                @Override
                public void updateIfDesired(DataTree updatableInput, Object inputKey, DataEntry inputEntry,
                                            @Nullable Object keyUpdate, @Nullable Object valueUpdate) {}

                @Override
                public void updateSizeShrunk(DeserializeInput deser, Map<K, V> built) {
                    deser.notifyUpdate(KeyPath.empty(), UpdateReason.OTHER);
                }

                @Override
                public void updateMaybeOtherwise(DeserializeInput deser, DataTree updatableInput) {}
            });
        }

        @Override
        public @NonNull LoadResult<@NonNull Map<K, V>> deserializeUpdate(@NonNull DeserializeInput deser, @NonNull SerializeOutput updateTo) {
            return implDeserialize(deser, new ImplDeserialize<DataTree.Mut, K, V>() {

                private boolean updated;

                @Override
                public DataTree.Mut prepare(DataTree dataTree) {
                    return dataTree.intoMut();
                }

                @Override
                public <E> LoadResult<E> deserialize(SerializeDeserialize<E> serializer, DeserializeInput deser) {
                    return serializer.deserializeUpdate(deser, updateTo);
                }

                @Override
                public @Nullable Object getUpdateFromDeserializeCall() {
                    return updateTo.getAndClearLastOutput();
                }

                @Override
                public void updateIfDesired(DataTree.Mut updatableInput, Object inputKey, DataEntry inputEntry, @Nullable Object keyUpdate, @Nullable Object valueUpdate) {
                    boolean updateKey = keyUpdate != null && !inputKey.equals(keyUpdate);
                    boolean updateValue = valueUpdate != null && !inputEntry.getValue().equals(valueUpdate);
                    if (updateKey || updateValue) {
                        DataEntry entry;
                        if (updateValue) {
                            entry = inputEntry.withValue(valueUpdate);
                        } else {
                            entry = inputEntry;
                        }
                        if (updateKey) {
                            updatableInput.remove(inputKey);
                            updatableInput.put(keyUpdate, entry);
                        } else {
                            updatableInput.put(inputKey, entry);
                        }
                        updated = true;
                    }
                }

                @Override
                public void updateSizeShrunk(DeserializeInput deser, Map<K, V> built) {
                    deser.notifyUpdate(KeyPath.empty(), UpdateReason.OTHER);
                    serialize(built, updateTo); // Reserialize the whole map
                }

                @Override
                public void updateMaybeOtherwise(DeserializeInput deser, DataTree.Mut updatableInput) {
                    // If the size didn't shrink, then perform our update if applicable
                    if (updated) {
                        deser.notifyUpdate(KeyPath.empty(), UpdateReason.UPDATED);
                        updateTo.outDataTree(updatableInput);
                    }
                }
            });
        }

        @Override
        public void serialize(@NonNull Map<K, V> value, @NonNull SerializeOutput ser) {
            DataTree.Mut output = new DataTree.Mut();
            for (Map.Entry<K, V> entry : value.entrySet()) {
                keySerializer.serialize(entry.getKey(), ser);
                Object keyOutput = ser.getAndClearLastOutput();
                if (keyOutput == null) {
                    throw new DeveloperMistakeException(
                            "Key serializer " + keySerializer + " did not produce output"
                    );
                }
                valueSerializer.serialize(entry.getValue(), ser);
                Object valueOutput = ser.getAndClearLastOutput();
                if (valueOutput == null) {
                    throw new DeveloperMistakeException("Value serializer " + valueSerializer + " did not produce output");
                }
                DataEntry previousValue = output.put(keyOutput, new DataEntry(valueOutput));
                if (previousValue != null) {
                    throw new DeveloperMistakeException(
                            "The key serializer " + keySerializer + " produced a duplicate output key " + keyOutput
                    );
                }
            }
            ser.outDataTree(output);
        }
    }
}
