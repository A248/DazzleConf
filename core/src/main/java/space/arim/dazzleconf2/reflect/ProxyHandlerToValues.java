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
import space.arim.dazzleconf2.internals.ImmutableCollections;
import space.arim.dazzleconf2.internals.MethodUtil;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class ProxyHandlerToValues extends ProxyHandler {

    private final Class<?>[] targets;
    private final Map<String, Object> fastValues;
    private Map<Method, MethodHandle> defaultMethodsMap;

    ProxyHandlerToValues(Class<?>[] targets, Map<String, Object> fastValues) {
        this.targets = targets;
        this.fastValues = ImmutableCollections.mapOf(fastValues);
    }

    @Override
    Object implInvoke(Method method, Object[] args) throws Throwable {
        Object fastValue = fastValues.get(method.getName());
        if (fastValue != null) {
            return fastValue;
        }
        MethodHandle defaultMethodHandle;
        if (defaultMethodsMap != null && MethodUtil.isDefault(method)
                && (defaultMethodHandle = defaultMethodsMap.get(method)) != null) {
            return defaultMethodHandle.invokeWithArguments(args);
        }
        // We shouldn't reach here, but if we do, it means the method maps we were provided are lacking
        // We could throw an exception. But that might slow down the compiled method. So just use an assert.
        assert false : "Bad proxy; incomplete data";
        return null;
    }

    @Override
    boolean implEquals(Object ourProxy, Object otherProxy, ProxyHandler otherHandler) {
        if (otherHandler instanceof ProxyHandlerToValues) {
            ProxyHandlerToValues that = (ProxyHandlerToValues) otherHandler;
            // No need to add defaultMethodsMap: it's the mirror of whatever is left out of fastValues
            return Arrays.equals(targets, that.targets) && fastValues.equals(that.fastValues);
        }
        if (otherHandler instanceof ProxyHandlerToDelegate) {
            // Invert direction => unwrap the delegate
            return otherHandler.implEquals(otherProxy, ourProxy, this);
        }
        if (otherHandler instanceof ProxyHandlerToEmpty) {
            // We're never equal - we can never know if we implement the same interfaces
            return false;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    int implHashCode() {
        // WARNING: Keep in sync with ProxyHandlerToEmpty#hashCode
        return fastValues.hashCode();
    }

    @Override
    void implToString(StringBuilder output) {
        for (Class<?> target : targets) {
            output.append(target.getName()).append(',');
        }
        fastValues.forEach((k, v) -> output.append(k).append('=').append(v).append(','));
    }

    void initDefaultMethods(Object proxy, Set<Method> defaultMethods) {
        defaultMethodsMap = buildDefaultMethodsMap(proxy, defaultMethods);
    }

    private static Map<Method, MethodHandle> buildDefaultMethodsMap(Object proxy, Set<Method> defaultMethods) {
        Map<Method, MethodHandle> result = new HashMap<>((int) (defaultMethods.size() / 0.98f), 0.999f);
        for (Method method : defaultMethods) {
            MethodHandle methodHandle;
            try {
                methodHandle = MethodUtil.createDefaultMethodHandle(method).bindTo(proxy);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                String className = method.getDeclaringClass().getName();
                throw new DeveloperMistakeException(
                        "Unable to generate default method accessor for " + className + '#' + method.getName(),
                        ex
                );
            }
            result.put(method, methodHandle);
        }
        return ImmutableCollections.mapOf(result);
    }
}
