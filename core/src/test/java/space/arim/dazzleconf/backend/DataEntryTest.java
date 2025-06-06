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
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataEntryTest {

    private final DataEntry entry = new DataEntry("value");

    @Test
    public void constructor() {
        assertThrows(IllegalArgumentException.class, () -> new DataEntry(new DataEntryTest()));
        assertThrows(IllegalArgumentException.class, () -> new DataEntry(new LinkedHashMap<>()));
        assertThrows(IllegalArgumentException.class, () -> new DataEntry(null));
    }

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

    @Test
    public void comments() {
        CommentData comments = CommentData.empty().setAt(CommentLocation.ABOVE, "hi");
        DataEntry commented = entry.withComments(comments);
        assertEquals(comments, commented.getComments());
        assertEquals(List.of("hi"), commented.getComments(CommentLocation.ABOVE));
    }

    @Test
    public void validateValue() {
        // Valid
        assertTrue(DataEntry.validateValue("hi"));
        assertTrue(DataEntry.validateValue(0));
        assertTrue(DataEntry.validateValue(0L));
        assertTrue(DataEntry.validateValue(0f));
        assertTrue(DataEntry.validateValue(0d));
        assertTrue(DataEntry.validateValue((byte) 0));
        assertTrue(DataEntry.validateValue((short) 0));
        assertTrue(DataEntry.validateValue(false));
        assertTrue(DataEntry.validateValue('c'));
        assertTrue(DataEntry.validateValue(new DataTree.Immut()));
        // Invalid
        assertFalse(DataEntry.validateValue(new int[] {1, 3, 4}));
        assertFalse(DataEntry.validateValue(this));
        assertFalse(DataEntry.validateValue(new LinkedHashMap<>()));
        assertFalse(DataEntry.validateValue(null));
    }

    @Test
    public void equals() {
        EqualsVerifier.forClass(DataEntry.class).withOnlyTheseFields("value").verify();
    }

    @Test
    public void toStringTest() {
        assertTrue(new DataEntry("hello").toString().contains(DataEntry.class.getSimpleName()));
        assertTrue(new DataEntry("hello").toString().contains("hello"));
    }
}
