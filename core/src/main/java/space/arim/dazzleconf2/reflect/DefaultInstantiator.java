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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link Instantiator} using standard proxy reflection
 *
 */
final class DefaultInstantiator implements Instantiator {

    private final MethodHandles.Lookup lookup;

    DefaultInstantiator(MethodHandles.Lookup lookup) {
        this.lookup = Objects.requireNonNull(lookup);
    }

    @Override
    public boolean hasProduced(@NonNull Object instance) {
        return Proxy.isProxyClass(instance.getClass()) && Proxy.getInvocationHandler(instance) instanceof ProxyHandler;
    }

    private <I> @NonNull I produceProxy(ProxyHandler<I> proxyHandler) {
        Class<I> iface = proxyHandler.iface;
        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), new Class[] {iface}, proxyHandler));
    }

    @Override
    public <I> @NonNull I generate(@NonNull Class<I> iface, @NonNull MethodYield methodYield) {

        ProxyHandlerToValues<I> proxyHandler = new ProxyHandlerToValues<>(iface);
        I proxy = produceProxy(proxyHandler);

        int methodCount = methodYield.sizeEstimate();
        // Use lower load factors for faster retrieval (1/4 for fastValues, 1/2 for defaultMethodMap)
        Map<String, Object> fastValues = new HashMap<>((int) (methodCount / 0.24f), 0.26f);
        DefaultMethodMap defaultMethodMap = new DefaultMethodMap(new HashMap<>(methodCount, 0.5f));

        for (MethodYield.Entry entry : methodYield.entries()) {
            MethodId methodId = entry.method();
            Object value = entry.returnValue();

            if (value instanceof InvokeDefaultFunction) {
                MethodId.OpaqueCache methodCache = methodId.getOpaqueCache();
                assert methodCache instanceof MethodCache;
                defaultMethodMap.addMethod(lookup, ((MethodCache) methodCache).method, proxy);
            } else {
                fastValues.put(methodId.name(), value);
            }
        }
        proxyHandler.init(fastValues, defaultMethodMap);
        return proxy;
    }

    @Override
    public <I> @NonNull ReloadShell<I> generateShell(@NonNull Class<I> iface) {
        ProxyHandlerToDelegate<I> proxyHandler = new ProxyHandlerToDelegate<>(iface, lookup);
        I shell = produceProxy(proxyHandler);
        return proxyHandler.new AsReloadShell(shell);
    }

    @Override
    public <I> @NonNull I generateEmpty(@NonNull Class<I> iface) {
        ProxyHandlerToEmpty<I> proxyHandler = new ProxyHandlerToEmpty<>(iface);
        I proxy = produceProxy(proxyHandler);

        DefaultMethodMap defaultMethodMap = new DefaultMethodMap(new HashMap<>());
        for (Method method : iface.getMethods()) {
            if (method.isDefault()) {
                defaultMethodMap.addMethod(lookup, method, proxy);
            }
        }
        proxyHandler.init(defaultMethodMap);

        return proxy;
    }

}
