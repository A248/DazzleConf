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

import space.arim.dazzleconf2.ReloadShell;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link Instantiator} using standard proxy reflection
 *
 */
public final class DefaultInstantiator implements Instantiator {

    /**
     * Creates
     */
    public DefaultInstantiator() {}

    @Override
    public Object generate(ClassLoader classLoader, Set<Class<?>> targets, MethodYield methodYield) {

        Map<String, Object> fastValues = new HashMap<>(20, 0.80f);
        Set<Method> defaultMethods = null;

        for (Class<?> target : targets) {
            for (Map.Entry<MethodId, Object> entry : methodYield.valuesFor(target)) {

                MethodId methodId = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof InvokeDefaultValue) {
                    if (defaultMethods == null) {
                        defaultMethods = new HashSet<>();
                    }
                    defaultMethods.add(methodId.getMethod(target));
                } else {
                    fastValues.put(methodId.name(), value);
                }
            }
        }
        ProxyHandlerToValues proyHandler = new ProxyHandlerToValues(fastValues);
        Object proxy = Proxy.newProxyInstance(classLoader, targets.toArray(Class[]::new), proyHandler);
        if (defaultMethods != null) {
            proyHandler.initDefaultMethods(proxy, defaultMethods);
        }
        return proxy;
    }

    @Override
    public <I> ReloadShell<I> generateShell(ClassLoader classLoader, Class<I> iface, Set<MethodId> methods) {
        ProxyHandlerToDelegate<I> proxyHandler = new ProxyHandlerToDelegate<>();
        @SuppressWarnings("unchecked")
        I shell = (I) Proxy.newProxyInstance(classLoader, new Class[] {iface}, proxyHandler);
        return proxyHandler.new AsReloadShell(shell);
    }

}
