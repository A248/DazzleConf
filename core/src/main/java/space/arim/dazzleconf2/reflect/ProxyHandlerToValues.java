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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Map;

final class ProxyHandlerToValues<I> extends ProxyHandler<I> {

    private Map<String, Object> fastValues;
    private DefaultMethodMap defaultMethodMap;

    ProxyHandlerToValues(Class<I> iface) {
        super(iface);
    }

    void init(Map<String, Object> fastValues, DefaultMethodMap defaultMethodMap) {
        this.fastValues = fastValues;
        this.defaultMethodMap = defaultMethodMap;
    }

    @Override
    Object fastPathNoParams(String methodName) {
        return fastValues.get(methodName);
    }

    @Override
    Object implInvoke(Method method, Object[] args) throws Throwable {
        Object fastValue;
        if (method.getParameterCount() == 0 && (fastValue = fastValues.get(method.getName())) != null) {
            return fastValue;
        }
        MethodHandle methodHandle = defaultMethodMap.getHandleWithBoundReceiver(method);
        // We shouldn't receive a null handle. If we do, it means the method yield we were provided was lacking
        // We could throw an exception, but that might slow down the compiled method. So just use an assert.
        assert methodHandle != null : "Bad proxy; incomplete data";
        return methodHandle.invokeWithArguments(args);
    }

    @Override
    boolean implEquals(Object ourProxy, Object otherProxy, ProxyHandler<?> otherHandler) {
        if (otherHandler instanceof ProxyHandlerToValues) {
            ProxyHandlerToValues<?> that = (ProxyHandlerToValues<?>) otherHandler;
            // No need to add defaultMethodsMap: it's the mirror of whatever is left out of fastValues
            return fastValues.equals(that.fastValues);
        }
        if (otherHandler instanceof ProxyHandlerToDelegate) {
            // Invert direction => unwrap the delegate
            return otherHandler.implEquals(otherProxy, ourProxy, this);
        }
        if (otherHandler instanceof ProxyHandlerToEmpty) {
            // Only possibility is that all methods are default, see Instantiator javadoc
            return fastValues.isEmpty();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    int implHashCode() {
        // IMPORTANT - See equality contract
        // If fastValues.isEmpty(), make sure that iface.hashCode() is the result
        int result = fastValues.hashCode();
        result = 31 * result + iface.hashCode();
        return result;
    }

    @Override
    void implToString(StringBuilder output) {
        output.append(',').append("values").append('=').append(fastValues);
    }
}
