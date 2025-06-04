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

package space.arim.dazzleconf2.engine.liaison;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.internals.ImmutableCollections;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.*;
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
        public @Nullable DefaultValues<COLL> loadDefaultValues(@NonNull DefaultInit defaultInit) {
            return null;
        }

        @Override
        public @NonNull SerializeDeserialize<COLL> makeSerializer() {
            return this;
        }

        abstract @NonNull BUILD_COLL makeMutableOutput(int sizeHint);

        abstract void addToMutableOutput(@NonNull BUILD_COLL output, int index, @NonNull E value);

        abstract @NonNull COLL buildThenCast(@NonNull BUILD_COLL output);

        private <UPD> @NonNull LoadResult<@NonNull COLL> implDeserialize(@NonNull DeserializeInput deser,
                                                                         @NonNull ImplDeserialize<COLL, E, UPD> impl) {
            Object object = deser.object();
            if (!(object instanceof List)) {
                LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                return deser.throwError(libraryLang.wrongTypeForValue(object, List.class));
            }
            Object[] input = ((List<?>) object).toArray();
            // When dealing with updates, this value keeps track of them
            // It is a lazily initialized array - a copy of input values, with updated elements replaced
            UPD writeBackUpdates = null;
            // Output collection, an Object[] for List/Collection or LinkedHashSet for Set
            BUILD_COLL output = makeMutableOutput(input.length);

            for (int n = 0; n < input.length; n++) {
                // Deserialize element
                Object elem = input[n];
                LoadResult<E> elemResult = impl.deserialize(elementSerializer, deser.makeChild(elem));
                if (elemResult.isFailure()) {
                    return LoadResult.failure(elemResult.getErrorContexts());
                }
                // Record update wish if necessary
                writeBackUpdates = impl.updateIfDesired(input, n, writeBackUpdates);

                addToMutableOutput(output, n, elemResult.getOrThrow());
            }
            // Construct result
            COLL built = buildThenCast(output);
            // Finish recording updates - check if size changed for Set, if handle notifications or updates
            if (built.size() != input.length) {
                impl.updateSizeShrunk(elementSerializer, deser, built);
            } else {
                impl.updateMaybeOtherwise(writeBackUpdates);
            }
            return LoadResult.of(built);
        }

        interface ImplDeserialize<COLL extends Collection<E>, E, U> {

            LoadResult<E> deserialize(SerializeDeserialize<E> elementSerializer, DeserializeInput deser);

            @Nullable U updateIfDesired(Object[] input, int index, @Nullable U writeBackUpdates);

            void updateSizeShrunk(SerializeDeserialize<E> elementSerializer, DeserializeInput deser, COLL built);

            void updateMaybeOtherwise(@Nullable U writeBackUpdates);

        }

        @Override
        public @NonNull LoadResult<@NonNull COLL> deserialize(@NonNull DeserializeInput deser) {
            return implDeserialize(deser, new ImplDeserialize<COLL, E, Void>() {
                @Override
                public LoadResult<E> deserialize(SerializeDeserialize<E> elementSerializer, DeserializeInput deser) {
                    return elementSerializer.deserialize(deser);
                }

                @Override
                public @Nullable Void updateIfDesired(Object[] input, int index, Void writeBackUpdates) {
                    return null;
                }

                @Override
                public void updateSizeShrunk(SerializeDeserialize<E> elementSerializer, DeserializeInput deser,
                                             COLL built) {
                    deser.flagUpdate(KeyPath.empty(), UpdateReason.OTHER);
                }

                @Override
                public void updateMaybeOtherwise(@Nullable Void writeBackUpdates) {}
            });
        }

        @Override
        public @NonNull LoadResult<@NonNull COLL> deserializeUpdate(@NonNull DeserializeInput deser,
                                                                    @NonNull SerializeOutput updateTo) {
            return implDeserialize(deser, new ImplDeserialize<COLL, E, Object[]>() {
                @Override
                public LoadResult<E> deserialize(SerializeDeserialize<E> elementSerializer, DeserializeInput deser) {
                    return elementSerializer.deserializeUpdate(deser, updateTo);
                }

                @Override
                public Object @Nullable [] updateIfDesired(Object[] input, int index, Object[] writeBackUpdates) {
                    Object elemUpdate = updateTo.getAndClearLastOutput();
                    if (elemUpdate != null && !input[index].equals(elemUpdate)) {
                        if (writeBackUpdates == null) {
                            // Init update array: fill in with input by default.
                            // Values after the current position might very well be overwritten
                            writeBackUpdates = input.clone();
                        }
                        writeBackUpdates[index] = elemUpdate;
                    }
                    return writeBackUpdates;
                }

                @Override
                public void updateSizeShrunk(SerializeDeserialize<E> elementSerializer, DeserializeInput deser, COLL built) {
                    deser.flagUpdate(KeyPath.empty(), UpdateReason.OTHER);
                    serialize(built, updateTo); // Reserialize the whole collection
                }

                @Override
                public void updateMaybeOtherwise(Object @Nullable [] writeBackUpdates) {
                    // If the size didn't shrink, then use our update array if it exists
                    if (writeBackUpdates != null) {
                        deser.flagUpdate(KeyPath.empty(), UpdateReason.UPDATED);
                        updateTo.outObjectUnchecked(Arrays.asList(writeBackUpdates));
                    }
                }
            });
        }

        @Override
        public void serialize(@NonNull COLL value, @NonNull SerializeOutput ser) {
            // Transform E[] into Object[], serializing one by one
            Object[] values = value.toArray();
            for (int n = 0; n < values.length; n++) {
                @SuppressWarnings("unchecked")
                E elem = (E) values[n];
                // Use the provided `ser` for per-element output
                elementSerializer.serialize(elem, ser);
                Object elemOutput = ser.getAndClearLastOutput();
                if (elemOutput == null) {
                    throw new DeveloperMistakeException(
                            "Element serializer " + elementSerializer + " did not produce output"
                    );
                }
                values[n] = elemOutput;
            }
            ser.outObjectUnchecked(Arrays.asList(values));
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
                return ImmutableCollections.emptySet();
            }
            return Collections.unmodifiableSet(output);
        }
    }
}
