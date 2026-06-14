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

@SuppressWarnings("EmptyClass") // note: Retention = SOURCE
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
        assertNotNull(curious.getOne(RepeatCont.class));
        // This behavior is surprising but technically correct
        assertFalse(onlyOne.hasAny(RepeatCont.class));
        assertNull(onlyOne.getOne(RepeatCont.class));
        assertTrue(onlyOneCont.hasAny(RepeatCont.class));
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
        assertNotNull(curious.getOne(RepeatInheritCont.class));
        // This behavior is surprising but technically correct
        assertFalse(onlyOne.hasAny(RepeatInheritCont.class));
        assertNull(onlyOne.getOne(RepeatInheritCont.class));
    }

    @Test
    public void setOne() {
        @RepeatCont({})
        class WithRepeatCont {}
        @Repeat
        @Repeat
        class WithRepeat {}
        RepeatCont repeatCont = WithRepeatCont.class.getAnnotation(RepeatCont.class);
        Repeat[] repeats = WithRepeat.class.getAnnotationsByType(Repeat.class);
        Repeat firstRepeat = repeats[0];

        ReifiedAnnotations empty = ReifiedAnnotations.empty();
        ReifiedAnnotations withRepeatCont = empty.setOne(RepeatCont.class, repeatCont);
        assertNotNull(withRepeatCont.getOne(RepeatCont.class));
        assertNull(withRepeatCont.getOne(Repeat.class));
        assertEquals(1, withRepeatCont.getAll(RepeatCont.class).length);
        assertEquals(0, withRepeatCont.getAll(Repeat.class).length);
        assertEqualsBothWays(ReifiedAnnotations.empty(), empty); // immutable
        assertSame(withRepeatCont, withRepeatCont.setOne(RepeatCont.class, repeatCont), "no-op set => same object");
        assertEqualsBothWays(computeFrom(WithRepeatCont.class), withRepeatCont); // equal to direct computation

        ReifiedAnnotations withFirstRepeat = empty.setOne(Repeat.class, firstRepeat);
        ReifiedAnnotations withFirstRepeatClone = withRepeatCont.setOne(Repeat.class, firstRepeat);
        assertEqualsBothWays(withFirstRepeat, withFirstRepeatClone);
        assertEqualsBothWays(computeFrom(WithRepeatCont.class), withRepeatCont); // immutable
        for (ReifiedAnnotations verify : new ReifiedAnnotations[] {withFirstRepeat, withFirstRepeatClone}) {
            assertTrue(verify.hasAny(Repeat.class));
            assertNotNull(verify.getOne(Repeat.class));
            assertEquals(1, verify.getAll(Repeat.class).length);
            assertFalse(verify.hasAny(RepeatCont.class));
            assertNull(verify.getOne(RepeatCont.class));
            assertEquals(0, verify.getAll(RepeatCont.class).length);
        }
    }

    @Test
    public void setAll() {
        @RepeatCont({})
        class WithRepeatCont {}
        @Repeat
        @Repeat
        class WithRepeat {}
        RepeatCont emptyRepeatCont = WithRepeatCont.class.getAnnotation(RepeatCont.class);
        Repeat[] repeats = WithRepeat.class.getAnnotationsByType(Repeat.class);
        RepeatCont fullRepeatCont = WithRepeat.class.getAnnotation(RepeatCont.class);

        ReifiedAnnotations empty = ReifiedAnnotations.empty();
        assertEqualsBothWays(ReifiedAnnotations.empty(), empty.setAll(Repeat.class)); // no-op setAll
        assertEqualsBothWays(ReifiedAnnotations.empty(), empty.setAll(RepeatCont.class)); // no-op setAll

        ReifiedAnnotations withEmptyRepeatCont = empty.setAll(RepeatCont.class, emptyRepeatCont);
        assertEqualsBothWays(ReifiedAnnotations.empty(), empty); // immutable
        assertEqualsBothWays(withEmptyRepeatCont, computeFrom(WithRepeatCont.class));
        ReifiedAnnotations withRepeatAlone = empty.setAll(Repeat.class, repeats);
        assertEqualsBothWays(ReifiedAnnotations.empty(), empty); // immutable
        ReifiedAnnotations withRepeatContained = computeFrom(WithRepeat.class);

        assertArrayEquals(new Repeat[0], withEmptyRepeatCont.getAll(Repeat.class));
        assertArrayEquals(repeats, withRepeatAlone.getAll(Repeat.class));
        assertArrayEquals(repeats, withRepeatContained.getAll(Repeat.class));
        assertEqualsBothWays(withRepeatAlone, withRepeatContained);

        // See what works with the container
        assertTrue(withEmptyRepeatCont.hasAny(RepeatCont.class));
        assertEquals(emptyRepeatCont, withEmptyRepeatCont.getOne(RepeatCont.class));
        assertArrayEquals(new RepeatCont[] {emptyRepeatCont}, withEmptyRepeatCont.getAll(RepeatCont.class));
        assertFalse(withRepeatAlone.hasAny(RepeatCont.class));
        assertNull(withRepeatAlone.getOne(RepeatCont.class));
        assertArrayEquals(new RepeatCont[0], withRepeatAlone.getAll(RepeatCont.class));
        assertTrue(withRepeatContained.hasAny(RepeatCont.class));
        assertEquals(fullRepeatCont, withRepeatContained.getOne(RepeatCont.class));
        assertArrayEquals(
                new RepeatCont[] {fullRepeatCont},
                withRepeatContained.getAll(RepeatCont.class)
        );
    }
}
