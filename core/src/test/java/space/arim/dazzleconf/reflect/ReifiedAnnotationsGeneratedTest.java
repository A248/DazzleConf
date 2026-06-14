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

import org.apiguardian.api.API;
import org.checkerframework.checker.calledmethods.qual.RequiresCalledMethods;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.framework.qual.RequiresQualifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import space.arim.dazzleconf.engine.Comments;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT.TEST_ALL_METHOD;

public class ReifiedAnnotationsGeneratedTest {

    @Test
    @SideEffectFree
    @Comments("hello")
    @Comments("some more comments")
    @API(status = API.Status.EXPERIMENTAL)
    @RequiresCalledMethods(value = {"hello"}, methods = {"method"})
    @RequiresCalledMethods(value = {"more"}, methods = {"methods", "zzz"})
    @RequiresQualifier.List({})
    @RequiresNonNull("self")
    public void test1() throws NoSuchMethodException {
        /*
org.opentest4j.AssertionFailedError:
Failed final observation containersToValues.length for subject 1 of action plan; 5 steps.
================================================================
Step 0: computeFrom; generate in 0; with argument 0 public java.util.stream.Stream space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT.testAll()
Step 1: getOne; observe 0; with argument 0 interface org.junit.jupiter.api.TestFactory
Step 2: removeIf; put in 1 from 0; with argument 2 RemoveAnnote[clazz=interface space.arim.dazzleconf.engine.Comments, removeIf=space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT$$Lambda/0x00000000591cd260@f627d13]
Step 3: containersToValues.length; observe 0
Step 4: getAll; observe 1; with argument 0 interface org.junit.jupiter.api.TestFactory
================================================================
	at space.arim.dazzleconf@2.0.0-M3-SNAPSHOT/space.arim.dazzleconf.backend.mutmodel.Plan.execute(Plan.java:115)
	at java.base/java.util.Optional.ifPresent(Optional.java:178)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1604)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1604)
Caused by: org.opentest4j.AssertionFailedError: Expected [@org.junit.jupiter.api.TestFactory(), @org.checkerframework.dataflow.qual.SideEffectFree(), @org.apiguardian.api.API(consumers={"*"}, since="", status=EXPERIMENTAL), @org.checkerframework.checker.calledmethods.qual.RequiresCalledMethods.List({@org.checkerframework.checker.calledmethods.qual.RequiresCalledMethods(value={"hello"}, methods={"method"}), @org.checkerframework.checker.calledmethods.qual.RequiresCalledMethods(value={"more"}, methods={"methods", "zzz"})}), @org.checkerframework.framework.qual.RequiresQualifier.List({}), @org.checkerframework.checker.nullness.qual.RequiresNonNull({"self"})] but got [Entry{containerAnnote=space.arim.dazzleconf.engine.Comments$Container, container=null, containedValue=[@space.arim.dazzleconf.engine.Comments(location=ABOVE, value={"some more comments"})]}, Entry{containerAnnote=org.checkerframework.framework.qual.RequiresQualifier$List, container=@org.checkerframework.framework.qual.RequiresQualifier.List({}), containedValue=null}, Entry{containerAnnote=org.junit.jupiter.api.TestFactory, container=@org.junit.jupiter.api.TestFactory(), containedValue=null}, Entry{containerAnnote=org.apiguardian.api.API, container=@org.apiguardian.api.API(consumers={"*"}, since="", status=EXPERIMENTAL), containedValue=null}, Entry{containerAnnote=org.checkerframework.checker.nullness.qual.RequiresNonNull$List, container=null, containedValue=[@org.checkerframework.checker.nullness.qual.RequiresNonNull({"self"})]}, Entry{containerAnnote=org.checkerframework.checker.calledmethods.qual.RequiresCalledMethods$List, container=@org.checkerframework.checker.calledmethods.qual.RequiresCalledMethods.List({@org.checkerframework.checker.calledmethods.qual.RequiresCalledMethods(value={"hello"}, methods={"method"}), @org.checkerframework.checker.calledmethods.qual.RequiresCalledMethods(value={"more"}, methods={"methods", "zzz"})}), containedValue=null}, Entry{containerAnnote=org.checkerframework.dataflow.qual.SideEffectFree, container=@org.checkerframework.dataflow.qual.SideEffectFree(), containedValue=null}] for expected space.arim.dazzleconf.reflect.RemoveAnnote$ElementOverride@6bf08014 ==> expected: <6> but was: <7>
         */
        AnnotatedElement comp0 = ReifiedAnnotationsGeneratedTest.class.getDeclaredMethod("test1");
        ReifiedAnnotations slot0 = ReifiedAnnotations.computeFrom(comp0);
        assertEquals(7, comp0.getAnnotations().length);
        assertEquals(7, slot0.containersToValues.length);
        assertEquals(comp0.getAnnotation(TestFactory.class), slot0.getOne(TestFactory.class));
        AnnotatedElement comp1 = new RemoveAnnote<>(Comments.class, (c) -> {
            for (String v : c.value()) {
                if (v.equals("hello")) {
                    return true;
                }
            }
            return false;
        }).removeIf(comp0);
        ReifiedAnnotations slot1 = slot0.removeIf(Comments.class, (c) -> {
            for (String v : c.value()) {
                if (v.equals("hello")) {
                    return true;
                }
            }
            return false;
        });
        assertEquals(7, comp0.getAnnotations().length);
        assertEquals(7, slot0.containersToValues.length);
        assertArrayEquals(comp1.getAnnotationsByType(TestFactory.class), slot1.getAll(TestFactory.class));
        assertEquals(7, comp1.getAnnotations().length);
        assertEquals(7, slot1.containersToValues.length);
    }

    @Test
    public void test2() {
        /*
[ERROR] space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT.testAll()[824] -- Time elapsed: 0.001 s <<< FAILURE!
org.opentest4j.AssertionFailedError:
Failed to execute step 5 of action plan; 6 steps.
================================================================
Step 0: computeFrom; generate in 0; with argument 1 class space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT
Step 1: getAll; observe 0; with argument 5 interface org.checkerframework.checker.nullness.qual.RequiresNonNull
Step 2: hasAny; observe 0; with argument 6 interface org.checkerframework.checker.nullness.qual.RequiresNonNull$List
Step 3: setOne; put in 1 from 0; with argument 0 SingleAnnote[clazz=interface space.arim.dazzleconf.engine.Comments, annote=@space.arim.dazzleconf.engine.Comments(location=ABOVE, value={"hello"})]
Step 4: removeIf; put in 2 from 1; with argument 2 RemoveAnnote[clazz=interface space.arim.dazzleconf.engine.Comments, removeIf=space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT$$Lambda/0x00000000bb1cd940@1130520d]
Step 5: getAll; observe 1; with argument 2 interface space.arim.dazzleconf.engine.Comments
================================================================
	at space.arim.dazzleconf@2.0.0-M3-SNAPSHOT/space.arim.dazzleconf.backend.mutmodel.Plan.execute(Plan.java:100)
	at java.base/java.util.Optional.ifPresent(Optional.java:178)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1604)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1604)
Caused by: org.opentest4j.AssertionFailedError: array lengths differ, expected: <0> but was: <1>
	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:151)
	at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
	at org.junit.jupiter.api.AssertArrayEquals.assertArraysHaveSameLength(AssertArrayEquals.java:428)
	at org.junit.jupiter.api.AssertArrayEquals.assertArrayEquals(AssertArrayEquals.java:335)
	at org.junit.jupiter.api.AssertArrayEquals.assertArrayEquals(AssertArrayEquals.java:159)
	at org.junit.jupiter.api.AssertArrayEquals.assertArrayEquals(AssertArrayEquals.java:155)
	at org.junit.jupiter.api.Assertions.assertArrayEquals(Assertions.java:1456)
	at space.arim.dazzleconf@2.0.0-M3-SNAPSHOT/space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT.lambda$static$6(ReifiedAnnotationsModificationsIT.java:125)
	at space.arim.dazzleconf@2.0.0-M3-SNAPSHOT/space.arim.dazzleconf.backend.mutmodel.ObserveStep.execute(ObserveStep.java:30)
	at space.arim.dazzleconf@2.0.0-M3-SNAPSHOT/space.arim.dazzleconf.backend.mutmodel.Plan.execute(Plan.java:96)
	... 3 more
         */
        AnnotatedElement comp0 = ReifiedAnnotationsModificationsIT.class;
        ReifiedAnnotations slot0 = ReifiedAnnotations.computeFrom(comp0);
        assertArrayEquals(comp0.getAnnotationsByType(RequiresNonNull.class), slot0.getAll(RequiresNonNull.class));
        assertEquals(comp0.isAnnotationPresent(RequiresNonNull.List.class), slot0.hasAny(RequiresNonNull.List.class));
        Comments setComment = TEST_ALL_METHOD.getAnnotationsByType(Comments.class)[0];
        AnnotatedElement comp1 = new SingleAnnote<>(Comments.class, setComment).setOne(comp0);
        ReifiedAnnotations slot1 = slot0.setOne(Comments.class, setComment);
        Comments[] comp1arr = comp1.getAnnotationsByType(Comments.class);
        Comments[] slot1arr = slot1.getAll(Comments.class);
        assertArrayEquals(comp1arr, slot1arr,
                () -> "expected " + Arrays.toString(comp1arr) + " but got " + Arrays.toString(slot1arr) + ". Comp1 bugged is " + comp1);
    }

    @Test
    public void test4() {
        /*
[ERROR]   ReifiedAnnotationsModificationsIT.testAll()[824] Failed final observation containersToValues.length for subject 2 of action plan; 6 steps.
================================================================
Step 0: computeFrom; generate in 0; with argument 1 class space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT
Step 1: getAll; observe 0; with argument 5 interface org.checkerframework.checker.nullness.qual.RequiresNonNull
Step 2: hasAny; observe 0; with argument 3 interface org.apiguardian.api.API
Step 3: setOne; put in 1 from 0; with argument 0 SingleAnnote[clazz=interface space.arim.dazzleconf.engine.Comments, annote=@space.arim.dazzleconf.engine.Comments(location=ABOVE, value={"hello"})]
Step 4: removeIf; put in 2 from 1; with argument 2 RemoveAnnote[clazz=interface space.arim.dazzleconf.engine.Comments, removeIf=space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT$$Lambda/0x00000000611cd940@1130520d]
Step 5: getAll; observe 1; with argument 2 interface space.arim.dazzleconf.engine.Comments
================================================================
         */
        AnnotatedElement comp0 = ReifiedAnnotationsModificationsIT.class;
        ReifiedAnnotations slot0 = ReifiedAnnotations.computeFrom(comp0);
        assertArrayEquals(comp0.getAnnotationsByType(RequiresNonNull.class), slot0.getAll(RequiresNonNull.class));
        assertEquals(comp0.isAnnotationPresent(API.class), slot0.hasAny(API.class));
        Comments setComment = TEST_ALL_METHOD.getAnnotationsByType(Comments.class)[0];
        AnnotatedElement comp1 = new SingleAnnote<>(Comments.class, setComment).setOne(comp0);
        ReifiedAnnotations slot1 = new SingleAnnote<>(Comments.class, setComment).setOne(slot0);
        AnnotatedElement comp2 = new RemoveAnnote<>(Comments.class, (c) -> true).removeIf(comp1);
        ReifiedAnnotations slot2 = new RemoveAnnote<>(Comments.class, (c) -> true).removeIf(slot1);
        assertArrayEquals(comp1.getAnnotationsByType(Comments.class), slot1.getAll(Comments.class));
        assertEquals(0, comp2.getAnnotations().length, () -> "" + comp2);
        assertEquals(0, slot2.containersToValues.length);
    }

    @Test
    public void test5() {
        /*
org.opentest4j.AssertionFailedError:
Failed final observation containersToValues.length for subject 2 of action plan; 6 steps.
================================================================
Step 0: computeFrom; generate in 0; with argument 1 class space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT
Step 1: getAll; observe 0; with argument 5 interface org.checkerframework.checker.nullness.qual.RequiresNonNull
Step 2: hasAny; observe 0; with argument 3 interface org.apiguardian.api.API
Step 3: setOne; put in 1 from 0; with argument 0 SingleAnnote[clazz=interface space.arim.dazzleconf.engine.Comments, annote=@space.arim.dazzleconf.engine.Comments(location=ABOVE, value={"hello"})]
Step 4: removeIf; put in 2 from 1; with argument 2 RemoveAnnote[clazz=interface space.arim.dazzleconf.engine.Comments, removeIf=space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT$$Lambda/0x00000000611cd940@1130520d]
Step 5: getAll; observe 1; with argument 2 interface space.arim.dazzleconf.engine.Comments
================================================================
	at space.arim.dazzleconf@2.0.0-M3-SNAPSHOT/space.arim.dazzleconf.backend.mutmodel.Plan.execute(Plan.java:115)
	at java.base/java.util.Optional.ifPresent(Optional.java:178)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1604)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1604)
Caused by: org.opentest4j.AssertionFailedError: Expected [SingleAnnoteOverride#withContainedValue(interface space.arim.dazzleconf.engine.Comments$Container, [])] but got [] for expected SetAnnoteOverride{inner=SetAnnoteOverride{inner=class space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT, additions=[SingleAnnoteOverride#withContainedValue(interface space.arim.dazzleconf.engine.Comments$Container, [@space.arim.dazzleconf.engine.Comments(location=ABOVE, value={"hello"})])], clazz=interface space.arim.dazzleconf.engine.Comments$Container}, additions=[SingleAnnoteOverride#withContainedValue(interface space.arim.dazzleconf.engine.Comments$Container, [])], clazz=interface space.arim.dazzleconf.engine.Comments$Container} ==> expected: <1> but was: <0>
	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:151)
	at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
	at org.junit.jupiter.api.AssertEquals.failNotEqual(AssertEquals.java:197)
	at org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:156)
	at org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:598)
	at space.arim.dazzleconf@2.0.0-M3-SNAPSHOT/space.arim.dazzleconf.reflect.ReifiedAnnotationsModificationsIT.lambda$static$2(ReifiedAnnotationsModificationsIT.java:108)
	at space.arim.dazzleconf@2.0.0-M3-SNAPSHOT/space.arim.dazzleconf.backend.mutmodel.ObserveAction.lambda$aggregate$0(ObserveAction.java:40)
	at space.arim.dazzleconf@2.0.0-M3-SNAPSHOT/space.arim.dazzleconf.backend.mutmodel.Plan.execute(Plan.java:110)
	... 3 more
         */
        AnnotatedElement comp0 = ReifiedAnnotationsModificationsIT.class;
        ReifiedAnnotations slot0 = ReifiedAnnotations.computeFrom(comp0);
        assertArrayEquals(comp0.getAnnotationsByType(RequiresNonNull.class), slot0.getAll(RequiresNonNull.class));
        assertEquals(comp0.isAnnotationPresent(API.class), slot0.hasAny(API.class));
        Comments setComment = TEST_ALL_METHOD.getAnnotationsByType(Comments.class)[0];
        AnnotatedElement comp1 = new SingleAnnote<>(Comments.class, setComment).setOne(comp0);
        ReifiedAnnotations slot1 = new SingleAnnote<>(Comments.class, setComment).setOne(slot0);
        AnnotatedElement comp2 = new RemoveAnnote<>(Comments.class, (c) -> true).removeIf(comp1);
        ReifiedAnnotations slot2 = new RemoveAnnote<>(Comments.class, (c) -> true).removeIf(slot1);
        assertArrayEquals(comp1.getAnnotationsByType(Comments.class), slot1.getAll(Comments.class));
        assertEquals(0, comp2.getAnnotations().length);
        assertEquals(0, slot2.containersToValues.length);
    }
}
