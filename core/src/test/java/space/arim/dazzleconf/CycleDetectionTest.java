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

package space.arim.dazzleconf;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.ConfigurationBuilder;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.*;

public class CycleDetectionTest {

    public record SpecialOne() {

        public record SelfCycle() implements TypeLiaison {

            @Override
            public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
                return Agent.matchOnToken(typeToken, SpecialOne.class, () -> {
                    handshake.getOtherSerializer(new TypeToken<SpecialOne>() {});
                    assert false : "Should not reach here (test failed)";
                    return null;
                });
            }
        }

        public record SelfCycleLater() implements TypeLiaison {

            @Override
            public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
                return Agent.matchOnToken(typeToken, SpecialOne.class, () -> {
                    return new Agent<SpecialOne>() {
                        @Override
                        public @Nullable DefaultValues<SpecialOne> loadDefaultValues(@NonNull DefaultInit defaultInit) {
                            return null;
                        }

                        @Override
                        public @NonNull SerializeDeserialize<SpecialOne> makeSerializer() {
                            handshake.getOtherSerializer(new TypeToken<SpecialOne>() {});
                            assert false : "Should not reach here (test failed)";
                            return null;
                        }
                    };
                });
            }
        }

        public record GoToPrecious() implements TypeLiaison {

            @Override
            public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
                return Agent.matchOnToken(typeToken, SpecialOne.class, () -> {
                    handshake.getOtherSerializer(new TypeToken<ThePrecious>() {});
                    assert false : "Should not reach here (test failed)";
                    return null;
                });
            }
        }

        public record GoToPreciousLater() implements TypeLiaison {

            @Override
            public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
                return Agent.matchOnToken(typeToken, SpecialOne.class, () -> {
                    return new Agent<>() {
                        @Override
                        public @Nullable DefaultValues<SpecialOne> loadDefaultValues(@NonNull DefaultInit defaultInit) {
                            return null;
                        }

                        @Override
                        public @NonNull SerializeDeserialize<SpecialOne> makeSerializer() {
                            handshake.getOtherSerializer(new TypeToken<ThePrecious>() {
                            });
                            assert false : "Should not reach here (test failed)";
                            return null;
                        }
                    };
                });
            }
        }
    }

    public record ThePrecious() {
        public record GoToSpecialOne() implements TypeLiaison {

            @Override
            public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
                return Agent.matchOnToken(typeToken, ThePrecious.class, () -> {
                    handshake.getOtherSerializer(new TypeToken<SpecialOne>() {});
                    assert false : "Should not reach here (test failed)";
                    return null;
                });
            }
        }

        public record GoToSpecialOneLater() implements TypeLiaison {
            @Override
            public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
                return Agent.matchOnToken(typeToken, ThePrecious.class, () -> {
                    return new Agent<>() {
                        @Override
                        public @Nullable DefaultValues<ThePrecious> loadDefaultValues(@NonNull DefaultInit defaultInit) {
                            return null;
                        }

                        @Override
                        public @NonNull SerializeDeserialize<ThePrecious> makeSerializer() {
                            handshake.getOtherSerializer(new TypeToken<SpecialOne>() {
                            });
                            assert false : "Should not reach here (test failed)";
                            return null;
                        }
                    };
                });
            }
        }

        public record FunctionalLiaison() implements TypeLiaison {

            @Override
            public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
                return Agent.matchOnToken(typeToken, ThePrecious.class, () -> {
                    return new Agent<>() {
                        @Override
                        public @Nullable DefaultValues<ThePrecious> loadDefaultValues(@NonNull DefaultInit defaultInit) {
                            return null;
                        }

                        @Override
                        public @NonNull SerializeDeserialize<ThePrecious> makeSerializer() {
                            ThePrecious instance = new ThePrecious();
                            return new SerializeDeserialize<>() {
                                @Override
                                public @NonNull LoadResult<@NonNull ThePrecious> deserialize(@NonNull DeserializeInput deser) {
                                    return LoadResult.of(instance);
                                }

                                @Override
                                public void serialize(@NonNull ThePrecious value, @NonNull SerializeOutput ser) {
                                    assert value == instance;
                                    ser.outInt(0);
                                }
                            };
                        }
                    };
                });
            }
        }
    }

    public interface SpecialOneOnly {
        SpecialOne specialOne();
    }

    @Test
    public void instantCycle() {
        ConfigurationBuilder<SpecialOneOnly> builder = new ConfigurationBuilder<>(new TypeToken<SpecialOneOnly>() {})
                .addTypeLiaisons(new SpecialOne.SelfCycle());
        assertThrows(DeveloperMistakeException.class, builder::build);
    }

    @Test
    public void instantCycleWhenSerializerIsMade() {
        ConfigurationBuilder<SpecialOneOnly> builder = new ConfigurationBuilder<>(new TypeToken<SpecialOneOnly>() {})
                .addTypeLiaisons(new SpecialOne.SelfCycleLater());
        assertThrows(DeveloperMistakeException.class, builder::build);
    }

    public interface TwinDeviants {
        SpecialOne specialOne();

        ThePrecious thePrecious();
    }

    @Test
    public void cycleBetweenTwins() {
        ConfigurationBuilder<TwinDeviants> builder = new ConfigurationBuilder<>(new TypeToken<TwinDeviants>() {})
                .addTypeLiaisons(new SpecialOne.GoToPrecious(), new ThePrecious.GoToSpecialOne());
        assertThrows(DeveloperMistakeException.class, builder::build);
    }

    @Test
    public void cycleBetweenTwinsWhenSerializersAreMade() {
        ConfigurationBuilder<TwinDeviants> builder = new ConfigurationBuilder<>(new TypeToken<TwinDeviants>() {})
                .addTypeLiaisons(new SpecialOne.GoToPreciousLater(), new ThePrecious.GoToSpecialOneLater());
        assertThrows(DeveloperMistakeException.class, builder::build);
    }

    @Test
    public void cycleBetweenTwinsOneOfThemWaitsUntilSerializerIsMade() {
        ConfigurationBuilder<TwinDeviants> builder = new ConfigurationBuilder<>(new TypeToken<TwinDeviants>() {})
                .addTypeLiaisons(new SpecialOne.GoToPrecious(), new ThePrecious.GoToSpecialOneLater());
        assertThrows(DeveloperMistakeException.class, builder::build);
    }

    public interface NoCycleJustMultipleCalls {

        ThePrecious precious1();

        ThePrecious precious2();
    }

    @Test
    public void noCycleJustMultipleCalls() {
        ConfigurationBuilder<NoCycleJustMultipleCalls> builder = new ConfigurationBuilder<>(new TypeToken<NoCycleJustMultipleCalls>() {})
                .addTypeLiaisons(new ThePrecious.FunctionalLiaison());
        Configuration<NoCycleJustMultipleCalls> configuration = assertDoesNotThrow(builder::build);

        NoCycleJustMultipleCalls loaded;
        {
            DataTree.Mut dataTree = new DataTree.Mut();
            dataTree.set("precious1", new DataEntry(1));
            dataTree.set("precious2", new DataEntry(2));
            loaded = configuration.readFrom(dataTree).getOrThrow();
        }
        DataTree.Mut output = new DataTree.Mut();
        configuration.writeTo(loaded, output);
        assertEquals(new DataEntry(0), output.get("precious1"));
        assertEquals(new DataEntry(0), output.get("precious2"));
    }
}
