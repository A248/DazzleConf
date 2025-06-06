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
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataToStringTest {

    @Test
    public void simpleTrees() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("hi", new DataEntry(1));
        dataTree.set("newliner", new DataEntry("line:\n"));
        dataTree.set("subtree", new DataEntry(new DataTree.Immut()));
        assertEquals("""
                Mut{
                  hi=DataEntry{value=1},
                  newliner=DataEntry{value="line:\\n"},
                  subtree=DataEntry{value=Immut{}},
                }""", dataTree.toString());
    }

    @Test
    public void simpleLists() {
        List<DataEntry> entryList = new ArrayList<>();
        entryList.add(new DataEntry(false));
        entryList.add(new DataEntry("hello").withLineNumber(5));
        entryList.add(new DataEntry(List.of()));
        DataEntry topEntry = new DataEntry(entryList);
        assertEquals("""
                DataEntry{value=[
                  DataEntry{value=false},
                  DataEntry{value="hello", lineNumber=5},
                  DataEntry{value=[]},
                ]}""", topEntry.toString());
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
        dataTree.set("hi", new DataEntry(1));
        DataTree.Mut subTree = new DataTree.Mut();
        subTree.set("subkey", new DataEntry(true).withLineNumber(10));
        subTree.set("escape-value", new DataEntry("\u007F"));
        dataTree.set("subtree", new DataEntry(subTree));
        List<DataEntry> entryList = new ArrayList<>();
        entryList.add(new DataEntry(false));
        entryList.add(new DataEntry(List.of()));
        dataTree.set("list", new DataEntry(entryList));

        assertEquals("""
                Mut{
                  hi=DataEntry{value=1},
                  subtree=DataEntry{value=Mut{
                    subkey=DataEntry{value=true, lineNumber=10},
                    escape-value=DataEntry{value="\\u007f"},
                  }},
                  list=DataEntry{value=[
                    DataEntry{value=false},
                    DataEntry{value=[]},
                  ]},
                }""", dataTree.toString());
    }
}
