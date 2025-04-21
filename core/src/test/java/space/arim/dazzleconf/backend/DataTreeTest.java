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
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DataTreeMut;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataTreeTest {

    @Test
    public void populateEntries() {
        // Sample values
        DataTreeMut dataTreeValue = new DataTreeMut();
        dataTreeValue.set("hello", new DataTree.Entry("goodbye"));
        dataTreeValue.set(1, new DataTree.Entry("key is not a string"));

        Object[] validValues = new Object[] {
                3, (byte) 4, Long.MAX_VALUE, Double.MIN_VALUE, (float) 42.7,
                false, 'c', "string",
                List.of(4, true, "list of mixed types"),
                dataTreeValue
        };
        // Verify usage
        for (Object validValue : validValues) {
            assertEquals(validValue, new DataTree.Entry(validValue).getValue());
            assertEquals(validValue, new DataTree.Entry(validValue)
                    .withComments(CommentLocation.ABOVE, List.of()).getValue());
            assertEquals(validValue, new DataTree.Entry(validValue).withLineNumber(14).getValue());
        }
        // Failed types
        assertThrows(RuntimeException.class, () -> new DataTree.Entry(null));
        assertThrows(IllegalArgumentException.class, () -> new DataTree.Entry(new Object()));
        assertThrows(IllegalArgumentException.class, () -> new DataTree.Entry(List.of("hi there", new Object())));
    }

    @Test
    public void iterateInOrder() {
        DataTreeMut dataTreeMut = new DataTreeMut();
        dataTreeMut.set("hello", new DataTree.Entry("goodbye"));
        dataTreeMut.set("also", new DataTree.Entry("yes"));
        dataTreeMut.set("zed", new DataTree.Entry(true));

        List<Object> orderedKeys = new ArrayList<>();
        List<Object> orderedValues = new ArrayList<>();
        dataTreeMut.forEach((k, v) -> {
            orderedKeys.add(k);
            orderedValues.add(v.getValue());
        });
        assertEquals(orderedKeys, List.of("hello", "also", "zed"));
        assertEquals(orderedValues, List.of("goodbye", "yes", true));

        assertEquals(orderedKeys, new ArrayList<>(dataTreeMut.keySetView()));
    }

    @Test
    public void commentsAndLineNumbers() {
        List<String> comments = List.of("no comment", "second line");
        List<String> comments2 = List.of("other location", "second line", "third line");
        DataTree.Entry entry = new DataTree.Entry("hi")
                .withComments(CommentLocation.ABOVE, comments)
                .withComments(CommentLocation.INLINE, comments2);
        assertEquals(comments, entry.getComments(CommentLocation.ABOVE));
        assertEquals(comments2, entry.getComments(CommentLocation.INLINE));
        assertEquals(List.of(), entry.getComments(CommentLocation.BELOW));
        assertEquals(new DataTree.Entry("hi"), entry);
    }

    @Test
    public void lineNumber()  {
        DataTree.Entry entry = new DataTree.Entry("hi").withLineNumber(32);
        assertEquals(32, entry.getLineNumber());
        assertEquals(new DataTree.Entry("hi"), entry);
    }

    @Test
    public void equality() {
        DataTreeMut dataTreeMut = new DataTreeMut();
        dataTreeMut.set("hello", new DataTree.Entry("goodbye"));
        dataTreeMut.set("also", new DataTree.Entry("yes"));
        dataTreeMut.set("zed", new DataTree.Entry(true));

        assertEquals((DataTree) dataTreeMut, dataTreeMut.makeImmut());
        assertEquals(dataTreeMut, dataTreeMut.makeImmut().makeMut());

        EqualsVerifier.forClass(DataTree.class).verify();
    }
}
