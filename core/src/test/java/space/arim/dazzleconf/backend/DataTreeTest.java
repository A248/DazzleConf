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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ImmutabilityGuard;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DataTreeTest {

    static class ImmutGuard extends ImmutabilityGuard<DataTree.Immut, Map<Object, DataEntry>> {

        protected ImmutGuard(DataTree.Immut value, DataTree.Immut...extra) {
            super(value, extra);
        }

        @Override
        protected Map<Object, DataEntry> takeSnapshot(DataTree.Immut value) {
            Map<Object, DataEntry> snapshot = new HashMap<>();
            value.forEach((key, entry) -> snapshot.put(key.toString(), entry));
            return Map.copyOf(snapshot);
        }
    }

    @Test
    public void populateEntries() {
        // Sample values
        DataTree.Mut dataTree = new DataTree.Mut();
        assertTrue(dataTree.isEmpty());
        assertEquals(0, dataTree.size());
        dataTree.set("hello", new DataEntry("goodbye"));
        dataTree.set(1, new DataEntry("key is not a string"));
        assertFalse(dataTree.isEmpty());
        assertEquals(2, dataTree.size());

        Object[] validValues = new Object[] {
                3, (byte) 4, Long.MAX_VALUE, Double.MIN_VALUE, (float) 42.7,
                false, 'c', "string",
                List.of(new DataEntry(4), new DataEntry(true), new DataEntry("list of mixed types")),
                dataTree
        };
        // Verify usage
        for (Object validValue : validValues) {
            assertEquals(validValue, new DataEntry(validValue).getValue());
            assertEquals(validValue, new DataEntry(validValue)
                    .withComments(CommentLocation.ABOVE, List.of()).getValue());
            assertEquals(validValue, new DataEntry(validValue).withLineNumber(14).getValue());
        }
        // Failed types
        assertThrows(RuntimeException.class, () -> new DataEntry(null));
        assertThrows(IllegalArgumentException.class, () -> new DataEntry(new Object()));
        assertThrows(IllegalArgumentException.class, () -> new DataEntry(List.of("hi there", new Object())));
    }

    @Test
    public void iterateInOrder() {
        DataTree.Mut dataTreeMut = new DataTree.Mut();
        dataTreeMut.set("hello", new DataEntry("goodbye"));
        dataTreeMut.set("also", new DataEntry("yes"));
        dataTreeMut.set("zed", new DataEntry(true));

        List<Object> orderedKeys = new ArrayList<>();
        List<Object> orderedValues = new ArrayList<>();
        dataTreeMut.forEach((k, v) -> {
            orderedKeys.add(k);
            orderedValues.add(v.getValue());
        });
        assertEquals(orderedKeys, List.of("hello", "also", "zed"));
        assertEquals(orderedValues, List.of("goodbye", "yes", true));

        // If you want getKeys() back, go back to commit 5ebd8571a5ef98e713c9dbbd1227e25f09fd0faf in version2-dev
        //assertEquals(orderedKeys, new ArrayList<>(dataTreeMut.getKeys()));
    }

    @Test
    public void commentsAndLineNumbers() {
        List<String> comments = List.of("no comment", "second line");
        List<String> comments2 = List.of("other location", "second line", "third line");
        DataEntry entry = new DataEntry("hi")
                .withComments(CommentLocation.ABOVE, comments)
                .withComments(CommentLocation.INLINE, comments2);
        assertEquals(comments, entry.getComments(CommentLocation.ABOVE));
        assertEquals(comments2, entry.getComments(CommentLocation.INLINE));
        assertEquals(List.of(), entry.getComments(CommentLocation.BELOW));
        assertEquals(new DataEntry("hi"), entry);
    }

    @Test
    public void lineNumber()  {
        DataEntry entry = new DataEntry("hi").withLineNumber(32);
        assertEquals(32, entry.getLineNumber());
        assertEquals(new DataEntry("hi"), entry);
    }

    @Test
    public void intoMutOnMut() {
        DataTree.Mut dataTreeMut = new DataTree.Mut();
        dataTreeMut.set("hello", new DataEntry("goodbye"));
        assertSame(dataTreeMut, dataTreeMut.intoMut());
    }

    @Test
    public void intoImmut() {
        DataTree.Mut dataTreeMut = new DataTree.Mut();
        dataTreeMut.set("hello", new DataEntry("goodbye"));
        dataTreeMut.set("also", new DataEntry("yes"));
        dataTreeMut.set("zed", new DataEntry(true));
        DataTree.Immut dataTreeImmut = dataTreeMut.intoImmut();

        assertEquals((DataTree) dataTreeImmut, dataTreeMut);
        assertSame(dataTreeImmut, dataTreeImmut.intoImmut());
    }

    @Test
    public void intoImmutMutRoundTrip() {
        DataTree.Mut dataTreeMut = new DataTree.Mut();
        dataTreeMut.set("hello", new DataEntry("goodbye"));
        dataTreeMut.set("also", new DataEntry("yes"));
        dataTreeMut.set("zed", new DataEntry(true));
        assertEquals(dataTreeMut, dataTreeMut.intoImmut().intoMut());

    }

    @Test
    public void intoMutOnImmutCannotMutateImmut() {
        DataTree.Mut dataTreeMut = new DataTree.Mut();
        dataTreeMut.set("hello", new DataEntry("goodbye"));

        DataTree.Immut snapshot = dataTreeMut.intoImmut();
        try (ImmutGuard guard = new ImmutGuard(snapshot)) {

            DataTree.Mut backToMut = snapshot.intoMut();
            backToMut.set("new key", new DataEntry("new value"));
            guard.check();
            assertNotEquals(backToMut, snapshot);

            DataTree.Mut backToMut2 = snapshot.intoMut();
            backToMut2.set("hello", null);
            guard.check();
            assertNotEquals(backToMut2, snapshot);

            DataTree.Mut backToMut3 = snapshot.intoMut();
            backToMut3.remove("hello");
            guard.check();
            assertNotEquals(backToMut3, snapshot);

            DataTree.Mut backToMut4 = snapshot.intoMut();
            backToMut4.clear();
            assertNotEquals(backToMut4, snapshot);

            assertEquals(backToMut2, backToMut3);
            assertEquals(backToMut3, backToMut4);
        }
    }

    @Test
    public void intoImmutOnMutCannotBeMutatedBySet() {
        DataTree.Mut dataTreeMut = new DataTree.Mut();
        dataTreeMut.set("hello", new DataEntry("goodbye"));
        dataTreeMut.set("also", new DataEntry("yes"));
        dataTreeMut.set("zed", new DataEntry(true));

        DataTree.Immut snapshot = dataTreeMut.intoImmut();
        try (ImmutGuard ignored = new ImmutGuard(snapshot)) {
            dataTreeMut.set("new key", new DataEntry("new value"));
        }
    }


    @Test
    public void intoImmutOnMutCannotBeMutatedBySetNull() {
        DataTree.Mut dataTreeMut = new DataTree.Mut();
        dataTreeMut.set("hello", new DataEntry("goodbye"));
        dataTreeMut.set("also", new DataEntry("yes"));
        dataTreeMut.set("zed", new DataEntry(true));

        DataTree.Immut snapshot = dataTreeMut.intoImmut();
        try (ImmutGuard ignored = new ImmutGuard(snapshot)) {
            dataTreeMut.set("hello", null);
        }
    }


    @Test
    public void intoImmutOnMutCannotBeMutatedByRemove() {
        DataTree.Mut dataTreeMut = new DataTree.Mut();
        dataTreeMut.set("hello", new DataEntry("goodbye"));
        dataTreeMut.set("also", new DataEntry("yes"));
        dataTreeMut.set("zed", new DataEntry(true));

        DataTree.Immut snapshot = dataTreeMut.intoImmut();
        try (ImmutGuard ignored = new ImmutGuard(snapshot)) {
            dataTreeMut.remove("hello");
        }
    }

    @Test
    public void intoImmutOnMutCannotBeMutatedByClear() {
        DataTree.Mut dataTreeMut = new DataTree.Mut();
        dataTreeMut.set("hello", new DataEntry("goodbye"));
        dataTreeMut.set("also", new DataEntry("yes"));
        dataTreeMut.set("zed", new DataEntry(true));

        DataTree.Immut snapshot = dataTreeMut.intoImmut();
        try (ImmutGuard ignored = new ImmutGuard(snapshot)) {
            dataTreeMut.clear();
        }
    }

    @Test
    public void equality() throws ClassNotFoundException {
        Class.forName(NonNull.class.getName());
        EqualsVerifier.simple().forClass(DataTree.class).verify();
    }

    @Test
    public void toStringTest() {
        assertTrue(new DataTree.Mut().toString().contains("Mut"));
        assertTrue(new DataTree.Immut().toString().contains("Immut"));
    }
}
