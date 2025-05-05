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
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.reflect.TypeToken;

abstract class BaseIntegerLiaison<N> implements TypeLiaison {

    @Override
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        Class<?> rawType = typeToken.getRawType();
        if (rawType.equals(primitiveType()) || rawType.equals(boxedType())) {
            Object rangeAnnote = typeToken.getReifiedType().getAnnotation(LongRange.class);
            if (rangeAnnote == null) {
                rangeAnnote = typeToken.getReifiedType().getAnnotation(IntegerRange.class);
            }
            @SuppressWarnings("unchecked")
            Agent<V> casted = (Agent<V>) new AgentImpl(rangeAnnote);
            return casted;
        }
        return null;
    }

    abstract @NonNull Class<N> boxedType();

    abstract @NonNull Class<?> primitiveType();

    abstract @Nullable N defaultValue(@NonNull AnnotationContext annotationContext);

    abstract @Nullable N castNumbers(@NonNull Object input);

    abstract @Nullable N parseFrom(@NonNull String input);

    abstract boolean lessOrEq(@NonNull N value, long max);

    abstract boolean greaterOrEq(@NonNull N value, long min);

    abstract boolean lessOrEq(@NonNull N value, int max);

    abstract boolean greaterOrEq(@NonNull N value, int min);

    private final class AgentImpl implements Agent<N> {

        private final Object rangeAnnote;

        private AgentImpl(Object rangeAnnote) {
            this.rangeAnnote = rangeAnnote;
        }

        @Override
        public @Nullable DefaultValues<N> loadDefaultValues(@NonNull DefaultInit defaultInit) {
            N value = defaultValue(defaultInit.methodAnnotations());
            if (value != null) {
                return new DefaultValues<N>() {
                    @Override
                    public @NonNull N defaultValue() {
                        return value;
                    }

                    @Override
                    public @NonNull N ifMissing() {
                        return value;
                    }
                };
            }
            return null;
        }

        @Override
        public @NonNull SerializeDeserialize<N> makeSerializer() {
            return new SerializeDeserialize<N>() {

                public @Nullable N deserUnbounded(@NonNull DeserializeInput deser) {
                    Object object = deser.object();
                    N fromOtherNumber = castNumbers(object);
                    if (fromOtherNumber != null) {
                        return fromOtherNumber;
                    }
                    if (object instanceof String) {
                        String string = (String) object;
                        return parseFrom(string);
                    }
                    return null;
                }

                @Override
                public @NonNull LoadResult<@NonNull N> deserialize(@NonNull DeserializeInput deser) {
                    N value = deserUnbounded(deser);
                    if (value != null) {
                        if (rangeAnnote instanceof LongRange) {
                            LongRange longRange = (LongRange) rangeAnnote;
                            if (!greaterOrEq(value, longRange.min()) || !lessOrEq(value, longRange.max())) {
                                LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                                return deser.throwError(libraryLang.outOfRange(value, longRange.min(), longRange.max()));
                            }
                        }
                        if (rangeAnnote instanceof IntegerRange) {
                            IntegerRange integerRange = (IntegerRange) rangeAnnote;
                            if (!greaterOrEq(value, integerRange.min()) || !lessOrEq(value, integerRange.max())) {
                                LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                                return deser.throwError(libraryLang.outOfRange(value, integerRange.min(), integerRange.max()));
                            }
                        }
                        return LoadResult.of(value);
                    }
                    LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                    return deser.throwError(libraryLang.wrongTypeForValue(deser.object(), boxedType()));
                }

                @Override
                public @NonNull LoadResult<@NonNull N> deserializeUpdate(@NonNull DeserializeInput deser,
                                                                         @NonNull SerializeOutput updateTo) {
                    LoadResult<N> result = deserialize(deser);
                    N loaded;
                    if (result.isSuccess() && (loaded = result.getOrThrow()) != deser.object()) {
                        serialize(loaded, updateTo);
                    }
                    return result;
                }

                @Override
                public void serialize(@NonNull N value, @NonNull SerializeOutput ser) {
                    // Safety: this class is only used with canonical types
                    ser.outObjectUnchecked(value);
                }
            };
        }
    }

}
