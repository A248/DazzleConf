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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract class ProxyHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            if (method.getName().equals("equals")) {
                /*
                Equals implementation - identity equality.
                There is no better way to implement .equals in accordance with the reflexivity contract.
                */
                Object that = args[0];
                return proxy == that;
            }
            return invokeMethodOnSelf(method, args);
        }
        return implInvoke(method, args);
    }

    abstract Object implInvoke(Method method, Object[] args) throws Throwable;

    private Object invokeMethodOnSelf(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(this, args);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new AssertionError(ex);

        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause != null) {
                throw cause;
            } else {
                throw ex;
            }
        }
    }
}
