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

package space.arim.dazzleconf.backend;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ImmutabilityGuard;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.engine.Comments;
import space.arim.dazzleconf2.internals.ImmutableCollections;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CommentDataTest {

    static class ImmutGuard extends ImmutabilityGuard<CommentData, Map<CommentLocation, List<String>>> {

        ImmutGuard(CommentData value, CommentData...extra) {
            super(value, extra);
        }

        @Override
        protected Map<CommentLocation, List<String>> takeSnapshot(CommentData value) {
            Map<CommentLocation, List<String>> data = new EnumMap<>(CommentLocation.class);
            for (CommentLocation location : CommentLocation.values()) {
                List<String> source = value.getAt(location);
                testImmutable(source, "dummy");
                data.put(location, ImmutableCollections.listOf(source));
            }
            return ImmutableCollections.mapOf(data);
        }
    }

    @Test
    public void setAt() {
        CommentData empty = CommentData.empty();
        try (ImmutGuard ignored = new ImmutGuard(empty)) {
            CommentData c1c2 = empty.setAt(CommentLocation.ABOVE, List.of("c1", "c2"));
            assertEquals(List.of("c1", "c2"), c1c2.getAt(CommentLocation.ABOVE));
            assertEquals(List.of(), c1c2.getAt(CommentLocation.INLINE));
        }
    }

    @Test
    public void isEmpty() {
        CommentData c1c2 = CommentData.empty().setAt(CommentLocation.ABOVE, List.of("c1", "c2"));
        assertFalse(c1c2.isEmpty());
        assertEquals(List.of("c1", "c2"), c1c2.getAt(CommentLocation.ABOVE), "immutable");
    }

    @Test
    public void clearAt() {
        CommentData empty = CommentData.empty();
        CommentData c1c2 = empty.setAt(CommentLocation.ABOVE, List.of("c1", "c2"));
        try (ImmutGuard ignored = new ImmutGuard(empty, c1c2)) {
            assertEquals(c1c2, c1c2.clearAt(CommentLocation.INLINE, CommentLocation.BELOW));
            assertEquals(c1c2, c1c2.clearAt());
            assertEquals(c1c2, c1c2.clearAt(CommentLocation.INLINE));
            assertEquals(empty, c1c2.clearAt(CommentLocation.ABOVE));
            assertEquals(empty, c1c2.clearAt(CommentLocation.values()));
            assertEquals(empty, empty.clearAt(CommentLocation.values()));
            assertEquals(List.of("c1", "c2"), c1c2.getAt(CommentLocation.ABOVE), "immutable");
        }
    }

    @Test
    public void clearAtWithLeftover() {
        CommentData empty = CommentData.empty();
        CommentData c1c2 = empty.setAt(CommentLocation.ABOVE, List.of("c1", "c2"));
        CommentData greeting = c1c2.setAt(CommentLocation.INLINE, "hi");
        try (ImmutGuard ignored = new ImmutGuard(empty, c1c2, greeting)) {
            assertEquals(greeting, greeting.clearAt(CommentLocation.BELOW));
            assertEquals(greeting, greeting.clearAt());
            assertEquals(c1c2, greeting.clearAt(CommentLocation.INLINE));
            assertEquals(List.of("hi"), greeting.clearAt(CommentLocation.ABOVE).getAt(CommentLocation.INLINE));
            assertEquals(empty, greeting.clearAt(CommentLocation.values()));
            assertEquals(List.of("c1", "c2"), greeting.getAt(CommentLocation.ABOVE), "immutable");
            assertEquals(List.of("hi"), greeting.getAt(CommentLocation.INLINE), "immutable");
        }
    }

    @Test
    public void clearAtViaSet() {
        CommentData empty = CommentData.empty();
        CommentData c1c2 = empty.setAt(CommentLocation.ABOVE, List.of("c1", "c2"));
        try (ImmutGuard ignored = new ImmutGuard(empty, c1c2)) {
            assertEquals(c1c2, c1c2.setAt(CommentLocation.INLINE, List.of()));
            assertEquals(empty, c1c2.setAt(CommentLocation.ABOVE, List.of()));
            assertEquals(c1c2, c1c2.setAt(CommentLocation.INLINE));
            assertEquals(empty, c1c2.setAt(CommentLocation.ABOVE));
            assertEquals(List.of("c1", "c2"), c1c2.getAt(CommentLocation.ABOVE), "immutable");
        }
    }

    @Test
    public void append() {
        CommentData empty = CommentData.empty();
        CommentData below = empty.setAt(CommentLocation.BELOW, "b1", "b2");
        CommentData overlap = empty.setAt(CommentLocation.BELOW, "overlap")
                .setAt(CommentLocation.INLINE, "untouched");
        try (ImmutGuard ignored = new ImmutGuard(empty, below, overlap)) {
            assertEquals(
                    empty.setAt(CommentLocation.BELOW, "b1", "b2", "overlap")
                            .setAt(CommentLocation.INLINE, "untouched"),
                    below.append(overlap)
            );
            assertEquals(
                    empty.setAt(CommentLocation.BELOW, "overlap", "b1", "b2")
                            .setAt(CommentLocation.INLINE, "untouched"),
                    overlap.append(below)
            );
            assertEquals(List.of(), below.append(overlap).getAt(CommentLocation.ABOVE));
            assertEquals(List.of(), overlap.append(below).getAt(CommentLocation.ABOVE));
        }
    }

    @Test
    public void appendAt() {
        CommentData empty = CommentData.empty();
        CommentData c1c2 = empty.setAt(CommentLocation.ABOVE, List.of("c1", "c2"));
        try (ImmutGuard ignored = new ImmutGuard(empty, c1c2)) {
            assertEquals(c1c2, empty.appendAt(CommentLocation.ABOVE, List.of("c1", "c2")));
            assertSame(c1c2, c1c2.appendAt(CommentLocation.INLINE, List.of()));

            List<String> inline = List.of("beside value");
            CommentData addInline = c1c2.appendAt(CommentLocation.INLINE, inline);
            assertEquals(inline, addInline.getAt(CommentLocation.INLINE));
            assertEquals(List.of(), addInline.getAt(CommentLocation.BELOW));

            CommentData combined = addInline.appendAt(CommentLocation.ABOVE, "to append");
            assertEquals(List.of("c1", "c2", "to append"), combined.getAt(CommentLocation.ABOVE));
            assertEquals(List.of(), combined.getAt(CommentLocation.BELOW));
        }
    }

    @Test
    public void empty() {
        CommentData empty = CommentData.empty();
        try (ImmutGuard ignored = new ImmutGuard(empty)) {
            assertTrue(empty.isEmpty());
            for (CommentLocation location : CommentLocation.values()) {
                assertEquals(List.of(), empty.getAt(location));
            }
            CommentData c1c2 = empty.setAt(CommentLocation.ABOVE, List.of("c1", "c2"));
            assertEquals(c1c2, empty.append(c1c2));
            assertEquals(c1c2, c1c2.append(empty));
            List<String> inline = List.of("beside value");
            CommentData addInline = empty.appendAt(CommentLocation.INLINE, inline);
            CommentData setInline = empty.setAt(CommentLocation.INLINE, inline);
            assertEquals(setInline, addInline);
            assertEquals(setInline.getAt(CommentLocation.ABOVE), addInline.getAt(CommentLocation.ABOVE));
        }
    }

    @Test
    public void buildFrom() {
        @Comments(value = "inline1", location = CommentLocation.INLINE)
        @Comments({"line1", "line2"}) // ABOVE
        @Comments(value = "line3", location = CommentLocation.ABOVE)
        @Comments(value = {}, location = CommentLocation.BELOW)
        @Comments(value = {"inline2"}, location = CommentLocation.INLINE)
        class TestOn { }

        @Comments(value = {}, location = CommentLocation.INLINE)
        @Comments(value = {}, location = CommentLocation.BELOW)
        class Empty { }

        assertEquals(CommentData.empty(), CommentData.buildFrom(new Comments[0]));
        assertEquals(CommentData.empty(), CommentData.buildFrom(Empty.class.getAnnotationsByType(Comments.class)));
        assertEquals(
                CommentData.empty()
                        .setAt(CommentLocation.ABOVE, "line1", "line2", "line3")
                        .setAt(CommentLocation.INLINE, "inline1", "inline2"),
                CommentData.buildFrom(TestOn.class.getAnnotationsByType(Comments.class))
        );
    }

    @Test
    public void equals() {
        EqualsVerifier.forClass(CommentData.class).verify();
    }

    @Test
    public void toStringTest() {
        assertTrue(CommentData.empty().setAt(CommentLocation.ABOVE, "i love comments").toString().contains("love"));
    }
}
