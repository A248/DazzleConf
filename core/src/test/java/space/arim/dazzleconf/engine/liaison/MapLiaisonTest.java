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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf.Configuration;
import space.arim.dazzleconf.ConfigurationDefinition;
import space.arim.dazzleconf.DeveloperMistakeException;
import space.arim.dazzleconf.LoadResult;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.DefaultKeyMapper;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.Interprocessor;
import space.arim.dazzleconf.engine.UpdateListener;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.reflect.TypeToken;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MapLiaisonTest {

    interface Config<K, V> {

        Map<K, V> values();
    }

    @Test
    public void loadRegular(@Mock UpdateListener updateListener) {
        DataTree.Mut dataTree = new DataTree.Mut();
        DataTree.Mut innerTree = new DataTree.Mut();
        innerTree.put("friends", new DataEntry("3"));
        innerTree.put("are  ", new DataEntry(3));
        innerTree.put("good", new DataEntry("3"));
        dataTree.put("values", new DataEntry(innerTree));
        {
            Map<String, Integer> values = StringType.configuration(new TypeToken<Config<String, Integer>>() {})
                    .readFrom(dataTree).getOrThrow().values();
            assertEquals(3, values.size());
        }
        {
            Map<TrimOnDeser, Integer> values = StringType.configuration(new TypeToken<Config<TrimOnDeser, Integer>>() {})
                    .readFrom(dataTree, updateListener).getOrThrow().values();
            assertEquals(3, values.size());
            assertEquals(
                    Map.of(
                            new TrimOnDeser("friends"), 3,
                            new TrimOnDeser("are"), 3,
                            new TrimOnDeser("good"), 3
                    ),
                    values
            );
            verify(updateListener).notifyUpdate(new KeyPath.Immut("values", "friends"), UpdateReason.UPDATED);
            verify(updateListener).notifyUpdate(new KeyPath.Immut("values", "good"), UpdateReason.UPDATED);
            verifyNoMoreInteractions(updateListener);
        }
    }

    @Test
    public void deserDuplicates(@Mock UpdateListener updateListener) {
        DataTree.Mut dataTree = new DataTree.Mut();
        DataTree.Mut innerTree = new DataTree.Mut();
        innerTree.put("whitespace", new DataEntry("intro"));
        innerTree.put("is", new DataEntry("hi"));
        innerTree.put("     bad ", new DataEntry("3"));
        innerTree.put("bad", new DataEntry("3"));
        dataTree.put("values", new DataEntry(innerTree));
        Map<TrimOnDeser, String> values = StringType.configuration(new TypeToken<Config<TrimOnDeser, String>>() {})
                .readFrom(dataTree, updateListener).getOrThrow().values();
        assertEquals(3, values.size());
        assertEquals(
                Map.of(
                        new TrimOnDeser("whitespace"), "intro",
                        new TrimOnDeser("is"), "hi",
                        new TrimOnDeser("bad"), "3"
                ),
                values
        );
        verify(updateListener).notifyUpdate(new KeyPath.Immut("values"), UpdateReason.OTHER);
    }

    @Test
    public void serDuplicates() {
        Config<ClipOnSer, Integer> okay = () ->
                Map.of(new ClipOnSer("parts"), 2, new ClipOnSer("pa"), 1);
        Config<ClipOnSer, Integer> withDuplicates = () ->
                Map.of(new ClipOnSer("parts"), 2, new ClipOnSer("par"), 2, new ClipOnSer("pa"), 1);
        Configuration<Config<ClipOnSer, Integer>> definition = StringType.configuration(new TypeToken<>() {});
        assertDoesNotThrow(() -> definition.writeTo(okay, new DataTree.Mut()));
        assertThrows(DeveloperMistakeException.class, () -> definition.writeTo(withDuplicates, new DataTree.Mut()));
    }

    private <C> void verifySingleFailureNoUpdates(TypeToken<C> configType, DataTree.Mut dataTree,
                                                  UpdateListener mockUpdateListener) {
        Configuration<C> definition = Configuration.defaultBuilder(configType)
                .addDefaultTypeLiaisons()
                .build();
        LoadResult<C> result = definition.readFrom(dataTree.intoImmut());
        assertTrue(result.isFailure());
        assertEquals(1, result.getErrorContexts().size());
        result = definition.readWithUpdate(dataTree, new ConfigurationDefinition.ReadWithUpdateOptions() {
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
                mockUpdateListener.notifyUpdate(entryPath, updateReason);
            }
        });
        assertTrue(result.isFailure());
        assertEquals(1, result.getErrorContexts().size());
        verifyNoInteractions(mockUpdateListener);
    }

    @Test
    public void failureOnKey(@Mock UpdateListener updateListener) {
        DataTree.Mut dataTree = new DataTree.Mut();
        DataTree.Mut innerTree = new DataTree.Mut();
        innerTree.put(1, new DataEntry("friends"));
        innerTree.put(2, new DataEntry("are"));
        innerTree.put("iuahbjnkz", new DataEntry("good"));
        dataTree.put("values", new DataEntry(innerTree));
        verifySingleFailureNoUpdates(
                new TypeToken<Config<Integer, String>>() {}, dataTree, updateListener
        );
    }

    @Test
    public void failureOnValue(@Mock UpdateListener updateListener) {
        DataTree.Mut dataTree = new DataTree.Mut();
        DataTree.Mut innerTree = new DataTree.Mut();
        innerTree.put("friends", new DataEntry(1));
        innerTree.put("are", new DataEntry(2));
        innerTree.put("good", new DataEntry("iasdzhiuas"));
        dataTree.put("values", new DataEntry(innerTree));
        verifySingleFailureNoUpdates(
                new TypeToken<Config<String, Integer>>() {}, dataTree, updateListener
        );
    }

    @Test
    public void updateReplace() {
        DataTree.Mut dataTree = new DataTree.Mut();
        DataTree.Mut innerTree = new DataTree.Mut();
        innerTree.put("regular", new DataEntry("stay"));
        innerTree.put("trim-me", new DataEntry("     trim "));
        innerTree.put("another-trim-me", new DataEntry(" \n  trim2 "));
        innerTree.put("good", new DataEntry("b"));
        dataTree.put("values", new DataEntry(innerTree));

        Configuration<Config<String, TrimOnDeser>> configuration = StringType.configuration(new TypeToken<>() {});
        Set<Map.Entry<KeyPath, UpdateReason>> updates = new HashSet<>();
        ConfigurationDefinition.ReadWithUpdateOptions options = new ConfigurationDefinition.ReadWithUpdateOptions() {
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
                updates.add(Map.entry(entryPath, updateReason));
            }
        };
        Config<String, TrimOnDeser> config = assertDoesNotThrow(() ->
                configuration.readWithUpdate(dataTree, options).getOrThrow());
        assertEquals(
                new HashSet<>(Arrays.asList(
                        Map.entry(new KeyPath.Immut("values", "trim-me"), UpdateReason.UPDATED),
                        Map.entry(new KeyPath.Immut("values", "another-trim-me"), UpdateReason.UPDATED)
                )), updates
        );
        assertEquals(
                Map.of(
                        "regular", "stay", "trim-me", "trim",
                        "another-trim-me", "trim2", "good", "b"
                ).entrySet(),
                config.values().entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey(), entry.getValue().value()))
                        .collect(Collectors.toSet())
        );
    }
}
