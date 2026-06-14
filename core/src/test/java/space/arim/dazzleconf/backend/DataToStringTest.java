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

package space.arim.dazzleconf.backend;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.engine.CommentLocation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataToStringTest {

    private static String showHash(Object obj) {
        return '@' + Integer.toHexString(System.identityHashCode(obj));
    }

    @Test
    public void simpleTrees() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.put("hi", new DataEntry(1));
        dataTree.put("newliner", new DataEntry("line:\n"));
        DataTree.Immut emptyImmut = new DataTree.Immut();
        dataTree.put("subtree", new DataEntry(emptyImmut));
        assertEquals("DataTree.Mut" + showHash(dataTree) + " {\n" +
                "  hi={value=1},\n" +
                "  newliner={value=\"line:\\n\"},\n" +
                "  subtree={value=DataTree.Immut" + showHash(emptyImmut) + " {}},\n" +
                "}", dataTree.toString());
    }

    @Test
    public void simpleLists() {
        DataList.Mut entryList = new DataList.Mut();
        entryList.add(new DataEntry(false));
        entryList.add(new DataEntry("hello").withLineNumber(5));
        DataList.Immut emptyImmut = new DataList.Immut();
        entryList.add(new DataEntry(emptyImmut));
        DataEntry topEntry = new DataEntry(entryList);
        assertEquals("DataEntry{value=DataList.Mut" + showHash(entryList) + " [\n" +
                "  {value=false},\n" +
                "  {value=\"hello\", lineNumber=5},\n" +
                "  {value=DataList.Immut" + showHash(emptyImmut) + " []},\n" +
                "]}", topEntry.toString());
    }

    @Test
    public void entryWithComments() {
        DataEntry withComments = new DataEntry(4).withComments(CommentData.empty()
                .setAt(CommentLocation.ABOVE, "above1", "above2")
                .setAt(CommentLocation.INLINE, "inline")
        );
        assertEquals("DataEntry{value=4, comments={ABOVE=[above1, above2], INLINE=[inline]}}", withComments.toString());
        assertEquals("DataEntry{value=4, lineNumber=5, comments={ABOVE=[above1, above2], INLINE=[inline]}}", withComments.withLineNumber(5).toString());
    }

    @Test
    public void complexTree() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.put("hi", new DataEntry(1));
        DataTree.Mut subTree = new DataTree.Mut();
        subTree.put("subkey", new DataEntry(true).withLineNumber(10));
        subTree.put("escape-value", new DataEntry("\u007F"));
        dataTree.put("subtree", new DataEntry(subTree));
        List<DataEntry> entryList = new ArrayList<>();
        DataList.Mut insideEntryList = new DataList.Mut();
        entryList.add(new DataEntry(false));
        entryList.add(new DataEntry(insideEntryList));
        DataList.Immut immutList = new DataList.Immut(entryList);
        dataTree.put("list", new DataEntry(immutList));

        assertEquals("DataTree.Mut" + showHash(dataTree) + " {\n" +
                "  hi={value=1},\n" +
                "  subtree={value=DataTree.Mut" + showHash(subTree) + " {\n" +
                "    subkey={value=true, lineNumber=10},\n" +
                "    escape-value={value=\"\\u007f\"},\n" +
                "  }},\n" +
                "  list={value=DataList.Immut" + showHash(immutList) + " [\n" +
                "    {value=false},\n" +
                "    {value=DataList.Mut" + showHash(insideEntryList) + " []},\n" +
                "  ]},\n" +
                "}", dataTree.toString());
    }

    @Test
    public void cyclic() {
        DataTree.Mut cyclicTree = new DataTree.Mut();
        cyclicTree.put("simple", new DataEntry("value"));
        cyclicTree.put("self", new DataEntry(cyclicTree));
        String output = assertDoesNotThrow(cyclicTree::toString);
        assertEquals(
                "DataTree.Mut" + showHash(cyclicTree) + " {\n" +
                        "  simple={value=\"value\"},\n" +
                        "  self={value=DataTree.Mut" + showHash(cyclicTree) + " (circular)},\n" +
                        "}",
                output
        );
    }
}
