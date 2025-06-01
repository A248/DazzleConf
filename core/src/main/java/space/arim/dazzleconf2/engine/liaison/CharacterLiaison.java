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

/**
 * Liaison for {@code char} and {@code Character}.
 * <p>
 * This liaison does not support any default value-providing annotations.
 */
public final class CharacterLiaison implements TypeLiaison {

    /**
     * Creates
     */
    public CharacterLiaison() {}

    @Override
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        Class<V> rawType = typeToken.getRawType();
        if (rawType.equals(char.class) || rawType.equals(Character.class)) {
            @SuppressWarnings("unchecked")
            Agent<V> castAgent = (Agent<V>) new AgentImpl();
            return castAgent;
        }
        return null;
    }

    private static final class AgentImpl implements Agent<Character> {

        @Override
        public @Nullable DefaultValues<Character> loadDefaultValues(@NonNull DefaultInit defaultInit) {
            return null;
        }

        @Override
        public @NonNull SerializeDeserialize<Character> makeSerializer() {
            return new SerializeDeserialize<Character>() {
                @Override
                public @NonNull LoadResult<@NonNull Character> deserialize(@NonNull DeserializeInput deser) {
                    if (deser.object() instanceof Character) {
                        return LoadResult.of((Character) deser.object());
                    }
                    return deser.requireString().flatMap(string -> {
                        char[] chars = string.toCharArray();
                        if (chars.length != 1) {
                            LibraryLang libraryLang = LibraryLang.Accessor.access(deser, DeserializeInput::getLocale);
                            return deser.throwError(libraryLang.wrongTypeForValue(
                                    deser.object(), libraryLang.character(), libraryLang.text())
                            );
                        }
                        return LoadResult.of(chars[0]);
                    });
                }

                @Override
                public void serialize(@NonNull Character value, @NonNull SerializeOutput ser) {
                    ser.outChar(value);
                }
            };
        }
    }
}
