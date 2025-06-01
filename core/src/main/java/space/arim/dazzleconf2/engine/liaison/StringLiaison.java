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
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.reflect.TypeToken;

/**
 * Liaison for strings. Handles the unannotated {@code String}
 *
 */
public final class StringLiaison implements TypeLiaison {

    // Come on, seriously. Is anyone going to troll us by saying they want to use this string for real?
    static final String IF_MISSING_STAND_IN = "ausutfyguhibgvftrdfyguhijnbhvgfxserrftgyuhinbgvfcrxszeretfygubna";

    /**
     * Creates
     */
    public StringLiaison() {}

    @Override
    @SideEffectFree
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        return Agent.matchOnToken(typeToken, String.class, StringAgent::new);
    }

    private static final class StringAgent implements Agent<String> {
        @Override
        public @Nullable DefaultValues<String> loadDefaultValues(@NonNull DefaultInit defaultInit) {

            StringDefault stringDefault = defaultInit.methodAnnotations().getAnnotation(StringDefault.class);
            if (stringDefault != null) {
                String defaultValue = stringDefault.value();
                String ifMissing = stringDefault.ifMissing();
                // Micro-optimize: make the DefaultValues hold 1 field only if ifMissing is not set
                if (ifMissing.equals(IF_MISSING_STAND_IN)) {
                    return DefaultValues.simple(defaultValue);
                } else {
                    return new DefaultValues<String>() {
                        @Override
                        public @NonNull String defaultValue() {
                            return defaultValue;
                        }

                        @Override
                        public @NonNull String ifMissing() {
                            return ifMissing;
                        }
                    };
                }
            }
            return null;
        }

        @Override
        public @NonNull SerializeDeserialize<String> makeSerializer() {
            return new SerializeDeserialize<String>() {
                @Override
                public @NonNull LoadResult<@NonNull String> deserialize(@NonNull DeserializeInput deser) {
                    return deser.requireString();
                }

                @Override
                public void serialize(@NonNull String value, @NonNull SerializeOutput ser) {
                    ser.outString(value);
                }
            };
        }
    }
}
