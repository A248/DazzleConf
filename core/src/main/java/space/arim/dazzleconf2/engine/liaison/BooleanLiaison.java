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

/**
 * Liaison for booleans
 */
public final class BooleanLiaison implements TypeLiaison {

    /**
     * Creates
     */
    public BooleanLiaison() {}

    @Override
    @SideEffectFree
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        Class<V> rawType = typeToken.getRawType();
        if (rawType.equals(boolean.class) || rawType.equals(Boolean.class)) {
            @SuppressWarnings("unchecked")
            Agent<V> castAgent = (Agent<V>) new AgentImpl();
            return castAgent;
        }
        return null;
    }

    private static final class AgentImpl implements Agent<Boolean> {

        @Override
        @SideEffectFree
        public @Nullable DefaultValues<Boolean> loadDefaultValues(@NonNull DefaultInit defaultInit) {
            BooleanDefault booleanDefault = defaultInit.methodAnnotations().getAnnotation(BooleanDefault.class);
            if (booleanDefault != null) {
                return new DefaultValues<Boolean>() {
                    @Override
                    public @NonNull Boolean defaultValue() {
                        return booleanDefault.value();
                    }

                    @Override
                    public @NonNull Boolean ifMissing() {
                        return booleanDefault.ifMissing();
                    }
                };
            }
            return null;
        }

        @Override
        @SideEffectFree
        public @NonNull SerializeDeserialize<Boolean> makeSerializer() {
            return new SerializeDeserialize<Boolean>() {

                private @NonNull LoadResult<@NonNull Boolean> implDeserialize(@NonNull DeserializeInput deser,
                                                                              @Nullable SerializeOutput updateTo) {
                    Object object = deser.object();
                    if (object instanceof Boolean) {
                        return LoadResult.of((Boolean) object);
                    }
                    tryFromString:
                    if (object instanceof String) {
                        String string = (String) object;
                        boolean fromString;
                        if ("true".equalsIgnoreCase(string)) {
                            fromString = true;
                        } else if ("false".equalsIgnoreCase(string)) {
                            fromString = false;
                        } else {
                            break tryFromString;
                        }
                        // Success: now flag and perform updates, then yield
                        deser.flagUpdate(KeyPath.empty(), UpdateReason.UPDATED);
                        if (updateTo != null) updateTo.outBoolean(fromString);
                        return LoadResult.of(fromString);
                    }
                    LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                    return deser.throwError(libraryLang.wrongTypeForValue(object, Boolean.class));
                }

                @Override
                public @NonNull LoadResult<@NonNull Boolean> deserialize(@NonNull DeserializeInput deser) {
                    return implDeserialize(deser, null);
                }

                @Override
                public @NonNull LoadResult<@NonNull Boolean> deserializeUpdate(@NonNull DeserializeInput deser,
                                                                               @NonNull SerializeOutput updateTo) {
                    return implDeserialize(deser, updateTo);
                }

                @Override
                public void serialize(@NonNull Boolean value, @NonNull SerializeOutput ser) {
                    ser.outBoolean(value);
                }
            };
        }
    }
}
