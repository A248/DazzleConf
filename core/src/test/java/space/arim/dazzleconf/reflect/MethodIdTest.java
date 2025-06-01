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

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.reflect.MethodId;
import space.arim.dazzleconf2.reflect.ReifiedType;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class MethodIdTest {

    private final Method sampleOne = Sample.class.getDeclaredMethod("sampleOne", String.class);
    private final Method sampleTwo = Sample.class.getDeclaredMethod("sampleTwo");

    private final ReifiedType.Annotated voidType = ReifiedType.Annotated.unannotated(void.class);
    private final ReifiedType.Annotated booleanType = ReifiedType.Annotated.unannotated(boolean.class);
    private final ReifiedType.Annotated stringType = ReifiedType.Annotated.unannotated(String.class);

    public MethodIdTest() throws NoSuchMethodException {}

    interface Sample {

        void sampleOne(String param);

        default boolean sampleTwo() {
            return true;
        }
    }

    @Test
    public void construct() {
        MethodId sampleOneId = new MethodId("sampleOne", voidType, new ReifiedType[] {stringType}, false);
        assertEquals("sampleOne", sampleOneId.name());
        assertNull(sampleOneId.method());
        assertEquals(sampleOne, sampleOneId.getMethod(Sample.class));
        assertEquals(voidType, sampleOneId.returnType());
        assertArrayEquals(new ReifiedType[] {stringType}, sampleOneId.parameters());
        assertEquals(1, sampleOneId.parameterCount());
        assertEquals(stringType, sampleOneId.parameterAt(0));
        assertFalse(sampleOneId.isDefault());

        MethodId sampleTwoId = assertDoesNotThrow(() -> new MethodId("sampleTwo", booleanType, new ReifiedType[0], true));
        assertEquals("sampleTwo", sampleTwoId.name());
        assertNull(sampleTwoId.method());
        assertEquals(sampleTwo, sampleTwoId.getMethod(Sample.class));
        assertEquals(booleanType, sampleTwoId.returnType());
        assertArrayEquals(new ReifiedType[0], sampleTwoId.parameters());
        assertTrue(sampleTwoId.isDefault());
    }

    @Test
    public void constructNull() {
        assertThrows(NullPointerException.class, () -> new MethodId((String) null, stringType, new ReifiedType[] {stringType}, false));
        assertThrows(NullPointerException.class, () -> new MethodId("sampleOne", null, new ReifiedType[] {stringType}, false));
        assertThrows(NullPointerException.class, () -> new MethodId("sampleOne", stringType, null, false));
        assertThrows(NullPointerException.class, () -> new MethodId("sampleOne", stringType, new ReifiedType[] {null}, false));
    }

    @Test
    public void constructFromMethod() {
        MethodId sampleOneId = assertDoesNotThrow(() -> new MethodId(sampleOne, voidType, new ReifiedType[] {stringType}, false));
        assertEquals("sampleOne", sampleOneId.name());
        assertEquals(sampleOne, sampleOneId.method());
        assertSame(sampleOne, sampleOneId.getMethod(getClass()));

        assertThrows(IllegalArgumentException.class, () -> new MethodId(sampleOne, stringType, new ReifiedType[] {stringType}, false));
        assertThrows(IllegalArgumentException.class, () -> new MethodId(sampleOne, voidType, new ReifiedType[] {}, false));
        assertThrows(IllegalArgumentException.class, () -> new MethodId(sampleOne, voidType, new ReifiedType[] {stringType}, true));

        MethodId sampleTwoId = assertDoesNotThrow(() -> new MethodId(sampleTwo, booleanType, new ReifiedType[0], true));
        assertEquals("sampleTwo", sampleTwoId.name());
        assertEquals(sampleTwo, sampleTwoId.method());
        assertSame(sampleTwo, sampleTwoId.getMethod(getClass()));

        assertThrows(IllegalArgumentException.class, () -> new MethodId(sampleTwo, stringType, new ReifiedType[0], true));
        assertThrows(IllegalArgumentException.class, () -> new MethodId(sampleTwo, booleanType, new ReifiedType[] {stringType}, true));
        assertThrows(IllegalArgumentException.class, () -> new MethodId(sampleTwo, booleanType, new ReifiedType[0], false));
    }

    @Test
    public void toStringTest() {
        MethodId sampleOne = new MethodId(this.sampleOne, voidType, new ReifiedType[] {stringType}, false);
        String toString = sampleOne.toString();
        assertTrue(toString.contains("sampleOne"));
        assertTrue(toString.contains(String.class.getSimpleName()));

        MethodId sampleTwo = new MethodId(this.sampleTwo, booleanType, new ReifiedType[0], true);
        toString = sampleTwo.toString();
        assertTrue(toString.contains("default"));
        assertTrue(toString.contains("sampleTwo"));
    }
}
