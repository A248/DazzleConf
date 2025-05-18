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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.engine.liaison.IntegerRange;
import space.arim.dazzleconf2.reflect.ReifiedType;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static space.arim.dazzleconf.reflect.Utilities.assertEqualsBothWays;
import static space.arim.dazzleconf.reflect.Utilities.assertNotEqualsBothWays;
import static space.arim.dazzleconf2.reflect.ReifiedType.Annotated.EMPTY_ARRAY;

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
                    new ReifiedType(stringClass, EMPTY_ARRAY), new ReifiedType(objectClass, EMPTY_ARRAY)
            })
    };

    @Test
    public void construct() {
        ReifiedType sample = new ReifiedType(type, params);
        assertEquals(type, sample.rawType());
        assertArrayEquals(params, sample.arguments());
        assertEquals(1, sample.argumentCount());
        assertEquals(params[0], sample.argumentAt(0));
    }

    @Test
    public void equality() {
        ReifiedType sample = new ReifiedType(type, params);
        assertEqualsBothWays(sample, sample);
        assertEqualsBothWays(new ReifiedType(type, params), sample);
        assertNotEqualsBothWays(new ReifiedType(type, EMPTY_ARRAY), sample);
        assertEqualsBothWays(
                ReifiedType.Annotated.unannotated(objectClass),
                sample.argumentAt(0).argumentAt(1)
        );
    }

    @Test
    public void toStringTest() {
        ReifiedType sample = new ReifiedType(type, params);
        assertTrue(sample.toString().contains("List"));
        assertTrue(sample.toString().contains("Map"));
    }

    @Nested
    public class AnnotatedTest {

        private final Class<Nullable> nullableClass = Nullable.class;
        private final AnnotatedElement annotatedNullable = ReifiedTypeTest.AnnotatedTest.class.getDeclaredMethod("annotatedNullable").getAnnotatedReturnType();
        private final Class<NonNull> nonNullClass = NonNull.class;
        private final AnnotatedElement annotatedNonNull = ReifiedTypeTest.AnnotatedTest.class.getDeclaredMethod("annotatedNonNull").getAnnotatedReturnType();

        @Nullable Object annotatedNullable() { return null; }
        @NonNull Object annotatedNonNull() { return new Object(); }
        public AnnotatedTest() throws NoSuchMethodException {}

        // @Nullable List<Map<String, @NonNull Object>>

        private final ReifiedType.Annotated[] annotatedParams = new ReifiedType.Annotated[] {
                new ReifiedType.Annotated(mapClass, new ReifiedType.Annotated[] {
                        ReifiedType.Annotated.unannotated(stringClass),
                        new ReifiedType.Annotated(objectClass, EMPTY_ARRAY, annotatedNonNull)
                }, ReifiedType.Annotated.unannotated())
        };

        @Test
        public void annotations(@Mock AnnotatedElement annotations) {
            ReifiedType.Annotated sample = new ReifiedType.Annotated(type, annotatedParams, annotatedNullable);
            assertTrue(sample.isAnnotationPresent(Nullable.class));
            assertFalse(sample.isAnnotationPresent(IntegerRange.class));
            assertFalse(sample.isAnnotationPresent(NonNull.class)); // Even though it's present within an argument
            assertNotNull(sample.getAnnotation(Nullable.class));
            assertNull(sample.getAnnotation(NonNull.class));
            assertArrayEquals(new Annotation[0], sample.getAnnotationsByType(NonNull.class));
            assertEquals(1, sample.getAnnotationsByType(Nullable.class).length);
            assertEquals(1, sample.getAnnotations().length);
            assertNotNull(sample.getDeclaredAnnotation(Nullable.class));
            assertNull(sample.getDeclaredAnnotation(NonNull.class));
            assertArrayEquals(new Annotation[0], sample.getDeclaredAnnotationsByType(NonNull.class));
            assertEquals(1, sample.getDeclaredAnnotationsByType(Nullable.class).length);
            assertEquals(1, sample.getDeclaredAnnotations().length);
        }

        @Test
        public void equality() {
            ReifiedType.Annotated onlyArgsAnnotated = new ReifiedType.Annotated(type, annotatedParams, ReifiedType.Annotated.unannotated());
            assertEqualsBothWays(onlyArgsAnnotated, onlyArgsAnnotated);
            assertEqualsBothWays(new ReifiedType.Annotated(type, annotatedParams, ReifiedType.Annotated.unannotated()), onlyArgsAnnotated);
            assertNotEqualsBothWays(new ReifiedType(type, params), onlyArgsAnnotated);

            ReifiedType.Annotated sample = new ReifiedType.Annotated(type, annotatedParams, annotatedNullable);
            assertEqualsBothWays(sample, sample);
            assertEqualsBothWays(new ReifiedType.Annotated(type, annotatedParams, annotatedNullable), sample);
            assertNotEqualsBothWays(new ReifiedType(type, params), sample);
            assertNotEqualsBothWays(onlyArgsAnnotated, sample);
        }

        @Test
        public void toStringTest() {
            ReifiedType sample = new ReifiedType.Annotated(type, annotatedParams, annotatedNullable);
            assertTrue(sample.toString().contains("Nullable"));
        }
    }
}
