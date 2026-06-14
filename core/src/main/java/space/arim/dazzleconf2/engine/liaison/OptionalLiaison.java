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
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.engine.DeserializeContext;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.reflect.ReifiedType;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Provides support for {@link Optional} and its family of related classes.
 * <p>
 * All of {@code java.util.Optional}, {@link OptionalInt}, {@link OptionalLong}, and {@link OptionalDouble} are
 * supported by this liaison.
 * <p>
 * <b>Limitations</b>
 * <p>
 * Default-value style annotations are not supported by this liaison. Developers who need optional types should use
 * default method implementations to return default values. If this behavior is limiting, then the developer can provide
 * another liaison with higher priority to handle {@code Optional<SpecificType>} as a special case.
 * <p>
 * Moreover, the serializers for {@code OptionalInt}/{@code OptionalLong}/{@code OptionalDouble} are not affected by
 * type annotations. The standard serializers for integers, longs, and doubles will be used for present values. This
 * behavior can be circumvented by using a regular optional. For example, to use an annotation on an integer type,
 * developers should declare {@code Optional<@SomeAnnotation Integer>} as the method return type.
 * <p>
 * <b>Implementation Semantics</b>
 * <p>
 * An optional value need not exist within its respective container. For example, if the relevant entry is missing from
 * a map, {@code Optional.empty()} might be used to represent that absence. On serialization, only optionals with
 * present values will generate output data.
 * <p>
 * The implementation of this class may be helpful to those wishing to understand the mechanics of the library.
 */
public final class OptionalLiaison implements TypeLiaison {

    /**
     * Creates the liaison
     */
    public OptionalLiaison() {}

    @Override
    @SideEffectFree
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        Class<?> rawType = typeToken.getRawType();
        if (rawType.equals(Optional.class)) {
            TypeToken<?> valueToken = new TypeToken<>(typeToken.getReifiedType().argumentAt(0));
            OptSerializer<?> optSerializer = new OptSerializer<>(handshake.getOtherSerializer(valueToken));
            @SuppressWarnings("unchecked")
            Agent<V> cast = (Agent<V>) optSerializer;
            return cast;
        }
        if (rawType.equals(OptionalInt.class)) {
            OptIntSerializer optIntSerializer = new OptIntSerializer(handshake.getOtherSerializer(
                    new TypeToken<>(ReifiedType.rawUnannotated(Integer.class))
            ));
            @SuppressWarnings("unchecked")
            Agent<V> cast = (Agent<V>) optIntSerializer;
            return cast;
        }
        if (rawType.equals(OptionalLong.class)) {
            OptLongSerializer optLongSerializer = new OptLongSerializer(handshake.getOtherSerializer(
                    new TypeToken<>(ReifiedType.rawUnannotated(Long.class))
            ));
            @SuppressWarnings("unchecked")
            Agent<V> cast = (Agent<V>) optLongSerializer;
            return cast;
        }
        if (rawType.equals(OptionalDouble.class)) {
            OptDoubleSerializer optDoubleSerializer = new OptDoubleSerializer(handshake.getOtherSerializer(
                    new TypeToken<>(ReifiedType.rawUnannotated(Double.class))
            ));
            @SuppressWarnings("unchecked")
            Agent<V> cast = (Agent<V>) optDoubleSerializer;
            return cast;
        }
        return null;
    }

    private static final class OptSerializer<V> implements Agent<Optional<V>>, SerializeDeserialize<Optional<V>> {

        private final SerializeDeserialize<V> serializer;

        private OptSerializer(SerializeDeserialize<V> serializer) {
            this.serializer = serializer;
        }

        @Override
        public @NonNull SerializeDeserialize<Optional<V>> makeSerializer() {
            return this;
        }

        @Override
        public @Nullable Optional<V> deserializeAbsent(@NonNull DeserializeContext deser) {
            return Optional.empty();
        }

        @Override
        public @NonNull LoadResult<Optional<V>> deserialize(@NonNull DeserializeInput deser) {
            return serializer.deserialize(deser).map(Optional::of);
        }

        @Override
        public @NonNull LoadResult<Optional<V>> deserializeUpdate(@NonNull DeserializeInput deser, @NonNull SerializeOutput updateTo) {
            return serializer.deserializeUpdate(deser, updateTo).map(Optional::of);
        }

        @Override
        public void serialize(@NonNull Optional<V> value, @NonNull SerializeOutput ser) {
            if (value.isPresent()) {
                serializer.serialize(value.get(), ser);
            } else {
                ser.outNone();
            }
        }
    }

    private static final class OptIntSerializer implements Agent<OptionalInt>, SerializeDeserialize<OptionalInt> {

        private final SerializeDeserialize<Integer> intSerializer;

        private OptIntSerializer(SerializeDeserialize<Integer> intSerializer) {
            this.intSerializer = intSerializer;
        }

        @Override
        public @NonNull SerializeDeserialize<OptionalInt> makeSerializer() {
            return this;
        }

        @Override
        public @Nullable OptionalInt deserializeAbsent(@NonNull DeserializeContext deser) {
            return OptionalInt.empty();
        }

        @Override
        public @NonNull LoadResult<OptionalInt> deserialize(@NonNull DeserializeInput deser) {
            return intSerializer.deserialize(deser).map(OptionalInt::of);
        }

        @Override
        public @NonNull LoadResult<OptionalInt> deserializeUpdate(@NonNull DeserializeInput deser, @NonNull SerializeOutput updateTo) {
            return intSerializer.deserializeUpdate(deser, updateTo).map(OptionalInt::of);
        }

        @Override
        public void serialize(@NonNull OptionalInt value, @NonNull SerializeOutput ser) {
            if (value.isPresent()) {
                intSerializer.serialize(value.getAsInt(), ser);
            } else {
                ser.outNone();
            }
        }
    }

    private static final class OptLongSerializer implements Agent<OptionalLong>, SerializeDeserialize<OptionalLong> {

        private final SerializeDeserialize<Long> longSerializer;

        private OptLongSerializer(SerializeDeserialize<Long> longSerializer) {
            this.longSerializer = longSerializer;
        }

        @Override
        public @NonNull SerializeDeserialize<OptionalLong> makeSerializer() {
            return this;
        }

        @Override
        public @Nullable OptionalLong deserializeAbsent(@NonNull DeserializeContext deser) {
            return OptionalLong.empty();
        }

        @Override
        public @NonNull LoadResult<OptionalLong> deserialize(@NonNull DeserializeInput deser) {
            return longSerializer.deserialize(deser).map(OptionalLong::of);
        }

        @Override
        public @NonNull LoadResult<OptionalLong> deserializeUpdate(@NonNull DeserializeInput deser, @NonNull SerializeOutput updateTo) {
            return longSerializer.deserializeUpdate(deser, updateTo).map(OptionalLong::of);
        }

        @Override
        public void serialize(@NonNull OptionalLong value, @NonNull SerializeOutput ser) {
            if (value.isPresent()) {
                longSerializer.serialize(value.getAsLong(), ser);
            } else {
                ser.outNone();
            }
        }
    }

    private static final class OptDoubleSerializer implements Agent<OptionalDouble>, SerializeDeserialize<OptionalDouble> {

        private final SerializeDeserialize<Double> doubleSerializer;

        private OptDoubleSerializer(SerializeDeserialize<Double> doubleSerializer) {
            this.doubleSerializer = doubleSerializer;
        }

        @Override
        public @NonNull SerializeDeserialize<OptionalDouble> makeSerializer() {
            return this;
        }

        @Override
        public @Nullable OptionalDouble deserializeAbsent(@NonNull DeserializeContext deser) {
            return OptionalDouble.empty();
        }

        @Override
        public @NonNull LoadResult<OptionalDouble> deserialize(@NonNull DeserializeInput deser) {
            return doubleSerializer.deserialize(deser).map(OptionalDouble::of);
        }

        @Override
        public @NonNull LoadResult<OptionalDouble> deserializeUpdate(@NonNull DeserializeInput deser, 
                                                                     @NonNull SerializeOutput updateTo) {
            return doubleSerializer.deserializeUpdate(deser, updateTo).map(OptionalDouble::of);
        }

        @Override
        public void serialize(@NonNull OptionalDouble value, @NonNull SerializeOutput ser) {
            if (value.isPresent()) {
                doubleSerializer.serialize(value.getAsDouble(), ser);
            } else {
                ser.outNone();
            }
        }
    }
}
