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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Base interface for proxying.
 * <p>
 * Subclasses <b>MUST</b> be aware of each other and implement equality accordingly!
 *
 */
abstract class ProxyHandler<I> implements InvocationHandler {

    final Class<I> iface;

    ProxyHandler(Class<I> iface) {
        this.iface = iface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            switch (method.getName()) {
                case "equals":
                    Object other = args[0];
                    InvocationHandler otherHandler;
                    ProxyHandler<?> otherProxyHandler;
                    return other == proxy || other != null
                            && Proxy.isProxyClass(other.getClass())
                            && (otherHandler = Proxy.getInvocationHandler(other)) instanceof ProxyHandler
                            && (otherProxyHandler = (ProxyHandler<?>) otherHandler).iface.equals(iface)
                            && implEquals(proxy, other, otherProxyHandler);
                case "hashCode":
                    return implHashCode();
                case "toString":
                    StringBuilder output = new StringBuilder();
                    output.append(getClass().getSimpleName());
                    output.append('{');
                    output.append("interface");
                    output.append('=');
                    output.append(iface.getName());
                    implToString(output);
                    output.append('}');
                    return output.toString();
                default:
                    // Breaks the contract of java.lang.reflect.Proxy
                    assert false : "Bad proxy; broken caller contract";
                    return null;
            }
        }
        return implInvoke(method, args);
    }

    Object fastPathNoParams(String methodName) {
        return null;
    }

    abstract Object implInvoke(Method method, Object[] args) throws Throwable;

    abstract boolean implEquals(Object proxy, Object other, ProxyHandler<?> otherHandler);

    abstract int implHashCode();

    abstract void implToString(StringBuilder output);

}
