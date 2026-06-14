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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ReloadShell;
import space.arim.dazzleconf.engine.Comments;
import space.arim.dazzleconf.engine.liaison.StringDefault;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
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

    protected final ReflectionService service;
    private final ReflectionProvider<EmptyInterface> emptyInterfaceProvider;
    private final ReflectionProvider<SingleMethod> singleMethodProvider;
    private final ReflectionProvider<InheritedMethod> inheritedMethodProvider;
    private final ReflectionProvider<PlusDefaultMethod> plusDefaultMethodProvider;
    private final ReflectionProvider<ExhilirantMethod> exhilirantMethodProvider;
    protected final MethodHandles.Lookup lookup;

    protected ReflectionServiceTest(ReflectionService service) {
        this.service = service;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        emptyInterfaceProvider = service.newProvider(EmptyInterface.class, lookup);
        singleMethodProvider = service.newProvider(SingleMethod.class, lookup);
        inheritedMethodProvider = service.newProvider(InheritedMethod.class, lookup);
        plusDefaultMethodProvider = service.newProvider(PlusDefaultMethod.class, lookup);
        exhilirantMethodProvider = service.newProvider(ExhilirantMethod.class, lookup);
        this.lookup = lookup;
    }

    private @Nullable MethodId findMethodIn(ReflectionProvider.TypeWalker typeWalker, Class<?> superType, String name) {
        if (typeWalker.getEnclosingType().rawType().equals(superType)) {
            return typeWalker.getViableMethods()
                    .filter(methodId -> methodId.name().equals(name))
                    .findAny()
                    .orElseThrow();
        }
        for (ReflectionProvider.TypeWalker parent : typeWalker.getSuperTypes()) {
            MethodId inParent = findMethodIn(parent, superType, name);
            if (inParent != null) {
                return inParent;
            }
        }
        return null;
    }

    public interface EmptyInterface { }

    @Test
    public void generateEmptyInterface() {
        var provider = emptyInterfaceProvider;
        EmptyInterface generated = provider.generate(provider.newMethodYield());
        assertNotNull(generated);
        assertTrue(service.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                provider.generate(provider.newMethodYield())
        );
    }

    @Test
    public void generateShellEmptyInterface() {
        var provider = emptyInterfaceProvider;
        // Make shell
        ReloadShell<EmptyInterface> reloadShell = provider.generateShell();
        assertEquals(reloadShell, reloadShell);
        assertNull(reloadShell.getCurrentDelegate());
        EmptyInterface shell = reloadShell.getShell();
        assertNotNull(shell);
        assertEqualsBothWays(shell, shell);
        assertTrue(service.hasProduced(shell));

        // Test delegation
        EmptyInterface delegate = new EmptyInterface() {};
        assertNotEqualsBothWays(delegate, shell);
        reloadShell.setCurrentDelegate(delegate);
        assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
        assertNotEqualsBothWays(delegate, shell);
        assertTrue(shell.toString().contains(delegate.toString()));
        assertFalse(service.hasProduced(delegate));

        EmptyInterface shell2 = reloadShell.getShell();
        assertSame(shell, shell2, "Shell stays constant");
        assertThrows(IllegalArgumentException.class, () -> reloadShell.setCurrentDelegate(shell));

        // Equality with another shell
        ReloadShell<EmptyInterface> secondShell = provider.generateShell();
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
        var provider = emptyInterfaceProvider;
        EmptyInterface generated = provider.generateEmpty();
        assertNotNull(generated);
        assertTrue(service.hasProduced(generated));

        assertEqualsBothWays(generated, provider.generateEmpty());
        assertNotEqualsBothWays(generated, service.newProvider(RandomAccess.class, lookup).generateEmpty());

        assertEqualsBothWays(
                generated,
                provider.generate(provider.newMethodYield())
        );
    }

    public interface SingleMethod {

        String value();
    }

    private <I extends SingleMethod> MethodId valueMethod(ReflectionProvider<I> provider, TypeToken<I> typeToken) {
        return findMethodIn(provider.typeWalker(typeToken.getReifiedType()), SingleMethod.class, "value");
    }

    @Test
    public void generateSingleMethod() {
        var provider = singleMethodProvider;
        var valueMethod = valueMethod(provider, new TypeToken<SingleMethod>() {});

        String toReturn = "val1";
        MethodYield methodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, toReturn);
        }
        SingleMethod generated = provider.generate(methodYield.copy());
        assertNotNull(generated);
        assertEquals(toReturn, generated.value());
        assertTrue(service.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                provider.generate(methodYield)
        );
        assertNotEqualsBothWays(generated, () -> toReturn);
        assertTrue(generated.toString().contains(toReturn));

        MethodYield otherMethodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = otherMethodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, "junk");
        }
        assertNotEqualsBothWays(
                generated,
                provider.generate(otherMethodYield)
        );
    }

    @Test
    public void generateShellSingleMethod() {
        var provider = singleMethodProvider;
        var valueMethod = valueMethod(provider, new TypeToken<SingleMethod>() {});

        // Make shell
        ReloadShell<SingleMethod> reloadShell = provider.generateShell();
        assertEquals(reloadShell, reloadShell);
        assertNull(reloadShell.getCurrentDelegate());
        SingleMethod shell = reloadShell.getShell();
        assertNotNull(shell);
        assertEqualsBothWays(shell, shell);
        assertTrue(service.hasProduced(shell));

        // Test delegation
        String toReturn = "delegated";
        SingleMethod mainDelegate = () -> toReturn;
        assertNotEqualsBothWays(mainDelegate, shell);
        reloadShell.setCurrentDelegate(mainDelegate);
        assertEqualsBothWays(mainDelegate, reloadShell.getCurrentDelegate());
        assertNotEqualsBothWays(mainDelegate, shell);
        assertEquals("delegated", shell.value());
        assertTrue(shell.toString().contains(mainDelegate.toString()));
        assertFalse(service.hasProduced(mainDelegate));
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
        ReloadShell<SingleMethod> secondShell = provider.generateShell();
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
        MethodYield methodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, toReturn);
        }
        SingleMethod generated = provider.generate(methodYield);
        reloadShell.setCurrentDelegate(null);
        assertNotEqualsBothWays(generated, shell);
        reloadShell.setCurrentDelegate(generated);
        assertEqualsBothWays(generated, shell);
    }

    @Test
    public void generateEmptySingleMethod() {
        var provider = singleMethodProvider;
        var valueMethod = valueMethod(provider, new TypeToken<SingleMethod>() {});

        SingleMethod generated = provider.generateEmpty();
        assertNotNull(generated);
        assertTrue(service.hasProduced(generated));

        assertEqualsBothWays(generated, provider.generateEmpty());
        assertNotEqualsBothWays(generated, service.newProvider(EmptyInterface.class, lookup).generateEmpty());

        MethodYield otherMethodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = otherMethodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, "junk");
        }
        assertNotEqualsBothWays(
                generated,
                provider.generate(otherMethodYield)
        );
        ReloadShell<SingleMethod> reloadShell = provider.generateShell();
        assertNotEqualsBothWays(generated, reloadShell.getCurrentDelegate());
        reloadShell.setCurrentDelegate(generated);
        assertEqualsBothWays(generated, reloadShell.getCurrentDelegate());
        reloadShell.setCurrentDelegate(provider.generateEmpty());
        assertEqualsBothWays(generated, reloadShell.getCurrentDelegate());
    }

    public interface InheritedMethod extends SingleMethod {

    }

    @Test
    public void generateInheritedMethod() {
        var provider = inheritedMethodProvider;
        var valueMethod = valueMethod(provider, new TypeToken<InheritedMethod>() {});

        String toReturn = "val1";
        MethodYield methodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, toReturn);
        }
        InheritedMethod generated = provider.generate(methodYield.copy());
        assertNotNull(generated);
        assertEquals(toReturn, generated.value());
        assertTrue(service.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                provider.generate(methodYield.copy())
        );
        assertNotEqualsBothWays(
                generated,
                singleMethodProvider.generate(methodYield)
        );
        MethodYield otherMethodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = otherMethodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, "junk");
        }
        assertNotEqualsBothWays(
                generated,
                provider.generate(otherMethodYield)
        );
    }

    @Test
    public void generateShellInheritedMethod() {
        var provider = inheritedMethodProvider;
        var valueMethod = valueMethod(provider, new TypeToken<InheritedMethod>() {});

        // Make a shell
        ReloadShell<InheritedMethod> reloadShell = provider.generateShell();
        assertEquals(reloadShell, reloadShell);
        assertNull(reloadShell.getCurrentDelegate());
        InheritedMethod shell = reloadShell.getShell();
        assertNotNull(shell);
        assertEqualsBothWays(shell, shell);
        assertTrue(service.hasProduced(shell));

        // Test delegation
        String toReturn = "delegated";
        InheritedMethod mainDelegate = () -> toReturn;
        assertNotEqualsBothWays(mainDelegate, shell);
        reloadShell.setCurrentDelegate(mainDelegate);
        assertEqualsBothWays(mainDelegate, reloadShell.getCurrentDelegate());
        assertNotEqualsBothWays(mainDelegate, shell);
        assertEquals("delegated", shell.value());
        assertTrue(shell.toString().contains(mainDelegate.toString()));
        assertFalse(service.hasProduced(mainDelegate));
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
        ReloadShell<InheritedMethod> secondShell = provider.generateShell();
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
        ReloadShell<SingleMethod> superShell = singleMethodProvider.generateShell();
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
        MethodYield methodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, toReturn);
        }
        InheritedMethod generated = provider.generate(methodYield);
        reloadShell.setCurrentDelegate(null);
        assertNotEqualsBothWays(generated, shell);
        reloadShell.setCurrentDelegate(mainDelegate);
        assertNotEqualsBothWays(generated, shell);
        reloadShell.setCurrentDelegate(generated);
        assertEqualsBothWays(generated, shell);
    }

    @Test
    public void generateEmptyInheritedMethod() {
        var provider = inheritedMethodProvider;
        var valueMethod = valueMethod(provider, new TypeToken<InheritedMethod>() {});

        InheritedMethod generated = provider.generateEmpty();
        assertNotNull(generated);
        assertTrue(service.hasProduced(generated));

        assertEqualsBothWays(generated, provider.generateEmpty());
        assertNotEqualsBothWays(generated, singleMethodProvider.generateEmpty());

        MethodYield methodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, "junk");
        }
        assertNotEqualsBothWays(
                generated,
                provider.generate(methodYield)
        );
    }

    public interface PlusDefaultMethod extends InheritedMethod {

        default <T> T giveBack(Supplier<T> what) {
            return what.get();
        }
    }

    private <I extends PlusDefaultMethod> MethodId giveBackMethod(ReflectionProvider<I> provider, TypeToken<I> typeToken) {
        return findMethodIn(provider.typeWalker(typeToken.getReifiedType()), PlusDefaultMethod.class, "giveBack");
    }

    private void testGiveBack(PlusDefaultMethod plusDefaultMethod) {
        assertEquals("hello", plusDefaultMethod.giveBack(() -> "hello"));
        assertEquals(3, plusDefaultMethod.giveBack(() -> 3));
    }

    @Test
    public void generatePlusDefaultMethod() {
        var provider = plusDefaultMethodProvider;
        var valueMethod = valueMethod(provider, new TypeToken<PlusDefaultMethod>() {});
        var giveBackMethod = giveBackMethod(provider, new TypeToken<PlusDefaultMethod>() {});

        String toReturn = "val1";
        MethodYield methodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, toReturn);
        }
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(PlusDefaultMethod.class)) {
            methodYieldFor.callDefault(giveBackMethod);
        }
        PlusDefaultMethod generated = provider.generate(methodYield.copy());
        assertNotNull(generated);
        assertEquals(toReturn, generated.value());
        testGiveBack(generated);
        assertTrue(service.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                provider.generate(methodYield.copy())
        );
        methodYield.clear();
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, toReturn);
        }
        assertNotEqualsBothWays(
                generated,
                inheritedMethodProvider.generate(methodYield.copy())
        );
        MethodYield otherMethodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = otherMethodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, "junk");
        }
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(PlusDefaultMethod.class)) {
            methodYieldFor.callDefault(giveBackMethod);
        }
        assertNotEqualsBothWays(
                generated,
                provider.generate(otherMethodYield)
        );
    }

    @Test
    public void generateShellPlusDefaultMethod() {
        var provider = plusDefaultMethodProvider;
        // Make a shell
        ReloadShell<PlusDefaultMethod> reloadShell = provider.generateShell();
        assertEquals(reloadShell, reloadShell);
        assertNull(reloadShell.getCurrentDelegate());
        PlusDefaultMethod shell = reloadShell.getShell();
        assertNotNull(shell);
        assertEqualsBothWays(shell, shell);
        assertTrue(service.hasProduced(shell));

        // Test delegation
        PlusDefaultMethod delegate = () -> "delegated";
        assertNotEqualsBothWays(delegate, shell);
        reloadShell.setCurrentDelegate(delegate);
        assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
        assertNotEqualsBothWays(delegate, shell);
        assertEquals("delegated", shell.value());
        assertTrue(shell.toString().contains(delegate.toString()));
        assertFalse(service.hasProduced(delegate));
        assertThrows(IllegalArgumentException.class, () -> reloadShell.setCurrentDelegate(shell));
        PlusDefaultMethod shellCopy = reloadShell.getShell();
        assertSame(shell, shellCopy, "Shell stays constant");
        // generateShell() is not supposed to have special handling for default methods, but we test it anyway
        testGiveBack(shell);

        // Equality with another shell
        ReloadShell<PlusDefaultMethod> secondShell = provider.generateShell();
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
        var provider = plusDefaultMethodProvider;
        var valueMethod = valueMethod(provider, new TypeToken<PlusDefaultMethod>() {});
        var giveBackMethod = giveBackMethod(provider, new TypeToken<PlusDefaultMethod>() {});

        PlusDefaultMethod generated = provider.generateEmpty();
        assertNotNull(generated);
        testGiveBack(generated);
        assertTrue(service.hasProduced(generated));

        MethodYield methodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, "junk");
        }
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(PlusDefaultMethod.class)) {
            methodYieldFor.callDefault(giveBackMethod);
        }
        assertNotEqualsBothWays(
                generated,
                provider.generate(methodYield)
        );
        assertEqualsBothWays(generated, provider.generateEmpty());
        assertNotEqualsBothWays(generated, singleMethodProvider.generateEmpty());
    }

    public interface ExhilirantMethod extends SingleMethod {

        default Object another(String dummy) {
            return dummy;
        }
    }

    private MethodId anotherMethod() {
        var provider = exhilirantMethodProvider;
        return findMethodIn(provider.typeWalker(new TypeToken<ExhilirantMethod>() {}.getReifiedType()), ExhilirantMethod.class, "another");
    }

    @Test
    public void generateExhilirantMethod() {
        var provider = exhilirantMethodProvider;
        var valueMethod = valueMethod(provider, new TypeToken<ExhilirantMethod>() {});
        var anotherMethod = anotherMethod();

        String returnVal = "val1";
        MethodYield methodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, returnVal);
        }
        try (MethodYield.ForImplementable methodYieldFor = methodYield.forImplementable(ExhilirantMethod.class)) {
            methodYieldFor.callDefault(anotherMethod);
        }
        ExhilirantMethod generated = provider.generate(methodYield.copy());
        assertNotNull(generated);
        assertEquals(returnVal, generated.value());
        assertEquals("arg", generated.another("arg"));
        assertTrue(service.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                provider.generate(methodYield.copy())
        );
        assertNotEqualsBothWays(
                generated,
                singleMethodProvider.generate(methodYield)
        );
        MethodYield otherMethodYield = provider.newMethodYield();
        try (MethodYield.ForImplementable methodYieldFor = otherMethodYield.forImplementable(SingleMethod.class)) {
            methodYieldFor.returnValue(valueMethod, returnVal);
        }
        try (MethodYield.ForImplementable methodYieldFor = otherMethodYield.forImplementable(ExhilirantMethod.class)) {
            methodYieldFor.returnValue(anotherMethod, "exhilirating");
        }
        ExhilirantMethod otherGenerated = provider.generate(otherMethodYield.copy());
        assertNotEqualsBothWays(otherGenerated, generated);
        assertEqualsBothWays(otherGenerated, otherGenerated);
        assertEqualsBothWays(otherGenerated, provider.generate(otherMethodYield));
    }


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
                                .takeWhile(frame -> !frame.getMethodName().equals("invokerEfficiency"))
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

        static Comparable<String> staticIgnore() {
            return "static ignore";
        }
    }

    private MethodId hello, overidden, anotherCall, giveBack, giveArray;
    MethodId checkReflection;

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
        var provider = service.newProvider(Base.class, lookup);
        ReflectionProvider.TypeWalker baseWalker = provider.typeWalker(new TypeToken<Base>() {}.getReifiedType());
        baseWalker.getViableMethods().forEach(this::setField);
        ReflectionProvider.TypeWalker midLevelWalker = baseWalker.getSuperTypes()[0];
        midLevelWalker.getViableMethods().forEach(this::setField);
        ReflectionProvider.TypeWalker parentWalker = midLevelWalker.getSuperTypes()[0];
        parentWalker.getViableMethods().forEach(this::setField);
    }

    @Test
    public void typeWalker() {
        var provider = service.newProvider(Base.class, lookup);
        ReifiedType baseType = new TypeToken<Base>() {}.getReifiedType();
        ReflectionProvider.TypeWalker baseWalker = provider.typeWalker(baseType);
        assertEquals(baseType, baseWalker.getEnclosingType(), "Return correct getEnclosingType()");
        assertEquals(
                Set.of(anotherCall, giveBack, checkReflection, giveArray),
                baseWalker.getViableMethods().collect(Collectors.toSet()),
                "Returns correct getViableMethods()"
        );
        Set<MethodId> expectedData = Set.of(
                new MethodId(
                        "anotherCall", ReifiedType.rawUnannotated(void.class), new ReifiedType[0], false
                ),
                new MethodId(
                        "giveBack", new TypeToken<Object>() {}.getReifiedType(), new ReifiedType[] {new TypeToken<Supplier<Object>>() {}.getReifiedType()}, true
                ),
                new MethodId(
                        "checkReflection", ReifiedType.rawUnannotated(void.class), new ReifiedType[] {new TypeToken<Consumer<Object>>() {}.getReifiedType()}, true
                ),
                new MethodId(
                        "giveArray", new TypeToken<String[]>() {}.getReifiedType(), new ReifiedType[0], true
                )
        );
        assertEquals(expectedData, Set.of(anotherCall, giveBack, checkReflection, giveArray));

        ReflectionProvider.TypeWalker midWalker;
        {
            ReflectionProvider.TypeWalker[] superTypes = baseWalker.getSuperTypes();
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
        ReflectionProvider.TypeWalker parentWalker;
        {
            ReflectionProvider.TypeWalker[] superTypes = midWalker.getSuperTypes();
            assertEquals(1, superTypes.length, "Return correct getSuperTypes()");
            parentWalker = superTypes[0];
            assertNotNull(parentWalker, "Return correct getSuperTypes()");
        }
        assertEquals(new TypeToken<Parent<String>>() {}.getReifiedType(), parentWalker.getEnclosingType(), "Return correct getEnclosingType()");
    }

    @Test
    public void makeInvoker() {
        var provider = service.newProvider(Base.class, lookup);
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
        ReflectionProvider.Invoker<Base> invokeBase = provider.makeInvoker(base, new TypeToken<Base>() {});
        assertDoesNotThrow(() -> invokeBase.invokeMethod(anotherCall),
                "Calls method via MethodInvoker#invokeMethod and uses correct implementation");
        assertTrue(anotherCallHook.get(),
                "Calls method via MethodInvoker#invokeMethod and uses correct implementation");
        assertThrows(IllegalStateException.class, () -> invokeBase.invokeMethod(anotherCall),
                "Catches exception when thrown by method via MethodInvoker#invokeMethod");
        Object invokeGiveBack = assertDoesNotThrow(() -> invokeBase.invokeMethod(giveBack, (Supplier<Boolean>) () -> true),
                "Calls default method via MethodInvoker#invokeMethod, and uses default implementation");
        assertEquals(true, invokeGiveBack);
        Object invokeGiveArray = assertDoesNotThrow(() -> invokeBase.invokeMethod(giveArray),
                "Calls overidden (originally default) method via MethodInvoker#invokeMethod, and uses overidden implementation");
        assertSame(presetArray, invokeGiveArray);

        ReflectionProvider.Invoker<Parent<String>> invokeParent = provider.makeInvoker(base, new TypeToken<Parent<String>>() {});
        assertEquals(0, assertDoesNotThrow(() -> invokeParent.invokeMethod(hello)),
                "Calls method in parent class via MethodInvoker#invokeMethod using correct invoker");
        assertEquals(1, assertDoesNotThrow(() -> invokeParent.invokeMethod(hello)),
                "Calls method in parent class via MethodInvoker#invokeMethod using correct invoker");
        assertEquals(2, helloHook.get());
    }
}
