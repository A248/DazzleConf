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
import space.arim.dazzleconf2.engine.DefaultValues;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.reflect.TypeToken;

import static space.arim.dazzleconf2.backend.Printable.join;
import static space.arim.dazzleconf2.backend.Printable.preBuilt;

/**
 * A liaison for enum types.
 * <p>
 * This liaison covers all types for which {@link Class#isEnum()} return true. It uses the enum names to match,
 * in a case-insensitive fashion, user strings.
 *
 */
public final class EnumLiaison implements TypeLiaison {

    /**
     * Creates
     */
    public EnumLiaison() {}

    @Override
    @SideEffectFree
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        Class<V> rawType = typeToken.getRawType();
        if (rawType.isEnum()) {
            @SuppressWarnings("unchecked")
            Agent<V> castAgent = (Agent<V>) new AgentImpl<>(rawType.asSubclass(Enum.class));
            return castAgent;
        }
        return null;
    }

    private static final class AgentImpl<E extends Enum<E>> implements Agent<E> {

        // TODO: Cache usage of this data using a Map<String, E>
        private final E[] enumConstants;

        AgentImpl(Class<E> enumClass) {
            this.enumConstants = enumClass.getEnumConstants();
        }

        @Override
        @SideEffectFree
        public @Nullable DefaultValues<E> loadDefaultValues(@NonNull DefaultInit defaultInit) {
            return null;
        }

        @Override
        @SideEffectFree
        public @NonNull SerializeDeserialize<E> makeSerializer() {
            return new SerializeDeserialize<E>() {
                @Override
                public @NonNull LoadResult<@NonNull E> deserialize(@NonNull DeserializeInput deser) {
                    Object object = deser.object();
                    if (object instanceof String) {
                        String string = (String) object;

                        for (E enumVal : enumConstants) {
                            if (enumVal.name().equalsIgnoreCase(string)) {
                                return LoadResult.of(enumVal);
                            }
                        }
                    }
                    // Failed to parse as the enum type. Let's build an error message that makes sense
                    LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                    // If we have too many elements, just accept the first 4 elements alphabetically
                    int numberAccepted = enumConstants.length;
                    int exampleCount = Math.min(numberAccepted, 4);
                    // If capped, append "+X more..." at the end
                    boolean capped = exampleCount < numberAccepted;
                    String[] acceptedExamples = new String[capped ? exampleCount + 1 : exampleCount];
                    for (int n = 0; n < exampleCount; n++) {
                        acceptedExamples[n] = enumConstants[n].name();
                    }
                    if (capped)
                        acceptedExamples[exampleCount] = '+' +  libraryLang.more(numberAccepted - exampleCount);
                    return deser.throwError(join(
                            preBuilt(libraryLang.badValue()),
                            preBuilt(" "),
                            libraryLang.notAccepted(object.toString(), acceptedExamples)
                    ));
                }

                @Override
                public void serialize(@NonNull E value, @NonNull SerializeOutput ser) {
                    ser.outString(value.name());
                }
            };
        }
    }
}
