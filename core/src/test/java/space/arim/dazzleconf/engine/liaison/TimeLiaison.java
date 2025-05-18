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

package space.arim.dazzleconf.engine.liaison;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * A liaison for {@link java.time.Instant} that uses the Java time API. It relies on a provided
 * {@link java.time.format.DateTimeFormatter} and reads/writes from a string value.
 * <p>
 * This liaison does <b>NOT</b> service the {@code java.util.Date} class.
 *
 */
public final class TimeLiaison implements TypeLiaison {

    private final DateTimeFormatter formatter;

    /**
     * Creates from the provided formatter
     *
     */
    public TimeLiaison(@NonNull DateTimeFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter);
    }

    @Override
    @SideEffectFree
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        return Agent.matchOnToken(typeToken, Instant.class, InstantAgent::new);
    }

    // TODO support more than Instant

    private final class InstantAgent implements Agent<Instant> {

        @Override
        public @Nullable DefaultValues<Instant> loadDefaultValues(@NonNull DefaultInit defaultInit) {
            return null;
        }

        @Override
        public @NonNull SerializeDeserialize<Instant> makeSerializer() {
            return new SerializeDeserialize<Instant>() {
                @Override
                public @NonNull LoadResult<@NonNull Instant> deserialize(@NonNull DeserializeInput deser) {
                    return deser.requireString().flatMap((string) -> {
                        Instant parsed;
                        try {
                            parsed = formatter.parse(string, Instant::from);
                        } catch (DateTimeParseException failure) {
                            return deser.throwError(failure.getMessage());
                        }
                        return LoadResult.of(parsed);
                    });
                }

                @Override
                public @NonNull LoadResult<@NonNull Instant> deserializeUpdate(@NonNull DeserializeInput deser, @NonNull SerializeOutput updateTo) {
                    return deserialize(deser);
                }

                @Override
                public void serialize(@NonNull Instant value, @NonNull SerializeOutput ser) {
                    ser.outString(formatter.format(value));
                }
            };
        }
    }
}
