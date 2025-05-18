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

import org.checkerframework.checker.nullness.qual.NonNull;
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
    public boolean hasProduced(@NonNull Object instance) {
        return Proxy.isProxyClass(instance.getClass()) && Proxy.getInvocationHandler(instance) instanceof ProxyHandler;
    }

    @Override
    public @NonNull Object generate(@NonNull ClassLoader classLoader, @NonNull Class<?> @NonNull [] targets,
                                    @NonNull MethodYield methodYield) {

        Map<String, Object> fastValues = new HashMap<>(20, 0.80f);
        Set<Method> defaultMethods = null;

        for (Class<?> target : targets) {
            for (Map.Entry<MethodId, Object> entry : methodYield.valuesFor(target).entrySet()) {

                MethodId methodId = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof InvokeDefaultFunction) {
                    if (defaultMethods == null) {
                        defaultMethods = new HashSet<>();
                    }
                    defaultMethods.add(methodId.getMethod(target));
                } else {
                    fastValues.put(methodId.name(), value);
                }
            }
        }
        ProxyHandlerToValues proyHandler = new ProxyHandlerToValues(targets, fastValues);
        Object proxy = Proxy.newProxyInstance(classLoader, targets, proyHandler);
        if (defaultMethods != null) {
            proyHandler.initDefaultMethods(proxy, defaultMethods);
        }
        return proxy;
    }

    @Override
    public <I> @NonNull ReloadShell<I> generateShell(@NonNull ClassLoader classLoader, @NonNull Class<I> iface,
                                                     @NonNull Set<@NonNull MethodId> methods) {
        ProxyHandlerToDelegate<I> proxyHandler = new ProxyHandlerToDelegate<>();
        @SuppressWarnings("unchecked")
        I shell = (I) Proxy.newProxyInstance(classLoader, new Class[] {iface}, proxyHandler);
        return proxyHandler.new AsReloadShell(shell);
    }

    @Override
    public @NonNull Object generateEmpty(@NonNull ClassLoader classLoader, @NonNull Class<?> iface) {
        ProxyHandlerToEmpty proxyHandler = new ProxyHandlerToEmpty(iface);
        Object proxy = Proxy.newProxyInstance(classLoader, new Class[] {iface}, proxyHandler);
        proxyHandler.initProxy(proxy);
        return proxy;
    }

}
