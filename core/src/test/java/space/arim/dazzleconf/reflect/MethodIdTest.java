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
import static space.arim.dazzleconf.Utilities.assertEqualsBothWays;

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
        assertNull(sampleOneId.getOpaqueCache());
        assertEquals("sampleOne", sampleOneId.name());
        //assertEquals(sampleOne, sampleOneId.getMethod(Sample.class));
        assertEquals(voidType, sampleOneId.returnType());
        assertArrayEquals(new ReifiedType[] {stringType}, sampleOneId.parameters());
        assertEquals(1, sampleOneId.parameterCount());
        assertEquals(stringType, sampleOneId.parameterAt(0));
        assertFalse(sampleOneId.isDefault());

        MethodId sampleTwoId = assertDoesNotThrow(() -> new MethodId("sampleTwo", booleanType, new ReifiedType[0], true));
        assertNull(sampleTwoId.getOpaqueCache());
        assertEquals("sampleTwo", sampleTwoId.name());
        //assertEquals(sampleTwo, sampleTwoId.getMethod(Sample.class));
        assertEquals(booleanType, sampleTwoId.returnType());
        assertArrayEquals(new ReifiedType[0], sampleTwoId.parameters());
        assertTrue(sampleTwoId.isDefault());
    }

    @Test
    public void constructNull() {
        assertThrows(NullPointerException.class, () -> new MethodId(null, stringType, new ReifiedType[] {stringType}, false));
        assertThrows(NullPointerException.class, () -> new MethodId("sampleOne", null, new ReifiedType[] {stringType}, false));
        assertThrows(NullPointerException.class, () -> new MethodId("sampleOne", stringType, null, false));
        assertThrows(NullPointerException.class, () -> new MethodId("sampleOne", stringType, new ReifiedType[] {null}, false));
    }

    record Cache(Method method) implements MethodId.OpaqueCache {}

    @Test
    public void withOpaqueCache() {
        MethodId original = new MethodId("sampleOne", voidType, new ReifiedType[] {stringType}, false);
        MethodId withCache = original.withOpaqueCache(new Cache(sampleOne));
        assertNotNull(withCache.getOpaqueCache());
        assertSame(sampleOne, ((Cache) withCache.getOpaqueCache()).method);

        assertEquals("sampleOne", withCache.name(), "same details");
        assertEqualsBothWays(original, withCache);
        MethodId withCacheCleared = withCache.withOpaqueCache(null);
        assertNull(withCacheCleared.getOpaqueCache());
        assertEqualsBothWays(withCache, withCacheCleared);
        assertEqualsBothWays(original, withCacheCleared);
    }

    @Test
    public void toStringTest() {
        MethodId sampleOne = new MethodId("sampleOne", voidType, new ReifiedType[] {stringType}, false);
        String toString = sampleOne.toString();
        assertTrue(toString.contains("sampleOne"));
        assertTrue(toString.contains(String.class.getSimpleName()));

        MethodId sampleTwo = new MethodId("sampleTwo", booleanType, new ReifiedType[0], true);
        toString = sampleTwo.toString();
        assertTrue(toString.contains("default"));
        assertTrue(toString.contains("sampleTwo"));
    }
}
