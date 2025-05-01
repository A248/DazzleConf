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
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.internals.ImmutableCollections;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;

/**
 * Liaison for lists
 */
public final class ListLiaison implements TypeLiaison {

    /**
     * Creates
     */
    public ListLiaison() {}

    @Override
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        if (typeToken.getRawType().equals(List.class)) {
            TypeToken<?> elementToken = new TypeToken<>(typeToken.getReifiedType().argumentAt(0));
            @SuppressWarnings("unchecked")
            Agent<V> casted = (Agent<V>) new AgentImpl<>(handshake.getOtherSerializer(elementToken));
            return casted;
        }
        return null;
    }

    private static final class AgentImpl<E> implements Agent<List<E>>, SerializeDeserialize<List<E>> {

        private final SerializeDeserialize<E> elementSerializer;

        private AgentImpl(SerializeDeserialize<E> elementSerializer) {
            this.elementSerializer = elementSerializer;
        }

        @Override
        public @Nullable DefaultValues<List<E>> loadDefaultValues(@NonNull AnnotationContext annotationContext) {
            return null;
        }

        @Override
        public @NonNull SerializeDeserialize<List<E>> makeSerializer() {
            return this;
        }

        private <U> @NonNull LoadResult<@NonNull List<E>> implDeserialize(@NonNull DeserializeInput deser,
                                                                          @NonNull ImplDeserialize<E, U> impl) {
            Object object = deser.object();
            if (!(object instanceof List)) {
                LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                return deser.throwError(libraryLang.wrongTypeForValue(object, List.class));
            }
            List<?> input = (List<?>) object;
            int inputSize = input.size();
            // When dealing with updates, this value keeps track of them
            // It is a lazily initialized array - a copy of input values, with updated elements replaced
            U writeBackUpdates = null;                  // Object[] writeBackUpdates
            Object[] output = new Object[inputSize];    // E[] output

            for (int n = 0; n < inputSize; n++) {
                // Deserialize element
                Object elem = input.get(n);
                LoadResult<E> elemResult = impl.deserialize(elementSerializer, deser.makeChild(elem));
                if (elemResult.isFailure()) {
                    return LoadResult.failure(elemResult.getErrorContexts());
                }
                // Record update wish if necessary
                writeBackUpdates = impl.updateIfDesired(input, n, elem, writeBackUpdates);

                output[n] = elemResult.getOrThrow();
            }
            // Finish recording updates
            impl.updateArrayFinished(writeBackUpdates);

            // All done. Safe cast since we essentially have E[] of output
            @SuppressWarnings("unchecked")
            List<E> castOutput = (List<E>) ImmutableCollections.listOf(output);
            return LoadResult.of(castOutput);
        }

        interface ImplDeserialize<E, U> {

            LoadResult<E> deserialize(SerializeDeserialize<E> serializer, DeserializeInput deser);

            @Nullable U updateIfDesired(List<?> input, int index, Object elemAtIndex, @Nullable U writeBackUpdates);

            void updateArrayFinished(@Nullable U writeBackUpdates);

        }

        @Override
        public @NonNull LoadResult<@NonNull List<E>> deserialize(@NonNull DeserializeInput deser) {
            return implDeserialize(deser, new ImplDeserialize<E, Void>() {
                @Override
                public LoadResult<E> deserialize(SerializeDeserialize<E> serializer, DeserializeInput deser) {
                    return serializer.deserialize(deser);
                }

                @Override
                public @Nullable Void updateIfDesired(List<?> input, int index, Object elemAtIndex,
                                                      Void writeBackUpdates) {
                    return null;
                }

                @Override
                public void updateArrayFinished(@Nullable Void writeBackUpdates) {}
            });
        }

        @Override
        public @NonNull LoadResult<@NonNull List<E>> deserializeUpdate(@NonNull DeserializeInput deser,
                                                                       @NonNull SerializeOutput updateTo) {
            return implDeserialize(deser, new ImplDeserialize<E, Object[]>() {
                @Override
                public LoadResult<E> deserialize(SerializeDeserialize<E> serializer, DeserializeInput deser) {
                    return serializer.deserializeUpdate(deser, updateTo);
                }

                @Override
                public Object @Nullable [] updateIfDesired(List<?> input, int index, Object elemAtIndex,
                                                           Object[] writeBackUpdates) {
                    Object elemUpdate = updateTo.getAndClearLastOutput();
                    if (elemUpdate != null) {
                        if (writeBackUpdates == null) {
                            // Init update array: fill in values retroactively.
                            // Values after the current position might very well be overwritten
                            writeBackUpdates = input.toArray(new Object[0]);
                        }
                        writeBackUpdates[index] = elemUpdate;

                    } else if (writeBackUpdates != null) {
                        writeBackUpdates[index] = elemAtIndex;
                    }
                    return writeBackUpdates;
                }

                @Override
                public void updateArrayFinished(Object @Nullable [] writeBackUpdates) {
                    if (writeBackUpdates != null) {
                        updateTo.outObjectUnchecked(Arrays.asList(writeBackUpdates));
                    }
                }
            });
        }

        @Override
        public void serialize(@NonNull List<E> value, @NonNull SerializeOutput ser) {

            Object[] builtOutput = new Object[value.size()];
            for (int n = 0; n < builtOutput.length; n++) {

                // Use the provided `ser` for per-element output
                elementSerializer.serialize(value.get(n), ser);
                Object elemOutput = ser.getAndClearLastOutput();
                if (elemOutput == null) {
                    throw new DeveloperMistakeException(
                            "Element serializer " + elementSerializer + " gave null output"
                    );
                }
                builtOutput[n] = elemOutput;
            }
            ser.outObjectUnchecked(Arrays.asList(builtOutput));
        }
    }
}
