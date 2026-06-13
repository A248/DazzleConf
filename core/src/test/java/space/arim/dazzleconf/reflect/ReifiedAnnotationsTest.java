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

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.*;
import static space.arim.dazzleconf.Utilities.assertEqualsBothWays;
import static space.arim.dazzleconf.Utilities.assertNotEqualsBothWays;
import static space.arim.dazzleconf.reflect.ReifiedAnnotations.computeFrom;

public class ReifiedAnnotationsTest {

    @Retention(RUNTIME)
    @interface Regular {}

    @Retention(RUNTIME)
    @Inherited
    @interface Inherit {}

    @Retention(RUNTIME)
    @interface RepeatCont {
        Repeat[] value();
    }

    @Retention(RUNTIME)
    @Repeatable(RepeatCont.class)
    @interface Repeat {}

    @Retention(RUNTIME)
    @Inherited
    @interface RepeatInheritCont {
        RepeatInherit[] value();
    }

    @Retention(RUNTIME)
    @Inherited
    @Repeatable(RepeatInheritCont.class)
    @interface RepeatInherit { }

    @Test
    public void noAnnotes() {
        class None {}
        assertEqualsBothWays(ReifiedAnnotations.empty(), computeFrom(None.class));
    }

    @Test
    public void simpleAnnote() {
        @Regular
        class One {}
        @Regular
        class Two {}
        ReifiedAnnotations annotations = computeFrom(One.class);
        assertEqualsBothWays(annotations, computeFrom(Two.class));
        assertTrue(annotations.hasAny(Regular.class));
        Regular annote = annotations.getOne(Regular.class);
        assertNotNull(annote);
        assertArrayEquals(new Regular[] {annote}, annotations.getAll(Regular.class));
    }

    @Test
    public void repeated() {
        @Repeat
        @Repeat
        class Curious {}
        @Repeat
        @Repeat
        class CuriousDouble {}
        @Repeat
        class OnlyOne {}
        @RepeatCont({@Repeat})
        class OnlyOneCont {}

        ReifiedAnnotations curious = computeFrom(Curious.class);
        ReifiedAnnotations onlyOne = computeFrom(OnlyOne.class);
        ReifiedAnnotations onlyOneCont = computeFrom(OnlyOneCont.class);

        assertEqualsBothWays(curious, computeFrom(CuriousDouble.class));
        assertEqualsBothWays(onlyOne, onlyOneCont);

        assertNotEqualsBothWays(curious, onlyOne);
        assertTrue(curious.hasAny(Repeat.class));
        assertTrue(onlyOne.hasAny(Repeat.class));
        assertTrue(onlyOneCont.hasAny(Repeat.class));
        assertThrows(IllegalStateException.class, () -> curious.getOne(Repeat.class));
        assertNotNull(onlyOne.getOne(Repeat.class));
        assertNotNull(onlyOneCont.getOne(Repeat.class));
        assertEqualsBothWays(2, curious.getAll(Repeat.class).length);
        assertEqualsBothWays(1, onlyOne.getAll(Repeat.class).length);
        assertEqualsBothWays(1, onlyOneCont.getAll(Repeat.class).length);
        // See what works with the container
        assertTrue(curious.hasAny(RepeatCont.class));
        assertTrue(onlyOne.hasAny(RepeatCont.class));
        assertNotNull(curious.getOne(RepeatCont.class));
        // This behavior is surprising but technically correct
        assertNull(onlyOne.getOne(RepeatCont.class));
        assertNotNull(onlyOneCont.getOne(RepeatCont.class));
    }

    @Test
    public void inherited() {
        @Inherit
        class Grandparent {}
        @Repeat
        @Regular
        class Parent extends Grandparent {}
        class Child extends Parent {}

        ReifiedAnnotations child = computeFrom(Child.class);
        ReifiedAnnotations grandparent = computeFrom(Grandparent.class);
        assertEqualsBothWays(child, grandparent);
        assertNotEqualsBothWays(child, computeFrom(Parent.class));
        assertTrue(child.hasAny(Inherit.class));
        assertNotNull(child.getOne(Inherit.class));
        assertEquals(1, child.getAll(Inherit.class).length);

        for (Class<?> check : new Class[] {Regular.class, Repeat.class, RepeatCont.class}) {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> checkAbsent = (Class<? extends Annotation>) check;
            assertFalse(child.hasAny(checkAbsent));
            assertNull(child.getOne(checkAbsent));
            assertEquals(0, child.getAll(checkAbsent).length);
        }
    }

    @Test
    public void repeatedInherited() {
        @RepeatInherit
        @RepeatInherit
        class CuriousParent {}
        class Curious extends CuriousParent {}
        @RepeatInherit
        class OnlyOnceParent {}
        class OnlyOnce extends OnlyOnceParent {}

        ReifiedAnnotations curious = computeFrom(Curious.class);
        ReifiedAnnotations onlyOne = computeFrom(OnlyOnce.class);
        assertNotEqualsBothWays(curious, onlyOne);
        assertTrue(curious.hasAny(RepeatInherit.class));
        assertTrue(onlyOne.hasAny(RepeatInherit.class));
        assertThrows(IllegalStateException.class, () -> curious.getOne(RepeatInherit.class));
        assertNotNull(onlyOne.getOne(RepeatInherit.class));
        assertEqualsBothWays(2, curious.getAll(RepeatInherit.class).length);
        assertEqualsBothWays(1, onlyOne.getAll(RepeatInherit.class).length);
        // See what works with the container
        assertTrue(curious.hasAny(RepeatInheritCont.class));
        assertTrue(onlyOne.hasAny(RepeatInheritCont.class));
        assertNotNull(curious.getOne(RepeatInheritCont.class));
        // This behavior is surprising but technically correct
        assertNull(onlyOne.getOne(RepeatInheritCont.class));
    }
}
