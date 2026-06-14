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

package space.arim.dazzleconf.engine;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf.Configuration;
import space.arim.dazzleconf.ConfigurationDefinition;
import space.arim.dazzleconf.LoadResult;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.DefaultKeyMapper;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.backend.Printable;
import space.arim.dazzleconf.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NondeterministicLiaisonTest {

    public enum Criminal {
        INCONSPICUOUS,
        CRIME,
        VANISH;

        @Nullable Criminal rotate() {
            return switch (this) {
                case INCONSPICUOUS -> CRIME;
                case CRIME -> VANISH;
                case VANISH -> null;
            };
        }
    }

    private static final class CriminalLiaison implements TypeLiaison {

        @Override
        public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
            return Agent.matchOnToken(typeToken, Criminal.class, CriminalAgent::new);
        }
    }

    private static final class CriminalAgent implements TypeLiaison.Agent<Criminal> {

        @Override
        public @Nullable DefaultValues<Criminal> loadDefaultValues(TypeLiaison.@NonNull DefaultInit<Criminal> defaultInit) {
            return new DefaultValues<Criminal>() {
                @Override
                public @NonNull Criminal defaultValue() {
                    return Criminal.INCONSPICUOUS;
                }

                @Override
                public @NonNull Criminal ifMissing() {
                    return Criminal.VANISH;
                }
            };
        }

        @Override
        public @NonNull SerializeDeserialize<Criminal> makeSerializer() {
            return new SerializeDeserialize<Criminal>() {
                @Override
                public @NonNull LoadResult<Criminal> deserialize(@NonNull DeserializeInput deser) {
                    return deser.requireString().flatMap(string -> {
                        Criminal value;
                        try {
                            value = Criminal.valueOf(string);
                        } catch (IllegalArgumentException e) {
                            return LoadResult.failure(deser.buildError(Printable.preBuilt("Bad argument")));
                        }
                        return LoadResult.of(value);
                    });
                }

                @Override
                public @NonNull LoadResult<Criminal> deserializeUpdate(@NonNull DeserializeInput deser,
                                                                       @NonNull SerializeOutput updateTo) {
                    return deserialize(deser).map(criminal -> {
                        Criminal rotated = criminal.rotate();
                        if (rotated == null) {
                            updateTo.outNone();
                        } else {
                            updateTo.outString(rotated.name());
                        }
                        deser.notifyUpdate(KeyPath.empty(), UpdateReason.UPDATED);
                        return criminal;
                    });
                }

                @Override
                public void serialize(@NonNull Criminal value, @NonNull SerializeOutput ser) {
                    Criminal rotate = value.rotate();
                    if (rotate == null) {
                        ser.outNone();
                    } else {
                        ser.outString(value.name());
                    }
                }
            };
        }
    }

    public interface Config {

        Criminal value();
    }

    private Configuration<Config> configuration;

    @BeforeEach
    public void setConfiguration() {
        configuration = Configuration.defaultBuilder(Config.class)
                .addTypeLiaisons(new CriminalLiaison())
                .build();
    }

    @Test
    public void missingValueSerializeToNone(@Mock UpdateListener updateListener) {
        DataTree.Mut dataTree = new DataTree.Mut();
        configuration.readWithUpdate(dataTree, new ConfigurationDefinition.ReadWithUpdateOptions() {
            @Override
            public @NonNull KeyMapper keyMapper() {
                return new DefaultKeyMapper();
            }

            @Override
            public @NonNull KeyPath keyPath() {
                return new KeyPath.Immut();
            }

            @Override
            public @NonNull Interprocessor getInterprocessor() {
                return Interprocessor.DEFAULT;
            }

            @Override
            public void notifyUpdate(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {
                updateListener.notifyUpdate(entryPath, updateReason);
            }
        });
        // Unaffected: missing value serialized to none, so tree stays empty
        assertEquals(0, dataTree.size());
        // However, an update should still have been triggered, because absent values are not deserialized
        verify(updateListener).notifyUpdate(new KeyPath.Immut("value"), UpdateReason.MISSING);
    }

    @Test
    public void readUpdateSerializeToNone(@Mock UpdateListener updateListener) {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.put("value", new DataEntry("VANISH"));
        configuration.readWithUpdate(dataTree, new ConfigurationDefinition.ReadWithUpdateOptions() {
            @Override
            public @NonNull KeyMapper keyMapper() {
                return new DefaultKeyMapper();
            }

            @Override
            public @NonNull KeyPath keyPath() {
                return KeyPath.empty();
            }

            @Override
            public @NonNull Interprocessor getInterprocessor() {
                return Interprocessor.DEFAULT;
            }

            @Override
            public void notifyUpdate(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason) {
                updateListener.notifyUpdate(entryPath, updateReason);
            }
        });
        // Missing value serialized to none, so entry must be removed from map
        assertEquals(0, dataTree.size());
        verify(updateListener).notifyUpdate(new KeyPath.Immut("value"), UpdateReason.UPDATED);
    }
}
