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

package space.arim.dazzleconf.internals;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.internals.ImmutableCollections;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImmutableCollectionsTest {

    @Test
    public void lists() {
        assertEquals(List.of(1), ImmutableCollections.listOf(1));
        assertEquals(List.of(1), ImmutableCollections.listOf(new Integer[] {1}));
        assertEquals(List.of("a", "b", "c"), ImmutableCollections.listOf("a", "b", "c"));
        assertEquals(List.of("c", "c", "c"), ImmutableCollections.listOf("c", "c", "c"));
    }

    @Test
    public void sets() {
        assertEquals(Set.of(1), ImmutableCollections.setOf(1));
        assertEquals(Set.of("a", "b", "c"), ImmutableCollections.setOf(Arrays.asList("a", "b", "c")));
        assertEquals(Set.of("c"), ImmutableCollections.setOf(Arrays.asList("c", "c", "c")));
    }
}
