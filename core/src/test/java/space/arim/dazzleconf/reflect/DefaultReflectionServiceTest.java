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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultReflectionServiceTest extends ReflectionServiceTest {

    public DefaultReflectionServiceTest() {
        super(new DefaultReflectionService());
    }

    @Test
    public void invokerEfficiency() {
        var provider = service.newProvider(Base.class, lookup);
        AtomicBoolean checkReflectionOutcome = new AtomicBoolean();
        Consumer<? super Boolean> checkReflection = checkReflectionOutcome::set;

        Base generatedBase = provider.generateEmpty();
        ReflectionProvider.Invoker<Base> invokeGeneratedBase = provider.makeInvoker(
                generatedBase, new TypeToken<>() {}
        );
        // Efficiency test: Make sure that ReflectionService.Invoker bypasses standard reflection
        // First check if our testing apparatus is set up correctly
        try {
            Base.class.getDeclaredMethod("checkReflection", Consumer.class).invoke(generatedBase, checkReflection);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new AssertionError("Calling problem", ex);
        }
        assertTrue(checkReflectionOutcome.get(), "Test setup: Called with reflection");
        generatedBase.checkReflection(checkReflection);
        assertFalse(checkReflectionOutcome.get(), "Test setup: Not called with reflection");

        // Then call this method using the invoker
        try {
            invokeGeneratedBase.invokeMethod(this.checkReflection, checkReflection);
        } catch (Throwable ex) {
            throw new AssertionError( "Calling problem", ex);
        }
        assertFalse(checkReflectionOutcome.get(), "Calling method via MethodInvoker#invokeMethod should not use standard reflection if called upon a proxy");
    }
}
