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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.ReloadShell;
import space.arim.dazzleconf2.engine.Comments;
import space.arim.dazzleconf2.engine.liaison.StringDefault;
import space.arim.dazzleconf2.reflect.*;

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
import static space.arim.dazzleconf.reflect.Utilities.assertEqualsBothWays;
import static space.arim.dazzleconf.reflect.Utilities.assertNotEqualsBothWays;

public abstract class InstantiatorTest {

    private final Instantiator instantiator;

    protected InstantiatorTest(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    public interface EmptyInterface {

    }

    @Test
    public void generateEmptyInterface() {
        Object generated = instantiator.generate(new Class[] {EmptyInterface.class}, new MethodYield());
        assertNotNull(generated);
        assertInstanceOf(EmptyInterface.class, generated);
        assertTrue(instantiator.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {EmptyInterface.class}, new MethodYield())
        );
    }

    @Test
    public void generateShellEmptyInterface() {
        ReloadShell<EmptyInterface> reloadShell = instantiator.generateShell(EmptyInterface.class);
        assertNull(reloadShell.getCurrentDelegate());
        EmptyInterface shell1 = reloadShell.getShell();
        assertNotNull(shell1);
        assertEqualsBothWays(shell1, shell1);
        assertTrue(instantiator.hasProduced(shell1));

        EmptyInterface delegate = new EmptyInterface() {};
        assertNotEqualsBothWays(delegate, shell1);
        reloadShell.setCurrentDelegate(delegate);
        assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
        assertNotEqualsBothWays(delegate, shell1);
        assertFalse(instantiator.hasProduced(delegate));

        EmptyInterface shell2 = reloadShell.getShell();
        assertSame(shell1, shell2, "Shell stays constant");

        ReloadShell<EmptyInterface> secondShell = instantiator.generateShell(EmptyInterface.class);
        assertNotEqualsBothWays(reloadShell, secondShell);
        assertNotEqualsBothWays(shell1, secondShell.getShell());
        secondShell.setCurrentDelegate(delegate);
        assertNotEqualsBothWays(shell1, secondShell.getCurrentDelegate());
        assertEqualsBothWays(shell1, secondShell.getShell());
    }

    @Test
    public void generateEmptyEmptyInterface() {
        EmptyInterface generated = instantiator.generateEmpty(EmptyInterface.class);
        assertNotNull(generated);
        assertInstanceOf(EmptyInterface.class, generated);
        assertTrue(instantiator.hasProduced(generated));

        assertNotEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {EmptyInterface.class}, new MethodYield())
        );
        assertEqualsBothWays(generated, instantiator.generateEmpty(EmptyInterface.class));
    }

    public interface SingleMethod {

        String value();
    }

    @Test
    public void generateSingleMethod() {
        String toReturn = "val1";
        MethodYield methodYield = new MethodYield();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        Object generated = instantiator.generate(new Class[] {SingleMethod.class}, methodYield);
        assertNotNull(generated);
        assertInstanceOf(SingleMethod.class, generated);
        assertEquals(toReturn, ((SingleMethod) generated).value());
        assertTrue(instantiator.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {SingleMethod.class}, methodYield)
        );
    }

    @Test
    public void generateSingleMethodWithAdditionalInterface() {
        Class<?> additionalInterface = RandomAccess.class;
        String toReturn = "val1";
        MethodYield methodYield = new MethodYield();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        Object generated = instantiator.generate(new Class[] {SingleMethod.class, additionalInterface}, methodYield);
        assertNotNull(generated);
        assertInstanceOf(SingleMethod.class, generated);
        assertInstanceOf(additionalInterface, generated);
        assertEquals(toReturn, ((SingleMethod) generated).value());
        assertTrue(instantiator.hasProduced(generated));

        assertNotEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {SingleMethod.class}, methodYield)
        );
    }

    @Test
    public void generateShellSingleMethod() {
        ReloadShell<SingleMethod> reloadShell = instantiator.generateShell(SingleMethod.class);
        assertNull(reloadShell.getCurrentDelegate());
        SingleMethod shell1 = reloadShell.getShell();
        assertNotNull(shell1);
        assertEqualsBothWays(shell1, shell1);
        assertTrue(instantiator.hasProduced(shell1));

        SingleMethod mainDelegate = () -> "delegated";
        assertNotEqualsBothWays(mainDelegate, shell1);
        reloadShell.setCurrentDelegate(mainDelegate);
        assertEqualsBothWays(mainDelegate, reloadShell.getCurrentDelegate());
        assertNotEqualsBothWays(mainDelegate, shell1);
        assertEquals("delegated", shell1.value());
        assertFalse(instantiator.hasProduced(mainDelegate));
        {
            IllegalStateException thrown = new IllegalStateException("Throw me");
            SingleMethod throwingDelegate = () -> { throw thrown; };
            reloadShell.setCurrentDelegate(throwingDelegate);
            try {
                shell1.value();
                fail("Expected IllegalStateException to be thrown");
            } catch (IllegalStateException caught) {
                assertSame(thrown, caught);
            } finally {
                // Set back for tests below
                reloadShell.setCurrentDelegate(mainDelegate);
            }
        }

        SingleMethod shell2 = reloadShell.getShell();
        assertSame(shell1, shell2, "Shell stays constant");

        ReloadShell<SingleMethod> secondShell = instantiator.generateShell(SingleMethod.class);
        assertNotEqualsBothWays(reloadShell, secondShell);
        assertNotEqualsBothWays(shell1, secondShell.getShell());
        secondShell.setCurrentDelegate(mainDelegate);
        assertNotEqualsBothWays(shell1, secondShell.getCurrentDelegate());
        assertEqualsBothWays(shell1, secondShell.getShell());
    }

    @Test
    public void generateEmptySingleMethod() {
        SingleMethod generated = instantiator.generateEmpty(SingleMethod.class);
        assertNotNull(generated);
        assertInstanceOf(SingleMethod.class, generated);
        assertTrue(instantiator.hasProduced(generated));

        MethodYield methodYield = new MethodYield();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), "junk"
        );
        assertNotEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {SingleMethod.class}, methodYield)
        );
        assertEqualsBothWays(generated, instantiator.generateEmpty(SingleMethod.class));
    }

    public interface InheritedMethod extends SingleMethod {

    }

    @Test
    public void generateInheritedMethod() {
        String toReturn = "val1";
        MethodYield methodYield = new MethodYield();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        Object generated = instantiator.generate(new Class[] {InheritedMethod.class, SingleMethod.class}, methodYield);
        assertNotNull(generated);
        assertInstanceOf(InheritedMethod.class, generated);
        assertEquals(toReturn, ((InheritedMethod) generated).value());
        assertTrue(instantiator.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {InheritedMethod.class, SingleMethod.class}, methodYield)
        );
    }

    @Test
    public void generateInheritedMethodWithAdditionalInterface() {
        Class<?> additionalInterface = RandomAccess.class;
        String toReturn = "val1";
        MethodYield methodYield = new MethodYield();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        Object generated = instantiator.generate(new Class[] {InheritedMethod.class, SingleMethod.class, additionalInterface}, methodYield);
        assertNotNull(generated);
        assertInstanceOf(InheritedMethod.class, generated);
        assertInstanceOf(additionalInterface, generated);
        assertEquals(toReturn, ((InheritedMethod) generated).value());
        assertTrue(instantiator.hasProduced(generated));

        assertNotEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {InheritedMethod.class, SingleMethod.class}, methodYield)
        );
    }

    @Test
    public void generateShellInheritedMethod() {
        ReloadShell<InheritedMethod> reloadShell = instantiator.generateShell(InheritedMethod.class);
        assertNull(reloadShell.getCurrentDelegate());
        InheritedMethod shell1 = reloadShell.getShell();
        assertNotNull(shell1);
        assertEqualsBothWays(shell1, shell1);
        assertTrue(instantiator.hasProduced(shell1));

        InheritedMethod delegate = () -> "delegated";
        assertNotEqualsBothWays(delegate, shell1);
        reloadShell.setCurrentDelegate(delegate);
        assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
        assertNotEqualsBothWays(delegate, shell1);
        assertEquals("delegated", shell1.value());
        assertFalse(instantiator.hasProduced(delegate));

        InheritedMethod shell2 = reloadShell.getShell();
        assertSame(shell1, shell2, "Shell stays constant");

        ReloadShell<InheritedMethod> secondShell = instantiator.generateShell(InheritedMethod.class);
        assertNotEqualsBothWays(reloadShell, secondShell);
        assertNotEqualsBothWays(shell1, secondShell.getShell());
        secondShell.setCurrentDelegate(delegate);
        assertNotEqualsBothWays(shell1, secondShell.getCurrentDelegate());
        assertEqualsBothWays(shell1, secondShell.getShell());
    }

    @Test
    public void generateEmptyInheritedMethod() {
        InheritedMethod generated = instantiator.generateEmpty(InheritedMethod.class);
        assertNotNull(generated);
        assertInstanceOf(InheritedMethod.class, generated);
        assertTrue(instantiator.hasProduced(generated));

        MethodYield methodYield = new MethodYield();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), "junk"
        );
        assertNotEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {SingleMethod.class, InheritedMethod.class}, methodYield)
        );
        assertEqualsBothWays(generated, instantiator.generateEmpty(InheritedMethod.class));
    }

    public interface PlusDefaultMethod extends InheritedMethod {

        default <T> T giveBack(Supplier<T> what) {
            return what.get();
        }

    }

    private void testGiveBack(PlusDefaultMethod plusDefaultMethod) {
        assertEquals("hello", plusDefaultMethod.giveBack(() -> "hello"));
        assertEquals(3, plusDefaultMethod.giveBack(() -> 3));
    }

    @Test
    public void generatePlusDefaultMethod() {
        String toReturn = "val1";
        MethodYield methodYield = new MethodYield();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        methodYield.addValue(
                PlusDefaultMethod.class, new MethodId(
                        "giveBack", new TypeToken<Object>() {}.getReifiedType(), new ReifiedType[] {new TypeToken<Supplier<Object>>() {}.getReifiedType()}, false
                ), new InvokeDefaultFunction());
        Object generated = instantiator.generate(new Class[] {PlusDefaultMethod.class, InheritedMethod.class, SingleMethod.class}, methodYield);
        assertNotNull(generated);
        assertInstanceOf(PlusDefaultMethod.class, generated);
        assertEquals(toReturn, ((PlusDefaultMethod) generated).value());
        testGiveBack((PlusDefaultMethod) generated);
        assertTrue(instantiator.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {PlusDefaultMethod.class, InheritedMethod.class, SingleMethod.class}, methodYield)
        );
    }

    @Test
    public void generatePlusDefaultMethodWithAdditionalInterface() {
        Class<?> additionalInterface = RandomAccess.class;
        String toReturn = "val1";
        MethodYield methodYield = new MethodYield();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        methodYield.addValue(
                PlusDefaultMethod.class, new MethodId(
                        "giveBack", new TypeToken<Object>() {}.getReifiedType(), new ReifiedType[] {new TypeToken<Supplier<Object>>() {}.getReifiedType()}, false
                ), new InvokeDefaultFunction());
        Object generated = instantiator.generate(new Class[] {PlusDefaultMethod.class, InheritedMethod.class, SingleMethod.class, additionalInterface}, methodYield);
        assertNotNull(generated);
        assertInstanceOf(PlusDefaultMethod.class, generated);
        assertInstanceOf(additionalInterface, generated);
        assertEquals(toReturn, ((PlusDefaultMethod) generated).value());
        testGiveBack((PlusDefaultMethod) generated);
        assertTrue(instantiator.hasProduced(generated));

        assertNotEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {PlusDefaultMethod.class, InheritedMethod.class, SingleMethod.class}, methodYield)
        );
    }

    @Test
    public void generateShellPlusDefaultMethod() {
        ReloadShell<PlusDefaultMethod> reloadShell = instantiator.generateShell(PlusDefaultMethod.class);
        assertNull(reloadShell.getCurrentDelegate());
        PlusDefaultMethod shell1 = reloadShell.getShell();
        assertNotNull(shell1);
        assertEqualsBothWays(shell1, shell1);
        assertTrue(instantiator.hasProduced(shell1));

        PlusDefaultMethod delegate = () -> "delegated";
        assertNotEqualsBothWays(delegate, shell1);
        reloadShell.setCurrentDelegate(delegate);
        assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
        assertNotEqualsBothWays(delegate, shell1);
        assertEquals("delegated", shell1.value());
        assertFalse(instantiator.hasProduced(delegate));

        PlusDefaultMethod shell2 = reloadShell.getShell();
        assertSame(shell1, shell2, "Shell stays constant");
        testGiveBack(shell1);

        ReloadShell<PlusDefaultMethod> secondShell = instantiator.generateShell(PlusDefaultMethod.class);
        assertNotEqualsBothWays(reloadShell, secondShell);
        assertNotEqualsBothWays(shell1, secondShell.getShell());
        secondShell.setCurrentDelegate(delegate);
        assertNotEqualsBothWays(shell1, secondShell.getCurrentDelegate());
        assertEqualsBothWays(shell1, secondShell.getShell());
    }

    @Test
    public void generateEmptyPlusDefaultMethod() {
        PlusDefaultMethod generated = instantiator.generateEmpty(PlusDefaultMethod.class);
        assertNotNull(generated);
        assertInstanceOf(PlusDefaultMethod.class, generated);
        testGiveBack(generated);
        assertTrue(instantiator.hasProduced(generated));

        MethodYield methodYield = new MethodYield();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), "junk"
        );
        methodYield.addValue(
                PlusDefaultMethod.class, new MethodId(
                        "giveBack", new TypeToken<Object>() {}.getReifiedType(), new ReifiedType[] {new TypeToken<Supplier<Object>>() {}.getReifiedType()}, false
                ), new InvokeDefaultFunction());
        assertNotEqualsBothWays(
                generated,
                instantiator.generate(new Class[] {SingleMethod.class, InheritedMethod.class, PlusDefaultMethod.class}, methodYield)
        );
        assertEqualsBothWays(generated, instantiator.generateEmpty(PlusDefaultMethod.class));
    }

    @Nested
    public class MirrorTest {

        private final MethodMirror methodMirror = instantiator.getMethodMirror();

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

        private final MethodId hello = new MethodId(
                "hello", ReifiedType.Annotated.unannotated(int.class), new ReifiedType[0], false
        );
        private final MethodId anotherCall = new MethodId(
                "anotherCall", ReifiedType.Annotated.unannotated(void.class), new ReifiedType[0], false
        );
        private final MethodId giveBack = new MethodId(
                "giveBack", new TypeToken<Object>() {}.getReifiedType(), new ReifiedType[] {new TypeToken<Supplier<Object>>() {}.getReifiedType()}, true
        );
        private final MethodId checkReflection = new MethodId(
                "checkReflection", ReifiedType.Annotated.unannotated(void.class), new ReifiedType[] {new TypeToken<Consumer<Object>>() {}.getReifiedType()}, true
        );
        private final MethodId giveArray = new MethodId(
                "giveArray", new TypeToken<String[]>() {}.getReifiedType(), new ReifiedType[0], true
        );

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

            MethodMirror.TypeWalker midWalker;
            {
                MethodMirror.TypeWalker[] superTypes = baseWalker.getSuperTypes();
                assertEquals(1, superTypes.length, "Return correct getSuperTypes()");
                midWalker = superTypes[0];
                assertNotNull(midWalker, "Return correct getSuperTypes()");
            }
            assertEquals(new TypeToken<MidLevel<String>>() {}.getReifiedType(), midWalker.getEnclosingType(), "Return correct getEnclosingType()");
            MethodId overiddenMethod = new MethodId("overidden", new TypeToken<List<String>>() {}.getReifiedType(), new ReifiedType[0], true);
            assertEquals(
                    Set.of(overiddenMethod),
                    midWalker.getViableMethods().collect(Collectors.toSet()),
                    "Returns correct getViableMethods()"
            );
            AnnotatedElement overiddenMethodAnnotations = midWalker.getAnnotations(overiddenMethod);
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
            assertEquals(
                    Set.of(
                            hello, new MethodId("overidden", overiddenMethod.returnType(), overiddenMethod.parameters(), false)
                    ),
                    parentWalker.getViableMethods().collect(Collectors.toSet()),
                    "Returns correct getViableMethods()"
            );
        }

        @Test
        public void makeInvoker() {
            AtomicBoolean anotherCallHook = new AtomicBoolean();
            AtomicInteger helloHook = new AtomicInteger();
            String[] presetArray = new String[]{"truly", "preset"};

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
                this.checkReflection.getMethod(Base.class).invoke(generatedBase, checkReflection);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new AssertionError("Calling problem", e);
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
