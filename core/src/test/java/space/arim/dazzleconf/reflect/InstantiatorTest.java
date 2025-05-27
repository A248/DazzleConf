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
import space.arim.dazzleconf2.ReloadShell;
import space.arim.dazzleconf2.reflect.*;

import java.util.RandomAccess;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public abstract class InstantiatorTest {

    private final ClassLoader classLoader = getClass().getClassLoader();
    private final Instantiator instantiator;

    protected InstantiatorTest(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    private static <T> void assertEqualsBothWays(T val1, T val2) {
        assertEquals(val1, val2);
        assertEquals(val2, val1);
        assertEquals(val1.hashCode(), val2.hashCode());
    }

    private static <T> void assertNotEqualsBothWays(T val1, T val2) {
        assertNotEquals(val1, val2);
        assertNotEquals(val2, val1);
        assertNotEquals(val1.toString(), val2.toString(), "toString should be implemented reasonably");
    }

    public interface EmptyInterface {

    }

    @Test
    public void generateEmptyInterface() {
        Object generated = instantiator.generate(classLoader, new Class[] {EmptyInterface.class}, new MethodYield.Builder().build());
        assertNotNull(generated);
        assertInstanceOf(EmptyInterface.class, generated);
        assertTrue(instantiator.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                instantiator.generate(classLoader, new Class[] {EmptyInterface.class}, new MethodYield.Builder().build())
        );
    }

    @Test
    public void generateShellEmptyInterface() {
        ReloadShell<EmptyInterface> reloadShell = instantiator.generateShell(classLoader, EmptyInterface.class);
        assertNull(reloadShell.getCurrentDelegate());
        EmptyInterface shell1 = reloadShell.getShell();
        assertNotNull(shell1);
        assertEqualsBothWays(shell1, shell1);
        assertTrue(instantiator.hasProduced(shell1));

        EmptyInterface delegate = new EmptyInterface() {};
        reloadShell.setCurrentDelegate(delegate);
        assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
        assertFalse(instantiator.hasProduced(delegate));

        EmptyInterface shell2 = reloadShell.getShell();
        assertSame(shell1, shell2, "Shell stays constant");

        ReloadShell<EmptyInterface> secondShell = instantiator.generateShell(classLoader, EmptyInterface.class);
        assertNotEqualsBothWays(reloadShell, secondShell);
        assertNotEqualsBothWays(shell1, secondShell.getShell());
        secondShell.setCurrentDelegate(delegate);
        assertNotEqualsBothWays(shell1, secondShell.getCurrentDelegate());
        assertEqualsBothWays(shell1, secondShell.getShell());
    }

    @Test
    public void generateEmptyEmptyInterface() {
        Object generated = instantiator.generateEmpty(classLoader, EmptyInterface.class);
        assertNotNull(generated);
        assertInstanceOf(EmptyInterface.class, generated);
        assertTrue(instantiator.hasProduced(generated));

        assertNotEqualsBothWays(
                generated,
                instantiator.generate(classLoader, new Class[] {EmptyInterface.class}, new MethodYield.Builder().build())
        );
    }

    public interface SingleMethod {

        String value();
    }

    @Test
    public void generateSingleMethod() {
        String toReturn = "val1";
        MethodYield.Builder methodYield = new MethodYield.Builder();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        Object generated = instantiator.generate(classLoader, new Class[] {SingleMethod.class}, methodYield.build());
        assertNotNull(generated);
        assertInstanceOf(SingleMethod.class, generated);
        assertEquals(toReturn, ((SingleMethod) generated).value());
        assertTrue(instantiator.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                instantiator.generate(classLoader, new Class[] {SingleMethod.class}, methodYield.build())
        );
    }

    @Test
    public void generateSingleMethodWithAdditionalInterface() {
        Class<?> additionalInterface = RandomAccess.class;
        String toReturn = "val1";
        MethodYield.Builder methodYield = new MethodYield.Builder();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        Object generated = instantiator.generate(classLoader, new Class[] {SingleMethod.class, additionalInterface}, methodYield.build());
        assertNotNull(generated);
        assertInstanceOf(SingleMethod.class, generated);
        assertInstanceOf(additionalInterface, generated);
        assertEquals(toReturn, ((SingleMethod) generated).value());
        assertTrue(instantiator.hasProduced(generated));

        assertNotEqualsBothWays(
                generated,
                instantiator.generate(classLoader, new Class[] {SingleMethod.class}, methodYield.build())
        );
    }

    @Test
    public void generateShellSingleMethod() {
        ReloadShell<SingleMethod> reloadShell = instantiator.generateShell(classLoader, SingleMethod.class);
        assertNull(reloadShell.getCurrentDelegate());
        SingleMethod shell1 = reloadShell.getShell();
        assertNotNull(shell1);
        assertEqualsBothWays(shell1, shell1);
        assertTrue(instantiator.hasProduced(shell1));

        SingleMethod delegate = () -> "delegated";
        reloadShell.setCurrentDelegate(delegate);
        assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
        assertEquals("delegated", shell1.value());
        assertFalse(instantiator.hasProduced(delegate));

        SingleMethod shell2 = reloadShell.getShell();
        assertSame(shell1, shell2, "Shell stays constant");

        ReloadShell<SingleMethod> secondShell = instantiator.generateShell(classLoader, SingleMethod.class);
        assertNotEqualsBothWays(reloadShell, secondShell);
        assertNotEqualsBothWays(shell1, secondShell.getShell());
        secondShell.setCurrentDelegate(delegate);
        assertNotEqualsBothWays(shell1, secondShell.getCurrentDelegate());
        assertEqualsBothWays(shell1, secondShell.getShell());
    }

    @Test
    public void generateEmptySingleMethod() {
        Object generated = instantiator.generateEmpty(classLoader, SingleMethod.class);
        assertNotNull(generated);
        assertInstanceOf(SingleMethod.class, generated);
        assertTrue(instantiator.hasProduced(generated));

        MethodYield.Builder methodYield = new MethodYield.Builder();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), "junk"
        );
        assertNotEqualsBothWays(
                generated,
                instantiator.generate(classLoader, new Class[] {SingleMethod.class}, methodYield.build())
        );
    }

    public interface InheritedMethod extends SingleMethod {

    }

    @Test
    public void generateInheritedMethod() {
        String toReturn = "val1";
        MethodYield.Builder methodYield = new MethodYield.Builder();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        Object generated = instantiator.generate(classLoader, new Class[] {InheritedMethod.class, SingleMethod.class}, methodYield.build());
        assertNotNull(generated);
        assertInstanceOf(InheritedMethod.class, generated);
        assertEquals(toReturn, ((InheritedMethod) generated).value());
        assertTrue(instantiator.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                instantiator.generate(classLoader, new Class[] {InheritedMethod.class, SingleMethod.class}, methodYield.build())
        );
    }

    @Test
    public void generateInheritedMethodWithAdditionalInterface() {
        Class<?> additionalInterface = RandomAccess.class;
        String toReturn = "val1";
        MethodYield.Builder methodYield = new MethodYield.Builder();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        Object generated = instantiator.generate(classLoader, new Class[] {InheritedMethod.class, SingleMethod.class, additionalInterface}, methodYield.build());
        assertNotNull(generated);
        assertInstanceOf(InheritedMethod.class, generated);
        assertInstanceOf(additionalInterface, generated);
        assertEquals(toReturn, ((InheritedMethod) generated).value());
        assertTrue(instantiator.hasProduced(generated));

        assertNotEqualsBothWays(
                generated,
                instantiator.generate(classLoader, new Class[] {InheritedMethod.class, SingleMethod.class}, methodYield.build())
        );
    }

    @Test
    public void generateShellInheritedMethod() {
        ReloadShell<InheritedMethod> reloadShell = instantiator.generateShell(classLoader, InheritedMethod.class);
        assertNull(reloadShell.getCurrentDelegate());
        InheritedMethod shell1 = reloadShell.getShell();
        assertNotNull(shell1);
        assertEqualsBothWays(shell1, shell1);
        assertTrue(instantiator.hasProduced(shell1));

        InheritedMethod delegate = () -> "delegated";
        reloadShell.setCurrentDelegate(delegate);
        assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
        assertEquals("delegated", shell1.value());
        assertFalse(instantiator.hasProduced(delegate));

        InheritedMethod shell2 = reloadShell.getShell();
        assertSame(shell1, shell2, "Shell stays constant");

        ReloadShell<InheritedMethod> secondShell = instantiator.generateShell(classLoader, InheritedMethod.class);
        assertNotEqualsBothWays(reloadShell, secondShell);
        assertNotEqualsBothWays(shell1, secondShell.getShell());
        secondShell.setCurrentDelegate(delegate);
        assertNotEqualsBothWays(shell1, secondShell.getCurrentDelegate());
        assertEqualsBothWays(shell1, secondShell.getShell());
    }

    @Test
    public void generateEmptyInheritedMethod() {
        Object generated = instantiator.generateEmpty(classLoader, InheritedMethod.class);
        assertNotNull(generated);
        assertInstanceOf(InheritedMethod.class, generated);
        assertTrue(instantiator.hasProduced(generated));

        MethodYield.Builder methodYield = new MethodYield.Builder();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), "junk"
        );
        assertNotEqualsBothWays(
                generated,
                instantiator.generate(classLoader, new Class[] {SingleMethod.class, InheritedMethod.class}, methodYield.build())
        );
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
        MethodYield.Builder methodYield = new MethodYield.Builder();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        methodYield.addValue(
                PlusDefaultMethod.class, new MethodId(
                        "giveBack", new TypeToken<Object>() {}.getReifiedType(), new ReifiedType[] {new TypeToken<Supplier<Object>>() {}.getReifiedType()}, false
                ), new InvokeDefaultFunction());
        Object generated = instantiator.generate(classLoader, new Class[] {PlusDefaultMethod.class, InheritedMethod.class, SingleMethod.class}, methodYield.build());
        assertNotNull(generated);
        assertInstanceOf(PlusDefaultMethod.class, generated);
        assertEquals(toReturn, ((PlusDefaultMethod) generated).value());
        testGiveBack((PlusDefaultMethod) generated);
        assertTrue(instantiator.hasProduced(generated));

        assertEqualsBothWays(
                generated,
                instantiator.generate(classLoader, new Class[] {PlusDefaultMethod.class, InheritedMethod.class, SingleMethod.class}, methodYield.build())
        );
    }

    @Test
    public void generatePlusDefaultMethodWithAdditionalInterface() {
        Class<?> additionalInterface = RandomAccess.class;
        String toReturn = "val1";
        MethodYield.Builder methodYield = new MethodYield.Builder();
        methodYield.addValue(
                SingleMethod.class, new MethodId(
                        "value", new TypeToken<String>() {}.getReifiedType(), new ReifiedType[0], false
                ), toReturn
        );
        methodYield.addValue(
                PlusDefaultMethod.class, new MethodId(
                        "giveBack", new TypeToken<Object>() {}.getReifiedType(), new ReifiedType[] {new TypeToken<Supplier<Object>>() {}.getReifiedType()}, false
                ), new InvokeDefaultFunction());
        Object generated = instantiator.generate(classLoader, new Class[] {PlusDefaultMethod.class, InheritedMethod.class, SingleMethod.class, additionalInterface}, methodYield.build());
        assertNotNull(generated);
        assertInstanceOf(PlusDefaultMethod.class, generated);
        assertInstanceOf(additionalInterface, generated);
        assertEquals(toReturn, ((PlusDefaultMethod) generated).value());
        testGiveBack((PlusDefaultMethod) generated);
        assertTrue(instantiator.hasProduced(generated));

        assertNotEqualsBothWays(
                generated,
                instantiator.generate(classLoader, new Class[] {PlusDefaultMethod.class, InheritedMethod.class, SingleMethod.class}, methodYield.build())
        );
    }

    @Test
    public void generateShellPlusDefaultMethod() {
        ReloadShell<PlusDefaultMethod> reloadShell = instantiator.generateShell(classLoader, PlusDefaultMethod.class);
        assertNull(reloadShell.getCurrentDelegate());
        PlusDefaultMethod shell1 = reloadShell.getShell();
        assertNotNull(shell1);
        assertEqualsBothWays(shell1, shell1);
        assertTrue(instantiator.hasProduced(shell1));

        PlusDefaultMethod delegate = () -> "delegated";
        reloadShell.setCurrentDelegate(delegate);
        assertEqualsBothWays(delegate, reloadShell.getCurrentDelegate());
        assertEquals("delegated", shell1.value());
        assertFalse(instantiator.hasProduced(delegate));

        PlusDefaultMethod shell2 = reloadShell.getShell();
        assertSame(shell1, shell2, "Shell stays constant");
        testGiveBack(shell1);

        ReloadShell<PlusDefaultMethod> secondShell = instantiator.generateShell(classLoader, PlusDefaultMethod.class);
        assertNotEqualsBothWays(reloadShell, secondShell);
        assertNotEqualsBothWays(shell1, secondShell.getShell());
        secondShell.setCurrentDelegate(delegate);
        assertNotEqualsBothWays(shell1, secondShell.getCurrentDelegate());
        assertEqualsBothWays(shell1, secondShell.getShell());
    }

    @Test
    public void generateEmptyPlusDefaultMethod() {
        Object generated = instantiator.generateEmpty(classLoader, PlusDefaultMethod.class);
        assertNotNull(generated);
        assertInstanceOf(PlusDefaultMethod.class, generated);
        testGiveBack((PlusDefaultMethod) generated);
        assertTrue(instantiator.hasProduced(generated));

        MethodYield.Builder methodYield = new MethodYield.Builder();
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
                instantiator.generate(classLoader, new Class[] {SingleMethod.class, InheritedMethod.class, PlusDefaultMethod.class}, methodYield.build())
        );
    }

}
