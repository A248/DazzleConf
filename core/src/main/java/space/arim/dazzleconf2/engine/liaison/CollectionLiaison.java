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
import space.arim.dazzleconf2.backend.DataList;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.internals.ImmutableCollections;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Liaison for lists, sets, and collections.
 * <p>
 * This liaison will match all of {@code Collection<E>}, {@code List<E>}, and {@code Set<E>}. It will use the relevant
 * serializer for {@code E} to build the relevant collection in deserialization, or to serialize the elements (in
 * the sequence given by the collection type) during serialization.
 * <p>
 * If a {@code Set} is requested, but the user input contains a duplicate element, it will be silently skipped.
 * <p>
 * <b>Limitations</b>
 * <p>
 * This liaison does not, and <b>cannot</b>, preserve entry metadata, such as comments, on list entries across separate
 * acts of deserialization and serialization. Only a {@link SerializeDeserialize#deserializeUpdate} operation can
 * preserve this entry metadata.
 * <p>
 * <b>Effective consistent order</b>
 * <p>
 * This liaison chose the following implementation details, in order to service the requested types:
 * <ul>
 *     <li>{@link Collection}: an immmutable list</li>
 *     <li>{@link List}: an immutable list</li>
 *     <li>{@link Set}: an immutable wrapper over a {@link LinkedHashSet}</li>
 * </ul>
 * <p>
 * Thus, all collections, including {@code Collection} and {@code Set}, are in fact internally implemented using a
 * consistent order. This means that from an end-user perspective, they will be indistinguishable from {@code List}:
 * e.g., they will be deserialized and serialized in the same order, the only difference being that duplicate
 * {@code Set} elements (if relevant) will be removed.
 * <p>
 * This class, {@code CollectionLiaison}, decided to adopt this behavior to reflect the fact that {@code Collection},
 * {@code Set}, and {@code List} are programmer-facing types: users likely have little knowledge of the difference.
 * These types exist mainly from a developer's perspective. We could think of their purpose as follows:
 *
 * <ul>
 *     <li>{@code Collection}: an ordered sequence for the user, an unordered bag for the developer</li>
 *     <li>{@code List}: an ordered sequence, both for the user and the developer</li>
 *     <li>{@code Set}: an ordered sequence for the user (with duplicates ignored), a set for the developer</li>
 * </ul>
 */
public final class CollectionLiaison implements TypeLiaison {

    /**
     * Creates the liaison
     */
    public CollectionLiaison() {}

    @Override
    @SideEffectFree
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        @SuppressWarnings("unchecked")
        Agent<V> casted = (Agent<V>) makeAgent(typeToken.getRawType(), () -> {
            TypeToken<?> elementToken = new TypeToken<>(typeToken.getReifiedType().argumentAt(0));
            return handshake.getOtherSerializer(elementToken);
        });
        return casted;
    }

    private <E> Agent<? extends Collection<E>> makeAgent(Class<?> rawType,
                                                         Supplier<SerializeDeserialize<E>> elementSerializer) {
        if (rawType.equals(List.class)) {
            return new ListAgent<>(elementSerializer.get());
        } else if (rawType.equals(Set.class)) {
            return new SetAgent<>(elementSerializer.get());
        } else if (rawType.equals(Collection.class)) {
            return new CollectionAgent<>(elementSerializer.get());
        }
        return null;
    }

    private static abstract class AgentBase<COLL extends Collection<E>, E, BUILD_COLL>
            implements Agent<COLL>, SerializeDeserialize<COLL> {

        private final SerializeDeserialize<E> elementSerializer;

        private AgentBase(SerializeDeserialize<E> elementSerializer) {
            this.elementSerializer = elementSerializer;
        }

        @Override
        @SideEffectFree
        public @NonNull SerializeDeserialize<COLL> makeSerializer() {
            return this;
        }

        abstract @NonNull BUILD_COLL makeMutableOutput(int sizeHint);

        abstract void addToMutableOutput(@NonNull BUILD_COLL output, int index, @NonNull E value);

        abstract @NonNull COLL buildThenCast(@NonNull BUILD_COLL output);

        private <D extends DataList> @NonNull LoadResult<@NonNull COLL> implDeserialize(
                @NonNull DeserializeInput deser, @NonNull ImplDeserialize<COLL, D, E> impl) {
            // In order to reduce stack depth, avoid functions like LoadResult#flatMap
            LoadResult<DataList> dataListResult = deser.requireDataList();
            if (dataListResult.isFailure()) {
                return LoadResult.failure(dataListResult.getErrorContexts());
            }
            // Either a DataList (for #serialize) or a DataList.Mut (for #serializeUpdate)
            // When dealing with updates, this variable performs the double responsibility of storing them
            D input = impl.prepare(dataListResult.getOrThrow());
            // Output collection, an Object[] for List/Collection or LinkedHashSet for Set
            BUILD_COLL output = makeMutableOutput(input.size());
            // Error handling - get a certain maximum before quitting, becomes non-null if we find at least 1 error
            ErrorContext[] collectedErrors = null;
            int errorCount = 0;

            for (int n = 0; n < input.size(); n++) {
                // Deserialize element
                DataEntry inputEntry = input.get(n);
                LoadResult<E> elemResult = impl.deserialize(elementSerializer, deser.makeChild(inputEntry, n));
                if (elemResult.isFailure()) {
                    if (collectedErrors == null) {
                        collectedErrors = new ErrorContext[deser.maximumErrorCollect()];
                    }
                    for (ErrorContext errorToAppend : elemResult.getErrorContexts()) {
                        // Append this error
                        collectedErrors[errorCount++] = errorToAppend;
                        // Check if maxed out
                        if (errorCount == collectedErrors.length) {
                            return LoadResult.failure(collectedErrors);
                        }
                    }
                } else if (collectedErrors == null) {
                    // Record update wish if necessary
                    impl.updateIfDesired(input, inputEntry, n);

                    addToMutableOutput(output, n, elemResult.getOrThrow());
                }
            }
            // Error handling
            if (collectedErrors != null) {
                return LoadResult.failure(Arrays.copyOf(collectedErrors, errorCount));
            }
            // Success - construct result
            COLL built = buildThenCast(output);
            // Finish recording updates - check if size changed for Set, to handle notifications or updates
            if (built.size() != input.size()) {
                // Note that size-related updates can't happen during iteration itself (concurrent modification)
                impl.updateSizeShrunk(deser, built);
            } else {
                impl.updateMaybeOtherwise(deser, input);
            }
            return LoadResult.of(built);
        }

        interface ImplDeserialize<COLL extends Collection<E>, D extends DataList, E> {

            D prepare(DataList dataList);

            LoadResult<E> deserialize(SerializeDeserialize<E> elementSerializer, DeserializeInput deser);

            void updateIfDesired(D updatableInput, DataEntry existingEntry, int idx);

            void updateSizeShrunk(DeserializeInput deser, COLL built);

            void updateMaybeOtherwise(DeserializeInput deser, D updatableInput);

        }

        @Override
        public @NonNull LoadResult<@NonNull COLL> deserialize(@NonNull DeserializeInput deser) {
            return implDeserialize(deser, new ImplDeserialize<COLL, DataList, E>() {
                @Override
                public DataList prepare(DataList dataList) {
                    return dataList;
                }

                @Override
                public LoadResult<E> deserialize(SerializeDeserialize<E> elementSerializer, DeserializeInput deser) {
                    return elementSerializer.deserialize(deser);
                }

                @Override
                public void updateIfDesired(DataList updatableInput, DataEntry existingEntry, int idx) {}

                @Override
                public void updateSizeShrunk(DeserializeInput deser,
                                             COLL built) {
                    deser.notifyUpdate(KeyPath.empty(), UpdateReason.OTHER);
                }

                @Override
                public void updateMaybeOtherwise(DeserializeInput deser, DataList updatableInput) {}
            });
        }

        @Override
        public @NonNull LoadResult<@NonNull COLL> deserializeUpdate(@NonNull DeserializeInput deser,
                                                                    @NonNull SerializeOutput updateTo) {
            return implDeserialize(deser, new ImplDeserialize<COLL, DataList.Mut, E>() {
                private boolean updated;

                @Override
                public DataList.Mut prepare(DataList dataList) {
                    return dataList.intoMut();
                }

                @Override
                public LoadResult<E> deserialize(SerializeDeserialize<E> elementSerializer, DeserializeInput deser) {
                    return elementSerializer.deserializeUpdate(deser, updateTo);
                }

                @Override
                public void updateIfDesired(DataList.Mut updatableInput, DataEntry existingEntry, int idx) {
                    Object elemUpdate = updateTo.getAndClearLastOutput();
                    if (elemUpdate != null && !existingEntry.getValue().equals(elemUpdate)) {
                        updatableInput.set(idx, existingEntry.withValue(elemUpdate));
                        updated = true;
                    }
                }

                @Override
                public void updateSizeShrunk(DeserializeInput deser, COLL built) {
                    deser.notifyUpdate(KeyPath.empty(), UpdateReason.OTHER);
                    serialize(built, updateTo); // Reserialize the whole collection
                }

                @Override
                public void updateMaybeOtherwise(DeserializeInput deser, DataList.Mut updatableInput) {
                    // If the size didn't shrink, then perform our update if applicable
                    if (updated) {
                        deser.notifyUpdate(KeyPath.empty(), UpdateReason.UPDATED);
                        updateTo.outDataList(updatableInput);
                    }
                }
            });
        }

        @Override
        public void serialize(@NonNull COLL value, @NonNull SerializeOutput ser) {
            DataList.Mut output = new DataList.Mut(value.size());
            for (E elem : value) {
                // Use the provided `ser` for per-element output
                elementSerializer.serialize(elem, ser);
                Object elemOutput = ser.getAndClearLastOutput();
                if (elemOutput == null) {
                    throw new DeveloperMistakeException(
                            "Element serializer " + elementSerializer + " did not produce output"
                    );
                }
                output.add(new DataEntry(elemOutput));
            }
            ser.outDataList(output);
        }
    }

    private static final class CollectionAgent<E> extends AgentBase<Collection<E>, E, Object[]> {

        private CollectionAgent(SerializeDeserialize<E> elementSerializer) {
            super(elementSerializer);
        }

        @Override
        Object @NonNull [] makeMutableOutput(int sizeHint) {
            return new Object[sizeHint];
        }

        @Override
        void addToMutableOutput(Object @NonNull [] output, int index, @NonNull E value) {
            output[index] = value;
        }

        @Override
        @SuppressWarnings("unchecked")
        @NonNull Collection<E> buildThenCast(Object @NonNull [] output) {
            return (List<E>) ImmutableCollections.listOf(output);
        }
    }

    private static final class ListAgent<E> extends AgentBase<List<E>, E, Object[]> {

        private ListAgent(SerializeDeserialize<E> elementSerializer) {
            super(elementSerializer);
        }

        @Override
        Object @NonNull [] makeMutableOutput(int sizeHint) {
            return new Object[sizeHint];
        }

        @Override
        void addToMutableOutput(Object @NonNull [] output, int index, @NonNull E value) {
            output[index] = value;
        }

        @Override
        @SuppressWarnings("unchecked")
        @NonNull List<E> buildThenCast(Object @NonNull [] output) {
            return (List<E>) ImmutableCollections.listOf(output);
        }
    }

    private static final class SetAgent<E> extends AgentBase<Set<E>, E, LinkedHashSet<E>> {

        private SetAgent(SerializeDeserialize<E> elementSerializer) {
            super(elementSerializer);
        }

        @Override
        @NonNull LinkedHashSet<E> makeMutableOutput(int sizeHint) {
            return new LinkedHashSet<>(sizeHint + 1, 0.999999f);
        }

        @Override
        void addToMutableOutput(@NonNull LinkedHashSet<E> output, int index, @NonNull E value) {
            output.add(value);
        }

        @Override
        @NonNull Set<E> buildThenCast(@NonNull LinkedHashSet<E> output) {
            if (output.isEmpty()) {
                // Optimization
                return ImmutableCollections.emptySet();
            }
            return Collections.unmodifiableSet(output);
        }
    }
}
