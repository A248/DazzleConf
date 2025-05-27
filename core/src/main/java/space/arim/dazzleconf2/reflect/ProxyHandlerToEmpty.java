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

import space.arim.dazzleconf2.internals.MethodUtil;
import space.arim.dazzleconf2.DeveloperMistakeException;

import java.lang.reflect.Method;

final class ProxyHandlerToEmpty extends ProxyHandler {

    private final Class<?> iface;
    private Object proxy;

    ProxyHandlerToEmpty(Class<?> iface) {
        this.iface = iface;
    }

    void initProxy(Object proxy) {
        this.proxy = proxy;
    }

    @Override
    Object implInvoke(Method method, Object[] args) throws Throwable {
        if (proxy == null) {
            throw new IllegalStateException("initProxy not called");
        }
        if (!MethodUtil.isDefault(method)) {
            throw new DeveloperMistakeException("Cannot call non-default configuration methods pre-initialization");
        }
        return MethodUtil.createDefaultMethodHandle(method).bindTo(proxy).invokeWithArguments(args);
    }

    @Override
    boolean implEquals(Object ourProxy, Object otherProxy, ProxyHandler otherHandler) {
        if (otherHandler instanceof ProxyHandlerToValues) {
            // We're never equal - we can never know if we implement the same interfaces
            return false;
        }
        if (otherHandler instanceof ProxyHandlerToDelegate) {
            // Invert direction => unwrap the delegate
            return otherHandler.implEquals(otherProxy, ourProxy, this);
        }
        if (otherHandler instanceof ProxyHandlerToEmpty) {
            return iface.equals(((ProxyHandlerToEmpty) otherHandler).iface);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    int implHashCode() {
        return iface.hashCode();
    }

    @Override
    void implToString(StringBuilder output) {
        output.append(iface.getName());
    }
}
