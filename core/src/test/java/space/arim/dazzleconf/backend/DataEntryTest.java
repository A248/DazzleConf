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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DataEntryTest {

    private final DataEntry entry = new DataEntry("value");

    @Test
    public void value() {
        DataEntry another = entry.withValue("another");
        assertEquals("value", entry.getValue());
        assertEquals("another", another.getValue());
    }

    @Test
    public void lineNumber() {
        DataEntry firstEntry = new DataEntry(45);
        assertNull(firstEntry.getLineNumber());
        DataEntry entryWith42 = firstEntry.withLineNumber(42);
        assertEquals(42, entryWith42.getLineNumber());
        assertNull(firstEntry.getLineNumber());

        DataEntry clearedAgain = entryWith42.clearLineNumber();
        assertNull(clearedAgain.getLineNumber());
        assertEquals(42, entryWith42.getLineNumber());
    }

    @Nested
    class CommentsTest {

        @Test
        public void setAt() {
            DataEntry.Comments empty = DataEntry.Comments.empty();
            DataEntry.Comments c1c2 = empty.setAt(CommentLocation.ABOVE, List.of("c1", "c2"));
            assertEquals(empty, DataEntry.Comments.empty(), "immutable");
            assertEquals(List.of("c1", "c2"), c1c2.getAt(CommentLocation.ABOVE));
            assertEquals(List.of(), c1c2.getAt(CommentLocation.INLINE));
        }

        @Test
        public void append() {
            DataEntry.Comments empty = DataEntry.Comments.empty();
            DataEntry.Comments below = empty.setAt(CommentLocation.BELOW, "b1", "b2");
            DataEntry.Comments overlap = empty.setAt(CommentLocation.BELOW, "overlap")
                    .setAt(CommentLocation.INLINE, "untouched");
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

        @Test
        public void appendAt() {
            DataEntry.Comments empty = DataEntry.Comments.empty();
            DataEntry.Comments c1c2 = empty.setAt(CommentLocation.ABOVE, List.of("c1", "c2"));
            assertEquals(c1c2, empty.appendAt(CommentLocation.ABOVE, List.of("c1", "c2")));
            List<String> inline = List.of("beside value");
            DataEntry.Comments addInline = c1c2.appendAt(CommentLocation.INLINE, inline);
            assertEquals(inline, addInline.getAt(CommentLocation.INLINE));
            assertEquals(List.of(), addInline.getAt(CommentLocation.BELOW));

            DataEntry.Comments combined = addInline.appendAt(CommentLocation.ABOVE, "to append");
            assertEquals(List.of("c1", "c2", "to append"), combined.getAt(CommentLocation.ABOVE));
            assertEquals(List.of(), combined.getAt(CommentLocation.BELOW));
        }

        @Test
        public void empty() {
            DataEntry.Comments empty = DataEntry.Comments.empty();
            for (CommentLocation location : CommentLocation.values()) {
                assertEquals(List.of(), empty.getAt(location));
            }
            DataEntry.Comments c1c2 = empty.setAt(CommentLocation.ABOVE, List.of("c1", "c2"));
            assertEquals(c1c2, empty.append(c1c2));
            assertEquals(c1c2, c1c2.append(empty));
            List<String> inline = List.of("beside value");
            DataEntry.Comments addInline = empty.appendAt(CommentLocation.INLINE, inline);
            DataEntry.Comments setInline = empty.setAt(CommentLocation.INLINE, inline);
            assertEquals(setInline, addInline);
            assertEquals(setInline.getAt(CommentLocation.ABOVE), addInline.getAt(CommentLocation.ABOVE));
        }
    }
}
