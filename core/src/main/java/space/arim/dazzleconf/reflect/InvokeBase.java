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

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.DeveloperMistakeException;

import java.lang.reflect.Method;

abstract class InvokeBase<I> implements ReflectionProvider.Invoker<I> {

    final I receiver;
    private final Class<I> declaringType;

    InvokeBase(I receiver, Class<I> declaringType) {
        this.receiver = receiver;
        this.declaringType = declaringType;
    }

    @Override
    public @NonNull I getReceiver() {
        return receiver;
    }

    Method getCheckedMethod(MethodId methodId) {
        // We rely on the method cache - it gives us the real java.lang.reflect.Method for the MethodId
        // Reconstructing the Method is actually not possible, because MethodId reifies generic variables
        Method method = DefaultReflectionProvider.getMethodFromCache(methodId);
        if (!method.getDeclaringClass().equals(declaringType)) {
            throw new DeveloperMistakeException("Method ID not from the enclosing type: " + methodId);
        }
        return method;
    }
}
