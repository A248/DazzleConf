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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf.Configuration;
import space.arim.dazzleconf.DeveloperMistakeException;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.UpdateListener;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.reflect.TypeToken;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
}
