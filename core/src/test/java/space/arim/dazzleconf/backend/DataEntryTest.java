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

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.backend.DataEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DataEntryTest {

    @Test
    public void value() {
        DataEntry entry1 = new DataEntry("value1");
        DataEntry entry2 = entry1.withValue("value2");
        assertEquals("value1", entry1.getValue());
        assertEquals("value2", entry2.getValue());
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
}
