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
public final class ProxyReflectionInstantiator implements Instantiator {

    /**
     * Creates
     */
    public ProxyReflectionInstantiator() {}

    @Override
    public Object generate(ClassLoader classLoader, Set<Class<?>> targets, MethodYield methodYield) {

        Map<String, Object> fastValues = new HashMap<>(20, 0.80f);
        Set<Method> defaultMethods = null;

        for (Class<?> target : targets) {
            for (Map.Entry<MethodId, Object> entry : methodYield.valuesFor(target).entrySet()) {

                MethodId methodId = entry.getKey();
                Object value = entry.getValue();

                if (value == InvokeDefaultValue.INSTANCE) {
                    if (defaultMethods == null) {
                        defaultMethods = new HashSet<>();
                    }
                    defaultMethods.add(methodId.method().orElseThrow());
                } else {
                    fastValues.put(methodId.name(), value);
                }
            }
        }
        ProxyAgentToValues proxyAgent = new ProxyAgentToValues(fastValues);
        Object proxy = Proxy.newProxyInstance(classLoader, targets.toArray(Class[]::new), proxyAgent);
        if (defaultMethods != null) {
            proxyAgent.initDefaultMethods(proxy, defaultMethods);
        }
        return proxy;
    }

    @Override
    public <I> ReloadShell<I> generateShell(ClassLoader classLoader, Class<I> iface, Set<MethodId> methods) {
        ProxyAgentToDelegate<I> proxyAgent = new ProxyAgentToDelegate<>();
        @SuppressWarnings("unchecked")
        I shell = (I) Proxy.newProxyInstance(classLoader, new Class[] {iface}, proxyAgent);
        return proxyAgent.new AsReloadShell(shell);
    }

}
