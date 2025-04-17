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

import space.arim.dazzleconf.internal.util.ImmutableCollections;
import space.arim.dazzleconf.internal.util.MethodUtil;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class ProxyHandlerToValues extends ProxyHandler {

    private final Map<String, Object> fastValues;
    private Map<Method, MethodHandle> defaultMethodsMap;

    ProxyHandlerToValues(Map<String, Object> fastValues) {
        this.fastValues = ImmutableCollections.mapOf(fastValues);
    }

    @Override
    Object implInvoke(Method method, Object[] args) throws Throwable {
        MethodHandle defaultMethodHandle;
        if (defaultMethodsMap != null && MethodUtil.isDefault(method)
                && (defaultMethodHandle = defaultMethodsMap.get(method)) != null) {
            return defaultMethodHandle.invokeWithArguments(args);
        }
        return fastValues.get(method.getName());
    }

    void initDefaultMethods(Object proxy, Set<Method> defaultMethods) {
        defaultMethodsMap = buildDefaultMethodsMap(proxy, defaultMethods);
    }

    private static Map<Method, MethodHandle> buildDefaultMethodsMap(Object proxy, Set<Method> defaultMethods) {
        Map<Method, MethodHandle> result = new HashMap<>((int) (defaultMethods.size() / 0.98f), 0.999f);
        for (Method method : defaultMethods) {
            MethodHandle methodHandle = MethodUtil.createDefaultMethodHandle(method).bindTo(proxy);
            result.put(method, methodHandle);
        }
        return ImmutableCollections.mapOf(result);
    }
}
