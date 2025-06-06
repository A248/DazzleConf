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

package space.arim.dazzleconf.reflect;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.reflect.MethodId;
import space.arim.dazzleconf2.reflect.MethodYield;
import space.arim.dazzleconf2.reflect.ReifiedType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MethodYieldTest {

    private final MethodId sample = new MethodId("sample", ReifiedType.Annotated.unannotated(String.class), new ReifiedType[0], false);

    public String sample() {
        return "sample";
    }

    @Test
    public void empty() {
        MethodYield methodYield = new MethodYield();
        assertFalse(methodYield.entries().iterator().hasNext());
    }

    @Test
    public void addEntry() {
        MethodYield methodYield = new MethodYield();
        methodYield.addEntry(MethodYieldTest.class, sample, "my value");
        MethodYield.Entry entry = methodYield.entries().iterator().next();
        assertEquals(MethodYieldTest.class, entry.implementable());
        assertEquals(sample,  entry.method());
        assertEquals("my value", entry.returnValue());
    }

    @Test
    public void clear() {
        MethodYield methodYield = new MethodYield();
        methodYield.addEntry(MethodYieldTest.class, sample, "my value");
        methodYield.clear();
        assertFalse(methodYield.entries().iterator().hasNext());
    }

    @Test
    public void copy() {
        MethodYield original = new MethodYield();
        original.addEntry(MethodYieldTest.class, sample, "s");
        MethodYield copy = original.copy();
        MethodYield.Entry entry = copy.entries().iterator().next();
        assertEquals(MethodYieldTest.class, entry.implementable());
        assertEquals(sample,  entry.method());
        assertEquals("s", entry.returnValue());
    }

    @Test
    public void copyCannotMutate() {
        MethodYield original = new MethodYield();
        original.addEntry(MethodYieldTest.class, sample, "s");
        MethodYield copy = original.copy();
        assertTrue(original.entries().iterator().hasNext());
        copy.clear();
        assertTrue(original.entries().iterator().hasNext());
    }

    @Test
    public void copyCannotBeMutated() {
        MethodYield original = new MethodYield();
        original.addEntry(MethodYieldTest.class, sample, "s");
        MethodYield copy = original.copy();
        assertTrue(copy.entries().iterator().hasNext());
        original.clear();
        assertTrue(copy.entries().iterator().hasNext());
    }

    @Test
    public void equality() {
        EqualsVerifier.forClass(MethodYield.class)
                .withPrefabValues(MethodId.class, sample, new MethodId("equality", ReifiedType.Annotated.unannotated(void.class), new ReifiedType[0], true))
                .withPrefabValues(ReifiedType[].class, ReifiedType.Annotated.EMPTY_ARRAY, new ReifiedType.Annotated[] {ReifiedType.Annotated.unannotated(void.class)})
                .verify();
    }

    @Test
    public void toStringTest() {
        MethodYield methodYield = new MethodYield();
        methodYield.addEntry(MethodYieldTest.class, sample, "myvalue");
        assertTrue(methodYield.toString().contains("myvalue"));
    }
}
