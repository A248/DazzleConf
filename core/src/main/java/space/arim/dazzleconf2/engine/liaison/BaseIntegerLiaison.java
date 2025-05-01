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

import java.lang.annotation.Annotation;
import java.util.Objects;

abstract class BaseIntegerLiaison<N, A extends Annotation> implements TypeLiaison {

    @Override
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        Class<?> rawType = typeToken.getRawType();
        if (rawType.equals(primitiveType()) || rawType.equals(boxedType())) {
            @SuppressWarnings("unchecked")
            Agent<V> casted = (Agent<V>) new AgentImpl();
            return casted;
        }
        return null;
    }

    abstract @NonNull Class<N> boxedType();

    abstract @NonNull Class<?> primitiveType();

    abstract @NonNull Class<A> defaultAnnotation();

    abstract @NonNull N valueOn(@NonNull A defaultAnnotation);

    abstract @Nullable N castNumbers(@NonNull Object input);

    abstract @Nullable N parseFrom(@NonNull String input);

    private final class AgentImpl implements Agent<N> {

        @Override
        public @Nullable DefaultValues<N> loadDefaultValues(@NonNull AnnotationContext annotationContext) {
            A defaultAnnotation = annotationContext.getAnnotation(defaultAnnotation());
            if (defaultAnnotation != null) {
                N value = Objects.requireNonNull(valueOn(defaultAnnotation));
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

                @Override
                public @NonNull LoadResult<@NonNull N> deserialize(@NonNull DeserializeInput deser) {
                    Object object = deser.object();
                    N fromOtherNumber = castNumbers(object);
                    if (fromOtherNumber != null) {
                        return LoadResult.of(fromOtherNumber);
                    }
                    if (object instanceof String) {
                        String string = (String) object;
                        N parsed = parseFrom(string);
                        if (parsed != null) {
                            return LoadResult.of(parsed);
                        }
                    }
                    LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                    return deser.throwError(libraryLang.wrongTypeForValue(object, boxedType()));
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
