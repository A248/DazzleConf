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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class ProxyAgentToDelegate<I> extends ProxyAgent {

    private volatile I delegate;

    @Override
    Object implInvoke(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(delegate, args);
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

    class AsReloadShell implements ReloadShell<I> {

        private final I shell;

        AsReloadShell(I shell) {
            this.shell = shell;
        }

        @Override
        public void setCurrentDelegate(I delegate) {
            ProxyAgentToDelegate.this.delegate = delegate;
        }

        @Override
        public I getCurrentDelegate() {
            return delegate;
        }

        @Override
        public I getShell() {
            return shell;
        }
    }
}
