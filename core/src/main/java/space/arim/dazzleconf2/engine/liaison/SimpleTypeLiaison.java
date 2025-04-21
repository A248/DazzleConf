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
import space.arim.dazzleconf2.engine.DefaultValues;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.Objects;

/**
 * A simple type liaison that wraps a {@link SerializeDeserialize}.
 * <p>
 * This class is probably appropriate for most user types. It provides serialization for a single user type, and relies
 * on the configuration interface method to supply a default value by being a default method.
 *
 * @param <U> the type being provided
 */
public final class SimpleTypeLiaison<U> implements TypeLiaison {

    private final TypeToken<U> typeToken;
    private final SerializeDeserialize<U> serializeDeserialize;

    /**
     * Creates from a type token and the provided serialization
     *
     * @param typeToken the type token
     * @param serializeDeserialize the serialization
     */
    public SimpleTypeLiaison(@NonNull TypeToken<U> typeToken, @NonNull SerializeDeserialize<U> serializeDeserialize) {
        this.typeToken = Objects.requireNonNull(typeToken, "typeToken");
        this.serializeDeserialize = Objects.requireNonNull(serializeDeserialize, "serializeDeserialize");
    }

    @Override
    @SideEffectFree
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        if (this.typeToken.equals(typeToken)) {
            // Safe cast - the type token matches
            @SuppressWarnings("unchecked")
            Agent<V> casted = (Agent<V>) new Agent<U>() {
                @Override
                public @Nullable DefaultValues<U> loadDefaultValues(@NonNull AnnotationContext annotationContext) {
                    return null;
                }

                @Override
                public @NonNull SerializeDeserialize<U> makeSerializer() {
                    return serializeDeserialize;
                }
            };
            return casted;
        }
        return null;
    }
}
