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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.framework.qual.RequiresQualifier;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import space.arim.dazzleconf.backend.mutmodel.ActionBag;
import space.arim.dazzleconf.backend.mutmodel.GenerateAction;
import space.arim.dazzleconf.backend.mutmodel.ObserveAction;
import space.arim.dazzleconf.backend.mutmodel.ProduceAction;
import space.arim.dazzleconf.backend.mutmodel.SemiExhaustiveMutabilityTesting;
import space.arim.dazzleconf.engine.Comments;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReifiedAnnotationsModificationsIT extends SemiExhaustiveMutabilityTesting<ReifiedAnnotations, AnnotatedElement> {

    static final Method TEST_ALL_METHOD;
    private static final ActionBag<ReifiedAnnotations, AnnotatedElement> ACTION_BAG;

    static {
        List<Class<? extends Annotation>> targetAnnotationTypes = List.of(
                TestFactory.class, SideEffectFree.class, Comments.class, API.class, RequiresCalledMethods.class, RequiresNonNull.class
        );
        Set<Class<? extends Annotation>> containerAnnotationTypes = targetAnnotationTypes
                .stream()
                .map(clazz -> clazz.getAnnotation(Repeatable.class))
                .filter(Objects::nonNull)
                .map(Repeatable::value)
                .collect(Collectors.toSet());
        List<Class<? extends Annotation>> nonRepeatableAnnotationTypes = targetAnnotationTypes
                .stream()
                .filter(clazz -> clazz.getAnnotation(Repeatable.class) == null)
                .toList();
        Method testAllMethod;
        try {
            testAllMethod = ReifiedAnnotationsModificationsIT.class.getDeclaredMethod("testAll");
        } catch (NoSuchMethodException ex) {
            throw new ExceptionInInitializerError(ex);
        }
        TEST_ALL_METHOD = testAllMethod;
        Comments[] commentsOnTestWithTree = testAllMethod.getAnnotationsByType(Comments.class);
        RequiresCalledMethods[] requiresCmOnTestWithTree = testAllMethod.getAnnotationsByType(RequiresCalledMethods.class);
        TestFactory testFactory = testAllMethod.getAnnotation(TestFactory.class);
        SideEffectFree sideEffectFree = testAllMethod.getAnnotation(SideEffectFree.class);
        ACTION_BAG = new ActionBag.Builder<ReifiedAnnotations, AnnotatedElement>()
                .generate(
                        new GenerateAction<>(
                                "computeFrom", List.of(
                                testAllMethod, ReifiedAnnotationsModificationsIT.class
                        ),
                                ReifiedAnnotations::computeFrom,
                                Function.identity(),
                                false
                        )
                )
                .observe(
                        ObserveAction.aggregate("containersToValues.length", (annotations, element) -> {
                            // Count the number of distinct containers
                            Set<Class<?>> containers = new HashSet<>();
                            Annotation[] expectedAnnotes = element.getAnnotations();
                            for (Annotation expected : expectedAnnotes) {
                                Repeatable repeatable = expected.annotationType().getAnnotation(Repeatable.class);
                                if (repeatable == null) {
                                    containers.add(expected.annotationType());
                                } else {
                                    containers.add(repeatable.value());
                                }
                            }
                            assertEquals(containers.size(), annotations.containersToValues.length,
                                    () -> "Expected " + Arrays.toString(expectedAnnotes) + " but got " + Arrays.toString(annotations.containersToValues) + " for expected " + element);
                        }),
                        new ObserveAction<>("hasAny", targetAnnotationTypes, (annotations, element, clazz) -> {
                            assertEquals(
                                    clazz.getAnnotation(Repeatable.class) != null ?
                                            element.getAnnotationsByType(clazz).length != 0 :
                                            element.getAnnotation(clazz) != null,
                                    annotations.hasAny(clazz)
                            );
                        }),
                        new ObserveAction<>("getOne", nonRepeatableAnnotationTypes, (annotations, element, clazz) -> {
                            assertEquals(element.getAnnotation(clazz), annotations.getOne(clazz));
                        }),
                        new ObserveAction<>("getAll", targetAnnotationTypes, (annotations, element, clazz) -> {
                            assertArrayEquals(element.getAnnotationsByType(clazz), annotations.getAll(clazz));
                        })
                )
                .produce(
                        new ProduceAction<>(
                                "setOne",
                                List.of(
                                        new SingleAnnote<>(Comments.class, commentsOnTestWithTree[0]),
                                        new SingleAnnote<>(RequiresCalledMethods.class, requiresCmOnTestWithTree[0]),
                                        new SingleAnnote<>(TestFactory.class, testFactory),
                                        new SingleAnnote<>(SideEffectFree.class, sideEffectFree)
                                ),
                                (annotations, arg) -> arg.setOne(annotations),
                                (element, arg) -> arg.setOne(element),
                                false
                        ),
                        new ProduceAction<>(
                                "setAll",
                                List.of(
                                        new MultiAnnote<>(Comments.class, commentsOnTestWithTree),
                                        new MultiAnnote<>(RequiresCalledMethods.class, requiresCmOnTestWithTree)
                                ),
                                (annotations, arg) -> arg.setAll(annotations),
                                (element, arg) -> arg.setAll(element),
                                false
                        ),
                        new ProduceAction<>(
                                "removeIf",
                                List.of(
                                        new RemoveAnnote<>(Comments.class, (c) -> true),
                                        new RemoveAnnote<>(SideEffectFree.class, (c) -> true),
                                        new RemoveAnnote<>(Comments.class, (c) -> {
                                            for (String v : c.value()) {
                                                if (v.equals("hello")) {
                                                    return true;
                                                }
                                            }
                                            return false;
                                        })
                                ),
                                (annotations, arg) -> arg.removeIf(annotations),
                                (element, arg) -> arg.removeIf(element),
                                false
                        )
                )
                .build();
    }

    public ReifiedAnnotationsModificationsIT() {
        super(137114454687073150L, ACTION_BAG);
    }

    @TestFactory
    @SideEffectFree
    @Comments("hello")
    @Comments("some more comments")
    @API(status = API.Status.EXPERIMENTAL)
    @RequiresCalledMethods(value = {"hello"}, methods = {"method"})
    @RequiresCalledMethods(value = {"more"}, methods = {"methods", "zzz"})
    @RequiresQualifier.List({})
    @RequiresNonNull("self")
    public Stream<DynamicTest> testAll() {
        return testAll(4, 6);
    }

    static @NonNull Annotation @Nullable [] getContainedValue(Annotation annotation) {
        if (annotation instanceof Comments.Container container) {
            return container.value();
        }
        if (annotation instanceof RequiresCalledMethods.List container) {
            return container.value();
        }
        if (annotation instanceof RequiresQualifier.List container) {
            return container.value();
        }
        if (annotation instanceof RequiresNonNull.List container) {
            return container.value();
        }
        return null;
    }
}
