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

package space.arim.dazzleconf.reflect;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf.engine.liaison.IntegerRange;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static space.arim.dazzleconf.Utilities.assertEqualsBothWays;
import static space.arim.dazzleconf.Utilities.assertNotEqualsBothWays;
import static space.arim.dazzleconf.reflect.ReifiedType.EMPTY_ARRAY;
import static space.arim.dazzleconf.reflect.ReifiedType.rawUnannotated;

@ExtendWith(MockitoExtension.class)
public class ReifiedTypeTest {

    private final Class<?> listClass = List.class;
    private final Class<?> mapClass = Map.class;
    private final Class<String> stringClass = String.class;
    private final Class<Object> objectClass = Object.class;

    // List<Map<String, @NonNull Object>>

    private final Class<?> type = listClass;
    private final ReifiedType[] params = new ReifiedType[] {
            new ReifiedType(mapClass, new ReifiedType[] {
                    ReifiedType.rawUnannotated(stringClass), ReifiedType.rawUnannotated(objectClass)
            }, ReifiedAnnotations.empty())
    };

    @Test
    public void construct() {
        ReifiedType sample = new ReifiedType(type, params, ReifiedAnnotations.empty());
        assertEquals(type, sample.rawType());
        assertArrayEquals(params, sample.arguments());
        assertEquals(1, sample.argumentCount());
        assertEquals(params[0], sample.argumentAt(0));
    }

    @Test
    public void equality() {
        ReifiedType sample = new ReifiedType(type, params, ReifiedAnnotations.empty());
        assertEqualsBothWays(sample, sample);
        assertEqualsBothWays(new ReifiedType(type, params, ReifiedAnnotations.empty()), sample);
        assertNotEqualsBothWays(new ReifiedType(type, EMPTY_ARRAY, ReifiedAnnotations.empty()), sample);
        assertEqualsBothWays(
                ReifiedType.rawUnannotated(objectClass),
                sample.argumentAt(0).argumentAt(1)
        );
    }

    @Test
    public void toStringTest() {
        ReifiedType sample = new ReifiedType(type, params, ReifiedAnnotations.empty());
        assertTrue(sample.toString().contains("List"));
        assertTrue(sample.toString().contains("Map"));
    }

    @Nested
    public class AnnotatedTest {

        private final ReifiedAnnotations annotatedNullable = ReifiedAnnotations.computeFrom(ReifiedTypeTest.AnnotatedTest.class.getDeclaredMethod("annotatedNullable").getAnnotatedReturnType());
        private final ReifiedAnnotations annotatedNonNull = ReifiedAnnotations.computeFrom(ReifiedTypeTest.AnnotatedTest.class.getDeclaredMethod("annotatedNonNull").getAnnotatedReturnType());

        @Nullable Object annotatedNullable() { return null; }
        @NonNull Object annotatedNonNull() { return new Object(); }
        public AnnotatedTest() throws NoSuchMethodException {}

        // @Nullable List<Map<String, @NonNull Object>>

        private final ReifiedType[] annotatedParams = new ReifiedType[] {
                new ReifiedType(mapClass, new ReifiedType[] {
                        ReifiedType.rawUnannotated(stringClass),
                        new ReifiedType(objectClass, EMPTY_ARRAY, annotatedNonNull)
                }, ReifiedAnnotations.empty())
        };

        @Test
        public void annotations(@Mock AnnotatedElement annotations) {
            ReifiedType sample = new ReifiedType(type, annotatedParams, annotatedNullable);
            assertTrue(sample.annotations().hasAny(Nullable.class));
            assertFalse(sample.annotations().hasAny(IntegerRange.class));
            assertFalse(sample.annotations().hasAny(NonNull.class)); // Even though it's present within an argument
            assertNotNull(sample.annotations().getOne(Nullable.class));
            assertNull(sample.annotations().getOne(NonNull.class));
            assertArrayEquals(new Annotation[0], sample.annotations().getAll(NonNull.class));
            assertEquals(1, sample.annotations().getAll(Nullable.class).length);
        }

        @Test
        public void equality() {
            ReifiedType onlyArgsAnnotated = new ReifiedType(type, annotatedParams, ReifiedAnnotations.empty());
            assertEqualsBothWays(onlyArgsAnnotated, onlyArgsAnnotated);
            assertEqualsBothWays(new ReifiedType(type, annotatedParams, ReifiedAnnotations.empty()), onlyArgsAnnotated);
            assertNotEqualsBothWays(new ReifiedType(type, params, ReifiedAnnotations.empty()), onlyArgsAnnotated);

            ReifiedType sample = new ReifiedType(type, annotatedParams, annotatedNullable);
            assertEqualsBothWays(sample, sample);
            assertEqualsBothWays(new ReifiedType(type, annotatedParams, annotatedNullable), sample);
            assertNotEqualsBothWays(new ReifiedType(type, params, ReifiedAnnotations.empty()), sample);
            assertNotEqualsBothWays(onlyArgsAnnotated, sample);
        }

        @Test
        public void toStringTest() {
            ReifiedType sample = new ReifiedType(type, annotatedParams, annotatedNullable);
            assertTrue(sample.toString().contains("Nullable"));
        }
    }

    @Nested
    public class ConstructionTest {

        private static void goodCtor(ThrowingSupplier<ReifiedType> supplier) {
            ReifiedType reifiedType = assertDoesNotThrow(supplier);
            assertNotNull(reifiedType);
        }

        private static void badCtor(Executable supplier) {
            assertThrows(IllegalArgumentException.class, supplier);
        }

        @Test
        public void argumentNumberMatch() {
            goodCtor(() -> ReifiedType.of(Map.class, ReifiedAnnotations.empty(), rawUnannotated(String.class), rawUnannotated(Object.class)));
            badCtor(() -> ReifiedType.of(Map.class, ReifiedAnnotations.empty(), rawUnannotated(String.class)));
            goodCtor(() -> ReifiedType.of(Map.class, ReifiedAnnotations.empty()));
            assertEqualsBothWays(rawUnannotated(Map.class), ReifiedType.of(Map.class, ReifiedAnnotations.empty()));
        }

        static class WackyBounds<T1 extends RuntimeException, T2 extends T1, T3 extends CharSequence> { }

        @Test
        public void satisfyBounds() {
            goodCtor(() -> ReifiedType.of(WackyBounds.class, ReifiedAnnotations.empty(),
                    rawUnannotated(IllegalArgumentException.class), rawUnannotated(IllegalArgumentException.class), rawUnannotated(String.class)));
            // T1 must extend RuntimeException
            badCtor(() -> ReifiedType.of(WackyBounds.class, ReifiedAnnotations.empty(),
                    rawUnannotated(Exception.class), rawUnannotated(IllegalArgumentException.class), rawUnannotated(String.class)));
            goodCtor(() -> ReifiedType.of(WackyBounds.class, ReifiedAnnotations.empty(),
                    rawUnannotated(RuntimeException.class), rawUnannotated(IllegalArgumentException.class), rawUnannotated(String.class)));
            // T2 also has erased bound of RuntimeException (T1 extension requirement not enforced)
            badCtor(() -> ReifiedType.of(WackyBounds.class, ReifiedAnnotations.empty(),
                    rawUnannotated(IllegalArgumentException.class), rawUnannotated(Exception.class), rawUnannotated(String.class)));
            goodCtor(() -> ReifiedType.of(WackyBounds.class, ReifiedAnnotations.empty(),
                    rawUnannotated(IllegalArgumentException.class), rawUnannotated(RuntimeException.class), rawUnannotated(String.class)));
            // T3 interface bound erases to Object
            goodCtor(() -> ReifiedType.of(WackyBounds.class, ReifiedAnnotations.empty(),
                    rawUnannotated(IllegalArgumentException.class), rawUnannotated(IllegalArgumentException.class), rawUnannotated(CharSequence.class)));
            goodCtor(() -> ReifiedType.of(WackyBounds.class, ReifiedAnnotations.empty(),
                    rawUnannotated(IllegalArgumentException.class), rawUnannotated(IllegalArgumentException.class), rawUnannotated(Object.class)));

            // Disallow TOCTOU
            ReifiedType[] payload = new ReifiedType[] {rawUnannotated(IllegalArgumentException.class), rawUnannotated(IllegalArgumentException.class), rawUnannotated(String.class)};
            ReifiedType target = ReifiedType.of(WackyBounds.class, ReifiedAnnotations.empty(), payload);
            payload[0] = rawUnannotated(Object.class);
            assertEquals(IllegalArgumentException.class, target.argumentAt(0).rawType());
        }
    }
}
