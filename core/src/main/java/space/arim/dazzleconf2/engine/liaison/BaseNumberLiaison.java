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
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.DefaultValues;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.lang.annotation.Annotation;

abstract class BaseNumberLiaison<TYPE extends Number, DEF_ANNOTE extends Annotation, RANGE_ANNOTE extends Annotation>
        implements TypeLiaison {

    @Override
    @SideEffectFree
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        Class<?> rawType = typeToken.getRawType();
        if (rawType.equals(primitiveType()) || rawType.equals(boxedType())) {
            RANGE_ANNOTE rangeAnnote = typeToken.getReifiedType().getAnnotation(rangeAnnotation());
            @SuppressWarnings("unchecked")
            Agent<V> casted = (Agent<V>) new AgentImpl(rangeAnnote);
            return casted;
        }
        return null;
    }

    abstract @NonNull Class<TYPE> boxedType();

    abstract @NonNull Class<TYPE> primitiveType();

    abstract @NonNull Class<DEF_ANNOTE> defaultAnnotation();

    abstract @NonNull TYPE defaultValue(@NonNull DEF_ANNOTE defaultAnnotation);

    abstract @NonNull TYPE ifMissing(@NonNull DEF_ANNOTE defaultAnnotation);

    abstract @Nullable TYPE castNumbers(@NonNull Object input);

    abstract @Nullable TYPE parseFrom(@NonNull String input);

    abstract @NonNull Class<RANGE_ANNOTE> rangeAnnotation();

    abstract @NonNull TYPE minFrom(@NonNull RANGE_ANNOTE rangeAnnote);

    abstract @NonNull TYPE maxFrom(@NonNull RANGE_ANNOTE rangeAnnote);

    boolean isNaN(@NonNull TYPE value) {
        return false;
    }

    abstract boolean greaterOrEq(@NonNull TYPE value, @NonNull TYPE min);

    abstract boolean lessOrEq(@NonNull TYPE value, @NonNull TYPE max);

    private final class AgentImpl implements Agent<TYPE> {

        private final RANGE_ANNOTE rangeAnnote;

        private AgentImpl(RANGE_ANNOTE rangeAnnote) {
            this.rangeAnnote = rangeAnnote;
        }

        @Override
        @SideEffectFree
        public @Nullable DefaultValues<TYPE> loadDefaultValues(@NonNull DefaultInit defaultInit) {
            DEF_ANNOTE defaultAnnotation = defaultInit.methodAnnotations().getAnnotation(defaultAnnotation());
            if (defaultAnnotation != null) {
                TYPE defaultValue = defaultValue(defaultAnnotation);
                TYPE ifMissing = ifMissing(defaultAnnotation);
                return new DefaultValues<TYPE>() {
                    @Override
                    public @NonNull TYPE defaultValue() {
                        return defaultValue;
                    }

                    @Override
                    public @NonNull TYPE ifMissing() {
                        return ifMissing;
                    }
                };
            }
            return null;
        }

        @Override
        @SideEffectFree
        public @NonNull SerializeDeserialize<TYPE> makeSerializer() {
            return new SerializeDeserialize<TYPE>() {
                @Override
                public @NonNull LoadResult<@NonNull TYPE> deserialize(@NonNull DeserializeInput deser) {
                    // Three steps: deserialize, validation, success

                    // 1. Deserialization
                    TYPE value;
                    boolean wasFromString;
                    deserRoot:
                    {
                        Object object = deser.object();
                        TYPE castNumbers = castNumbers(object);
                        if (castNumbers != null) {
                            value = castNumbers;
                            wasFromString = false;
                            break deserRoot;
                        }
                        if (object instanceof String) {
                            String string = (String) object;
                            TYPE parseFrom = parseFrom(string);
                            if (parseFrom != null) {
                                value = parseFrom;
                                wasFromString = true;
                                break deserRoot;
                            }
                        }
                        LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                        return deser.throwError(libraryLang.wrongTypeForValue(object, boxedType()));
                    }
                    // 2. Validation
                    if (isNaN(value)) {
                        LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                        return deser.throwError(libraryLang.notANumber(value));
                    }
                    if (rangeAnnote != null) {
                        TYPE min = minFrom(rangeAnnote);
                        TYPE max = maxFrom(rangeAnnote);
                        if (!greaterOrEq(value, min) || !lessOrEq(value, max)) {
                            LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                            return deser.throwError(libraryLang.outOfRange(value, min, max));
                        }
                    }
                    // 3. Success
                    if (wasFromString) {
                        // We only notify updates if parsed from string. Backends often collapse numeric types into just
                        // integer/double, so calling #flagUpdate after casting would cause perpetual notifications
                        deser.flagUpdate(KeyPath.empty(), UpdateReason.UPDATED);
                    }
                    return LoadResult.of(value);
                }

                @Override
                public @NonNull LoadResult<@NonNull TYPE> deserializeUpdate(@NonNull DeserializeInput deser,
                                                                            @NonNull SerializeOutput updateTo) {
                    LoadResult<TYPE> result = deserialize(deser);
                    TYPE loaded;
                    // Use identity equality to determine if the value changed
                    if (result.isSuccess() && (loaded = result.getOrThrow()) != deser.object()) {
                        serialize(loaded, updateTo);
                    }
                    return result;
                }

                @Override
                public void serialize(@NonNull TYPE value, @NonNull SerializeOutput ser) {
                    // Safety: this class is only used with canonical types
                    ser.outObjectUnchecked(value);
                }
            };
        }
    }

}
