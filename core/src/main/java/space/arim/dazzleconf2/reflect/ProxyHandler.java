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
abstract class ProxyHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            switch (method.getName()) {
                case "equals":
                    Object other = args[0];
                    InvocationHandler otherHandler;
                    return other == proxy || Proxy.isProxyClass(other.getClass())
                            && (otherHandler = Proxy.getInvocationHandler(other)) instanceof ProxyHandler
                            && implEquals(proxy, other, (ProxyHandler) otherHandler);
                case "hashCode":
                    return implHashCode();
                case "toString":
                    StringBuilder output = new StringBuilder();
                    output.append(getClass().getSimpleName());
                    output.append("{ ");
                    implToString(output);
                    output.append(" }");
                    return output.toString();
                default:
                    // Breaks the contract of java.lang.reflect.Proxy
                    return null;
            }
        }
        return implInvoke(method, args);
    }

    abstract Object implInvoke(Method method, Object[] args) throws Throwable;

    abstract boolean implEquals(Object proxy, Object other, ProxyHandler otherHandler);

    abstract int implHashCode();

    abstract void implToString(StringBuilder output);

}
