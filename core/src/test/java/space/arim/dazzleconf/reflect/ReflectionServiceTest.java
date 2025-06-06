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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.ReloadShell;
import space.arim.dazzleconf2.engine.Comments;
import space.arim.dazzleconf2.engine.liaison.StringDefault;
import space.arim.dazzleconf2.reflect.*;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static space.arim.dazzleconf.Utilities.assertEqualsBothWays;
import static space.arim.dazzleconf.Utilities.assertNotEqualsBothWays;

public abstract class ReflectionServiceTest {

    private final Instantiator instantiator;
    private final MethodMirror methodMirror;

    protected ReflectionServiceTest(ReflectionService reflectionService) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        instantiator = reflectionService.makeInstantiator(lookup);
        methodMirror = reflectionService.makeMethodMirror(lookup);
    }

    @Nested
    public class InstantiatorTest {

        public interface EmptyInterface { }

        @Test
        public void generateEmptyInterface() {
            EmptyInterface generated = instantiator.generate(EmptyInterface.class, new MethodYield());
            assertNotNull(generated);
            assertInstanceOf(EmptyInterface.class, generated);
            assertTrue(instantiator.hasProduced(generated));

            assertEqualsBothWays(
                    generated,
                    instantiator.generate(EmptyInterface.class, new MethodYield())
            );
        }

        @Test
        public void generateShellEmptyInterface() {
            // Make shell
            ReloadShell<EmptyInterface> reloadShell = instantiator.generateShell(EmptyInterface.class);
            assertEquals(reloadShell, reloadShell);
            assertNull(reloadShell.getCurrentDelegate());
            EmptyInterface shell = reloadShell.getShell();
            assertNotNull(shell);
            assertEqualsBothWays(shell, shell);
            assertTrue(instantiator.hasProduced(shell));

            // Test delegation
            EmptyInterface delegate = new EmptyInterface() {};
            assertNotEqualsBothWays(delegate, shell);
            reloadShell.setCurrentDelegate(delegate);
            assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
            assertNotEqualsBothWays(delegate, shell);
            assertTrue(shell.toString().contains(delegate.toString()));
            assertFalse(instantiator.hasProduced(delegate));

            EmptyInterface shell2 = reloadShell.getShell();
            assertSame(shell, shell2, "Shell stays constant");
            assertThrows(IllegalArgumentException.class, () -> reloadShell.setCurrentDelegate(shell));

            // Equality with another shell
            ReloadShell<EmptyInterface> secondShell = instantiator.generateShell(EmptyInterface.class);
            assertNotEqualsBothWays(reloadShell, secondShell);
            assertNotEqualsBothWays(shell, secondShell.getShell());
            secondShell.setCurrentDelegate(delegate);
            assertNotEqualsBothWays(shell, secondShell.getCurrentDelegate());
            assertEqualsBothWays(shell, secondShell.getShell());
            // Advanced cycle
            secondShell.setCurrentDelegate(shell);
            assertThrows(IllegalArgumentException.class, () -> reloadShell.setCurrentDelegate(secondShell.getShell()));
        }

        @Test
        public void generateEmptyEmptyInterface() {
            EmptyInterface generated = instantiator.generateEmpty(EmptyInterface.class);
            assertNotNull(generated);
            assertInstanceOf(EmptyInterface.class, generated);
            assertTrue(instantiator.hasProduced(generated));

            assertEqualsBothWays(generated, instantiator.generateEmpty(EmptyInterface.class));
            assertNotEqualsBothWays(generated, instantiator.generateEmpty(RandomAccess.class));

            assertEqualsBothWays(
                    generated,
                    instantiator.generate(EmptyInterface.class, new MethodYield())
            );
        }

        public interface SingleMethod {

            String value();
        }
        private final MethodId valueMethod = methodMirror.typeWalker(new TypeToken<SingleMethod>() {}.getReifiedType())
                .getViableMethods().findAny().orElseThrow();

        @Test
        public void generateSingleMethod() {
            String toReturn = "val1";
            MethodYield methodYield = new MethodYield();
            methodYield.addEntry(
                    SingleMethod.class, valueMethod, toReturn
            );
            SingleMethod generated = instantiator.generate(SingleMethod.class, methodYield.copy());
            assertNotNull(generated);
            assertInstanceOf(SingleMethod.class, generated);
            assertEquals(toReturn, generated.value());
            assertTrue(instantiator.hasProduced(generated));

            assertEqualsBothWays(
                    generated,
                    instantiator.generate(SingleMethod.class, methodYield)
            );
            assertNotEqualsBothWays(generated, () -> toReturn);
            assertTrue(generated.toString().contains(toReturn));

            MethodYield otherMethodYield = new MethodYield();
            otherMethodYield.addEntry(
                    SingleMethod.class, valueMethod, "junk"
            );
            assertNotEqualsBothWays(
                    generated,
                    instantiator.generate(SingleMethod.class, otherMethodYield)
            );
        }

        @Test
        public void generateShellSingleMethod() {
            // Make shell
            ReloadShell<SingleMethod> reloadShell = instantiator.generateShell(SingleMethod.class);
            assertEquals(reloadShell, reloadShell);
            assertNull(reloadShell.getCurrentDelegate());
            SingleMethod shell = reloadShell.getShell();
            assertNotNull(shell);
            assertEqualsBothWays(shell, shell);
            assertTrue(instantiator.hasProduced(shell));

            // Test delegation
            String toReturn = "delegated";
            SingleMethod mainDelegate = () -> toReturn;
            assertNotEqualsBothWays(mainDelegate, shell);
            reloadShell.setCurrentDelegate(mainDelegate);
            assertEqualsBothWays(mainDelegate, reloadShell.getCurrentDelegate());
            assertNotEqualsBothWays(mainDelegate, shell);
            assertEquals("delegated", shell.value());
            assertTrue(shell.toString().contains(mainDelegate.toString()));
            assertFalse(instantiator.hasProduced(mainDelegate));
            assertThrows(IllegalArgumentException.class, () -> reloadShell.setCurrentDelegate(shell));
            SingleMethod shellCopy = reloadShell.getShell();
            assertSame(shell, shellCopy, "Shell stays constant");
            assertEquals(shell, shellCopy);

            // Test delegation (throwing)
            IllegalStateException thrown = new IllegalStateException("Throw me");
            SingleMethod throwingDelegate = () -> { throw thrown; };
            reloadShell.setCurrentDelegate(throwingDelegate);
            try {
                shell.value();
                fail("Expected IllegalStateException to be thrown");
            } catch (IllegalStateException caught) {
                assertSame(thrown, caught);
            }

            // Equality with another shell
            ReloadShell<SingleMethod> secondShell = instantiator.generateShell(SingleMethod.class);
            reloadShell.setCurrentDelegate(null);
            assertNotEquals(reloadShell, secondShell);
            assertEqualsBothWays(shell, secondShell.getShell());

            reloadShell.setCurrentDelegate(mainDelegate);
            assertNotEquals(reloadShell, secondShell);
            assertNotEqualsBothWays(shell, secondShell.getShell());
            secondShell.setCurrentDelegate(mainDelegate);
            assertNotEquals(reloadShell, secondShell);
            assertEqualsBothWays(shell, secondShell.getShell());

            // Advanced cycle
            secondShell.setCurrentDelegate(shell);
            assertThrows(IllegalArgumentException.class, () -> reloadShell.setCurrentDelegate(secondShell.getShell()));

            // Equality with regular generation
            MethodYield methodYield = new MethodYield();
            methodYield.addEntry(
                    SingleMethod.class, new MethodId(
                            "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                    ), toReturn
            );
            SingleMethod generated = instantiator.generate(SingleMethod.class, methodYield);
            reloadShell.setCurrentDelegate(null);
            assertNotEqualsBothWays(generated, shell);
            reloadShell.setCurrentDelegate(generated);
            assertEqualsBothWays(generated, shell);
        }

        @Test
        public void generateEmptySingleMethod() {
            SingleMethod generated = instantiator.generateEmpty(SingleMethod.class);
            assertNotNull(generated);
            assertInstanceOf(SingleMethod.class, generated);
            assertTrue(instantiator.hasProduced(generated));

            assertEqualsBothWays(generated, instantiator.generateEmpty(SingleMethod.class));
            assertNotEqualsBothWays(generated, instantiator.generateEmpty(EmptyInterface.class));

            MethodYield otherMethodYield = new MethodYield();
            otherMethodYield.addEntry(
                    SingleMethod.class, valueMethod, "junk"
            );
            assertNotEqualsBothWays(
                    generated,
                    instantiator.generate(SingleMethod.class, otherMethodYield)
            );
            ReloadShell<SingleMethod> reloadShell = instantiator.generateShell(SingleMethod.class);
            assertNotEqualsBothWays(generated, reloadShell.getCurrentDelegate());
            reloadShell.setCurrentDelegate(generated);
            assertEqualsBothWays(generated, reloadShell.getCurrentDelegate());
            reloadShell.setCurrentDelegate(instantiator.generateEmpty(SingleMethod.class));
            assertEqualsBothWays(generated, reloadShell.getCurrentDelegate());
        }

        public interface InheritedMethod extends SingleMethod {

        }

        @Test
        public void generateInheritedMethod() {
            String toReturn = "val1";
            MethodYield methodYield = new MethodYield();
            methodYield.addEntry(
                    SingleMethod.class, valueMethod, toReturn
            );
            InheritedMethod generated = instantiator.generate(InheritedMethod.class, methodYield.copy());
            assertNotNull(generated);
            assertInstanceOf(InheritedMethod.class, generated);
            assertEquals(toReturn, generated.value());
            assertTrue(instantiator.hasProduced(generated));

            assertEqualsBothWays(
                    generated,
                    instantiator.generate(InheritedMethod.class, methodYield.copy())
            );
            assertNotEqualsBothWays(
                    generated,
                    instantiator.generate(SingleMethod.class, methodYield)
            );
            MethodYield otherMethodYield = new MethodYield();
            otherMethodYield.addEntry(
                    SingleMethod.class, valueMethod, "junk"
            );
            assertNotEqualsBothWays(
                    generated,
                    instantiator.generate(InheritedMethod.class, otherMethodYield)
            );
        }

        @Test
        public void generateShellInheritedMethod() {
            // Make a shell
            ReloadShell<InheritedMethod> reloadShell = instantiator.generateShell(InheritedMethod.class);
            assertEquals(reloadShell, reloadShell);
            assertNull(reloadShell.getCurrentDelegate());
            InheritedMethod shell = reloadShell.getShell();
            assertNotNull(shell);
            assertEqualsBothWays(shell, shell);
            assertTrue(instantiator.hasProduced(shell));

            // Test delegation
            String toReturn = "delegated";
            InheritedMethod mainDelegate = () -> toReturn;
            assertNotEqualsBothWays(mainDelegate, shell);
            reloadShell.setCurrentDelegate(mainDelegate);
            assertEqualsBothWays(mainDelegate, reloadShell.getCurrentDelegate());
            assertNotEqualsBothWays(mainDelegate, shell);
            assertEquals("delegated", shell.value());
            assertTrue(shell.toString().contains(mainDelegate.toString()));
            assertFalse(instantiator.hasProduced(mainDelegate));
            assertThrows(IllegalArgumentException.class, () -> reloadShell.setCurrentDelegate(shell));
            InheritedMethod shellCopy = reloadShell.getShell();
            assertSame(shell, shellCopy, "Shell stays constant");
            assertEquals(shell, shellCopy);

            // Test delegation (throwing)
            IllegalStateException thrown = new IllegalStateException("Throw me");
            InheritedMethod throwingDelegate = () -> { throw thrown; };
            reloadShell.setCurrentDelegate(throwingDelegate);
            try {
                shell.value();
                fail("Expected IllegalStateException to be thrown");
            } catch (IllegalStateException caught) {
                assertSame(thrown, caught);
            }

            // Equality with another shell
            ReloadShell<InheritedMethod> secondShell = instantiator.generateShell(InheritedMethod.class);
            reloadShell.setCurrentDelegate(null);
            assertNotEquals(reloadShell, secondShell);
            assertEqualsBothWays(shell, secondShell.getShell());

            reloadShell.setCurrentDelegate(mainDelegate);
            assertNotEquals(reloadShell, secondShell);
            assertNotEqualsBothWays(shell, secondShell.getShell());
            secondShell.setCurrentDelegate(mainDelegate);
            assertNotEquals(reloadShell, secondShell);
            assertEqualsBothWays(shell, secondShell.getShell());

            // Advanced cycle
            secondShell.setCurrentDelegate(shell);
            assertThrows(IllegalArgumentException.class, () -> reloadShell.setCurrentDelegate(secondShell.getShell()));

            // Equality with shell of super type - not accepted even if the delegate is equal
            ReloadShell<SingleMethod> superShell = instantiator.generateShell(SingleMethod.class);
            reloadShell.setCurrentDelegate(null);
            assertNotEquals(reloadShell, superShell);
            assertNotEqualsBothWays(shell, superShell.getShell());
            reloadShell.setCurrentDelegate(mainDelegate);
            assertNotEquals(reloadShell, superShell);
            assertNotEqualsBothWays(shell, superShell.getShell());
            superShell.setCurrentDelegate(mainDelegate);
            assertNotEquals(reloadShell, superShell);
            assertNotEqualsBothWays(shell, superShell.getShell());

            // Equality with regular generation
            MethodYield methodYield = new MethodYield();
            methodYield.addEntry(
                    SingleMethod.class, valueMethod, toReturn
            );
            InheritedMethod generated = instantiator.generate(
                    InheritedMethod.class, methodYield
            );
            reloadShell.setCurrentDelegate(null);
            assertNotEqualsBothWays(generated, shell);
            reloadShell.setCurrentDelegate(mainDelegate);
            assertNotEqualsBothWays(generated, shell);
            reloadShell.setCurrentDelegate(generated);
            assertEqualsBothWays(generated, shell);
        }

        @Test
        public void generateEmptyInheritedMethod() {
            InheritedMethod generated = instantiator.generateEmpty(InheritedMethod.class);
            assertNotNull(generated);
            assertInstanceOf(InheritedMethod.class, generated);
            assertTrue(instantiator.hasProduced(generated));

            assertEqualsBothWays(generated, instantiator.generateEmpty(InheritedMethod.class));
            assertNotEqualsBothWays(generated, instantiator.generateEmpty(SingleMethod.class));

            MethodYield methodYield = new MethodYield();
            methodYield.addEntry(
                    SingleMethod.class, valueMethod, "junk"
            );
            assertNotEqualsBothWays(
                    generated,
                    instantiator.generate(InheritedMethod.class, methodYield)
            );
        }

        public interface PlusDefaultMethod extends InheritedMethod {

            default <T> T giveBack(Supplier<T> what) {
                return what.get();
            }

        }
        private final MethodId giveBackMethod = methodMirror.typeWalker(new TypeToken<PlusDefaultMethod>() {}.getReifiedType())
                .getViableMethods().findAny().orElseThrow();

        private void testGiveBack(PlusDefaultMethod plusDefaultMethod) {
            assertEquals("hello", plusDefaultMethod.giveBack(() -> "hello"));
            assertEquals(3, plusDefaultMethod.giveBack(() -> 3));
        }

        @Test
        public void generatePlusDefaultMethod() {
            String toReturn = "val1";
            MethodYield methodYield = new MethodYield();
            methodYield.addEntry(
                    SingleMethod.class, valueMethod, toReturn
            );
            methodYield.addEntry(
                    PlusDefaultMethod.class, giveBackMethod, new InvokeDefaultFunction()
            );
            PlusDefaultMethod generated = instantiator.generate(PlusDefaultMethod.class, methodYield.copy());
            assertNotNull(generated);
            assertInstanceOf(PlusDefaultMethod.class, generated);
            assertEquals(toReturn, generated.value());
            testGiveBack(generated);
            assertTrue(instantiator.hasProduced(generated));

            assertEqualsBothWays(
                    generated,
                    instantiator.generate(PlusDefaultMethod.class, methodYield.copy())
            );
            methodYield.clear();
            methodYield.addEntry(
                    SingleMethod.class, valueMethod, toReturn
            );
            assertNotEqualsBothWays(
                    generated,
                    instantiator.generate(InheritedMethod.class, methodYield)
            );
            MethodYield otherMethodYield = new MethodYield();
            otherMethodYield.addEntry(
                    SingleMethod.class, valueMethod, "junk"
            );
            otherMethodYield.addEntry(
                    PlusDefaultMethod.class, giveBackMethod, new InvokeDefaultFunction()
            );
            assertNotEqualsBothWays(
                    generated,
                    instantiator.generate(PlusDefaultMethod.class, otherMethodYield)
            );
        }

        @Test
        public void generateShellPlusDefaultMethod() {
            // Make a shell
            ReloadShell<PlusDefaultMethod> reloadShell = instantiator.generateShell(PlusDefaultMethod.class);
            assertEquals(reloadShell, reloadShell);
            assertNull(reloadShell.getCurrentDelegate());
            PlusDefaultMethod shell = reloadShell.getShell();
            assertNotNull(shell);
            assertEqualsBothWays(shell, shell);
            assertTrue(instantiator.hasProduced(shell));

            // Test delegation
            PlusDefaultMethod delegate = () -> "delegated";
            assertNotEqualsBothWays(delegate, shell);
            reloadShell.setCurrentDelegate(delegate);
            assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
            assertNotEqualsBothWays(delegate, shell);
            assertEquals("delegated", shell.value());
            assertTrue(shell.toString().contains(delegate.toString()));
            assertFalse(instantiator.hasProduced(delegate));
            assertThrows(IllegalArgumentException.class, () -> reloadShell.setCurrentDelegate(shell));
            PlusDefaultMethod shellCopy = reloadShell.getShell();
            assertSame(shell, shellCopy, "Shell stays constant");
            // generateShell() is not supposed to have special handling for default methods, but we test it anyway
            testGiveBack(shell);

            // Equality with another shell
            ReloadShell<PlusDefaultMethod> secondShell = instantiator.generateShell(PlusDefaultMethod.class);
            reloadShell.setCurrentDelegate(null);
            assertNotEquals(reloadShell, secondShell);
            assertEqualsBothWays(shell, secondShell.getShell());

            reloadShell.setCurrentDelegate(delegate);
            assertNotEquals(reloadShell, secondShell);
            assertNotEqualsBothWays(shell, secondShell.getShell());
            secondShell.setCurrentDelegate(delegate);
            assertNotEquals(reloadShell, secondShell);
            assertEqualsBothWays(shell, secondShell.getShell());

            // Advanced cycle
            secondShell.setCurrentDelegate(shell);
            assertThrows(IllegalArgumentException.class, () -> reloadShell.setCurrentDelegate(secondShell.getShell()));
        }

        @Test
        public void generateEmptyPlusDefaultMethod() {
            PlusDefaultMethod generated = instantiator.generateEmpty(PlusDefaultMethod.class);
            assertNotNull(generated);
            assertInstanceOf(PlusDefaultMethod.class, generated);
            testGiveBack(generated);
            assertTrue(instantiator.hasProduced(generated));

            MethodYield methodYield = new MethodYield();
            methodYield.addEntry(
                    SingleMethod.class, valueMethod, "junk"
            );
            methodYield.addEntry(
                    PlusDefaultMethod.class, giveBackMethod, new InvokeDefaultFunction()
            );
            assertNotEqualsBothWays(
                    generated,
                    instantiator.generate(PlusDefaultMethod.class, methodYield)
            );
            assertEqualsBothWays(generated, instantiator.generateEmpty(PlusDefaultMethod.class));
            assertNotEqualsBothWays(generated, instantiator.generateEmpty(SingleMethod.class));
        }
    }

    @Nested
    public class MirrorTest {

        public interface Parent<V> {

            int hello();

            List<V> overidden();
        }

        public interface MidLevel<V> extends Parent<V> {

            @Override
            @Comments("see me")
            default List<V> overidden() {
                return List.of();
            }
        }

        public interface Base extends MidLevel<String> {

            void anotherCall();

            default <V> V giveBack(Supplier<V> supplier) {
                return supplier.get();
            }

            default void checkReflection(Consumer<? super Boolean> checkReflection) {
                // Check if we are called by Java reflection
                boolean detectedCall = StackWalker
                        .getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE, StackWalker.Option.SHOW_REFLECT_FRAMES))
                        .walk(frameStream -> {
                            return frameStream
                                    // Ignore everything before the @Test method, which is called by JUnit
                                    .takeWhile(frame -> !frame.getMethodName().equals("makeInvoker"))
                                    // Look for the first usage of java.lang.reflect.Method
                                    .anyMatch(frame -> {
                                        return frame.getDeclaringClass().equals(Method.class);
                                    });
                        });
                checkReflection.accept(detectedCall);
            }

            default String[] giveArray() {
                return new String[0];
            }
        }

        private MethodId hello, overidden, anotherCall, giveBack, checkReflection, giveArray;
        private void setField(MethodId methodId) {
            switch (methodId.name()) {
                case "hello" -> hello = methodId;
                case "overidden" -> {
                    if (overidden == null) overidden = methodId;
                }
                case "anotherCall" -> anotherCall = methodId;
                case "giveBack" -> giveBack = methodId;
                case "checkReflection" -> checkReflection = methodId;
                case "giveArray" -> giveArray = methodId;
            }
        }

        @BeforeEach
        public void setupMethods() {
            MethodMirror.TypeWalker baseWalker = methodMirror.typeWalker(new TypeToken<Base>() {}.getReifiedType());
            baseWalker.getViableMethods().forEach(this::setField);
            MethodMirror.TypeWalker midLevelWalker = baseWalker.getSuperTypes()[0];
            midLevelWalker.getViableMethods().forEach(this::setField);
            MethodMirror.TypeWalker parentWalker = midLevelWalker.getSuperTypes()[0];
            parentWalker.getViableMethods().forEach(this::setField);
        }

        @Test
        public void typeWalker() {
            ReifiedType.Annotated baseType = new TypeToken<Base>() {}.getReifiedType();
            MethodMirror.TypeWalker baseWalker = methodMirror.typeWalker(baseType);
            assertEquals(baseType, baseWalker.getEnclosingType(), "Return correct getEnclosingType()");
            assertEquals(
                    Set.of(anotherCall, giveBack, checkReflection, giveArray),
                    baseWalker.getViableMethods().collect(Collectors.toSet()),
                    "Returns correct getViableMethods()"
            );
            Set<MethodId> expectedData = Set.of(
                    new MethodId(
                            "anotherCall", ReifiedType.Annotated.unannotated(void.class), new ReifiedType[0], false
                    ),
                    new MethodId(
                            "giveBack", new TypeToken<Object>() {}.getReifiedType(), new ReifiedType[] {new TypeToken<Supplier<Object>>() {}.getReifiedType()}, true
                    ),
                    new MethodId(
                            "checkReflection", ReifiedType.Annotated.unannotated(void.class), new ReifiedType[] {new TypeToken<Consumer<Object>>() {}.getReifiedType()}, true
                    ),
                    new MethodId(
                            "giveArray", new TypeToken<String[]>() {}.getReifiedType(), new ReifiedType[0], true
                    )
            );
            assertEquals(expectedData, Set.of(anotherCall, giveBack, checkReflection, giveArray));

            MethodMirror.TypeWalker midWalker;
            {
                MethodMirror.TypeWalker[] superTypes = baseWalker.getSuperTypes();
                assertEquals(1, superTypes.length, "Return correct getSuperTypes()");
                midWalker = superTypes[0];
                assertNotNull(midWalker, "Return correct getSuperTypes()");
            }
            assertEquals(new TypeToken<MidLevel<String>>() {}.getReifiedType(), midWalker.getEnclosingType(), "Return correct getEnclosingType()");
            Set<MethodId> viableMethods = midWalker.getViableMethods().collect(Collectors.toSet());
            assertEquals(
                    Set.of(overidden), viableMethods, "Returns correct getViableMethods()"
            );
            AnnotatedElement overiddenMethodAnnotations = midWalker.getAnnotations(overidden);
            assertFalse(overiddenMethodAnnotations.isAnnotationPresent(StringDefault.class), "Returns annotations present on method");
            Comments seeMeComment = overiddenMethodAnnotations.getAnnotation(Comments.class);
            assertNotNull(seeMeComment, "Returns annotations present on method");
            assertArrayEquals(new String[] {"see me"}, seeMeComment.value(), "Returns annotations present on method");
            MethodMirror.TypeWalker parentWalker;
            {
                MethodMirror.TypeWalker[] superTypes = midWalker.getSuperTypes();
                assertEquals(1, superTypes.length, "Return correct getSuperTypes()");
                parentWalker = superTypes[0];
                assertNotNull(parentWalker, "Return correct getSuperTypes()");
            }
            assertEquals(new TypeToken<Parent<String>>() {}.getReifiedType(), parentWalker.getEnclosingType(), "Return correct getEnclosingType()");
        }

        @Test
        public void makeInvoker() {
            AtomicBoolean anotherCallHook = new AtomicBoolean();
            AtomicInteger helloHook = new AtomicInteger();
            String[] presetArray = new String[] {"truly", "preset"};

            Base base = new Base() {
                @Override
                public void anotherCall() {
                    if (!anotherCallHook.compareAndSet(false, true)) {
                        throw new IllegalStateException();
                    }
                }

                @Override
                public int hello() {
                    return helloHook.getAndIncrement();
                }

                @Override
                public String[] giveArray() {
                    return presetArray;
                }
            };
            MethodMirror.Invoker invokeBase = methodMirror.makeInvoker(base, Base.class);
            assertDoesNotThrow(() -> invokeBase.invokeMethod(anotherCall),
                    "Calls method via MethodInvoker#invokeMethod and uses correct implementation");
            assertTrue(anotherCallHook.get(),
                    "Calls method via MethodInvoker#invokeMethod and uses correct implementation");
            assertThrows(InvocationTargetException.class, () -> invokeBase.invokeMethod(anotherCall),
                    "Catches exception when thrown by method via MethodInvoker#invokeMethod and wraps in InvocationTargetException");
            try {
                invokeBase.invokeMethod(anotherCall);
            } catch (InvocationTargetException caught) {
                assertInstanceOf(IllegalStateException.class, caught.getCause(),
                        "Catches exception when thrown by method via MethodInvoker#invokeMethod and wraps in InvocationTargetException");
            }
            Object invokeGiveBack = assertDoesNotThrow(() -> invokeBase.invokeMethod(giveBack, (Supplier<Boolean>) () -> true),
                    "Calls default method via MethodInvoker#invokeMethod, and uses default implementation");
            assertEquals(true, invokeGiveBack);
            Object invokeGiveArray = assertDoesNotThrow(() -> invokeBase.invokeMethod(giveArray),
                    "Calls overidden (originally default) method via MethodInvoker#invokeMethod, and uses overidden implementation");
            assertSame(presetArray, invokeGiveArray);

            MethodMirror.Invoker invokeParent = methodMirror.makeInvoker(base, Parent.class);
            assertEquals(0, assertDoesNotThrow(() -> invokeParent.invokeMethod(hello)),
                    "Calls method in parent class via MethodInvoker#invokeMethod using correct invoker");
            assertEquals(1, assertDoesNotThrow(() -> invokeParent.invokeMethod(hello)),
                    "Calls method in parent class via MethodInvoker#invokeMethod using correct invoker");
            assertEquals(2, helloHook.get());

            AtomicBoolean checkReflectionOutcome = new AtomicBoolean();
            Consumer<? super Boolean> checkReflection = checkReflectionOutcome::set;

            Base generatedBase = instantiator.generateEmpty(Base.class);
            MethodMirror.Invoker invokeGeneratedBase = methodMirror.makeInvoker(generatedBase, Base.class);

            // Efficiency test: Make sure that MethodMirror.Invoker bypasses standard reflection
            // First check if our testing apparatus is set up correctly
            generatedBase.checkReflection(checkReflection);
            assertFalse(checkReflectionOutcome.get(), "Test setup: Not called with reflection");
            try {
                Base.class.getDeclaredMethod("checkReflection", Consumer.class).invoke(generatedBase, checkReflection);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                throw new AssertionError("Calling problem", ex);
            }
            assertTrue(checkReflectionOutcome.get(), "Test setup: Called with reflection");
            // Then call this method using the invoker
            try {
                invokeGeneratedBase.invokeMethod(this.checkReflection, checkReflection);
            } catch (InvocationTargetException ex) {
                throw new AssertionError( "Calling problem", ex);
            }
            assertFalse(checkReflectionOutcome.get(), "Calling method via MethodInvoker#invokeMethod should not use standard reflection if called upon a proxy");
        }
    }
}
