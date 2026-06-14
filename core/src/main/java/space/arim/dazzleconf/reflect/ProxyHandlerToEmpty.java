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

import space.arim.dazzleconf.DeveloperMistakeException;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Map;

final class ProxyHandlerToEmpty<I> extends ProxyHandler<I> {

    private final Map<Method, MethodHandle> defaultMethods;

    ProxyHandlerToEmpty(Class<I> iface, Map<Method, MethodHandle> defaultMethods) {
        super(iface);
        this.defaultMethods = defaultMethods;
    }

    @Override
    Object implInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodHandle methodHandle = defaultMethods.get(method);
        if (methodHandle == null) {
            if (method.isDefault()) {
                throw new IllegalStateException();
            } else {
                throw new DeveloperMistakeException("Cannot call non-default configuration methods pre-initialization");
            }
        }
        return methodHandle.invokeExact(proxy, args);
    }

    @Override
    boolean implEquals(Object ourProxy, Object otherProxy, ProxyHandler<?> otherHandler) {
        if (otherHandler instanceof ProxyHandlerToValues || otherHandler instanceof ProxyHandlerToDelegate) {
            // Invert direction => Let the other instance implement equality
            return otherHandler.implEquals(otherProxy, ourProxy, this);
        }
        if (otherHandler instanceof ProxyHandlerToEmpty) {
            return true;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    int implHashCode() {
        return iface.hashCode();
    }

    @Override
    void implToString(StringBuilder output) {}
}
