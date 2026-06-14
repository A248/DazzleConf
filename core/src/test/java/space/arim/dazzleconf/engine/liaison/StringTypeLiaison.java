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

package space.arim.dazzleconf.engine.liaison;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.LoadResult;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.DefaultValues;
import space.arim.dazzleconf.engine.DeserializeInput;
import space.arim.dazzleconf.engine.SerializeDeserialize;
import space.arim.dazzleconf.engine.SerializeOutput;
import space.arim.dazzleconf.engine.TypeLiaison;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.reflect.TypeToken;

import java.util.function.Function;

record StringTypeLiaison<T extends StringType>(Class<T> type, Function<String, T> ctor) implements TypeLiaison {
    @Override
    public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
        return Agent.matchOnToken(typeToken, type, AgentImpl::new);
    }

    private final class AgentImpl implements Agent<T> {

        @Override
        public @Nullable DefaultValues<T> loadDefaultValues(@NonNull DefaultInit<T> defaultInit) {
            return defaultInit.methodDefault();
        }

        @Override
        public @NonNull SerializeDeserialize<T> makeSerializer() {
            return new SerializeDeserialize<T>() {
                @Override
                public @NonNull LoadResult<@NonNull T> deserialize(@NonNull DeserializeInput deser) {
                    return deser.requireString().map(ctor);
                }

                @Override
                public @NonNull LoadResult<@NonNull T> deserializeUpdate(@NonNull DeserializeInput deser,
                                                                         @NonNull SerializeOutput outputForUpdate) {
                    return deser.requireString().map(inputVal -> {
                        T val = ctor.apply(inputVal);
                        String reser = val.value();
                        if (!inputVal.equals(reser)) {
                            deser.notifyUpdate(KeyPath.empty(), UpdateReason.UPDATED);
                            serialize(val, outputForUpdate);
                        }
                        return val;
                    });
                }

                @Override
                public void serialize(@NonNull T value, @NonNull SerializeOutput ser) {
                    String outValue = value.value();
                    if (outValue == null) {
                        ser.outNone();
                    } else {
                        ser.outString(outValue);
                    }
                }
            };
        }
    }
}
