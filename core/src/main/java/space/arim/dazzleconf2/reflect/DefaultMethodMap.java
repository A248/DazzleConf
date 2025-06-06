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

package space.arim.dazzleconf2.reflect;

import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.internals.MethodUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

final class DefaultMethodMap {

    /**
     * Methods whose default implementation to call. This works because java.lang.reflect.Proxy always passes the
     * overidden Method object, so keys match.
     */
    private final Map<Method, MethodHandle> methodHandles;

    DefaultMethodMap(Map<Method, MethodHandle> methodHandles) {
        this.methodHandles = methodHandles;
    }

    void addMethod(MethodHandles.Lookup lookup, Method method, Object receiver) {
        MethodHandle methodHandle;
        try {
            methodHandle = MethodUtil.createDefaultMethodHandle(method, lookup);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            String className = method.getDeclaringClass().getName();
            throw new DeveloperMistakeException(
                    "Unable to generate method accessor for " + className + '#' + method.getName(),
                    ex
            );
        }
        methodHandles.put(method, methodHandle.bindTo(receiver));
    }

    MethodHandle getHandleWithBoundReceiver(Method method) {
        return methodHandles.get(method);
    }
}
