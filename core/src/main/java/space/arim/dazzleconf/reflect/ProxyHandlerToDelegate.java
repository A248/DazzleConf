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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.ReloadShell;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

final class ProxyHandlerToDelegate<I> extends ProxyHandler<I> {

    private volatile I delegate;
    private final MethodHandles.Lookup lookup;
    private boolean needLookup;
    private final ReflectHandleCache reflectHandleCache;

    ProxyHandlerToDelegate(Class<I> iface, MethodHandles.Lookup lookup, ReflectHandleCache reflectHandleCache) {
        super(iface);
        this.lookup = lookup;
        this.reflectHandleCache = reflectHandleCache;
    }

    @Override
    Object implInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        I delegate = this.delegate;
        if (delegate == null) {
            throw new NullPointerException("delegate");
        }
        if (!needLookup) {
            try {
                return method.invoke(delegate, args);
            } catch (IllegalAccessException ex) {
                needLookup = true; // Data race okay

            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause();
                if (cause != null) {
                    throw cause;
                } else {
                    throw ex;
                }
            }
        }
        MethodHandle methodHandle = reflectHandleCache.forType(lookup, method.getDeclaringClass())
                .getOrComputeHandle(method);
        return methodHandle.invokeExact((Object) delegate, args);
    }

    @Override
    boolean implEquals(Object ourProxy, Object otherProxy, ProxyHandler<?> otherHandler) {
        I delegate = this.delegate;
        if (otherHandler instanceof ProxyHandlerToDelegate) {
            ProxyHandlerToDelegate<?> that = (ProxyHandlerToDelegate<?>) otherHandler;
            return Objects.equals(delegate, that.delegate);
        }
        return delegate != null && delegate.equals(otherProxy);
    }

    @Override
    int implHashCode() {
        return Objects.hashCode(delegate);
    }

    @Override
    void implToString(StringBuilder output) {
        output.append(',').append("delegate").append('=').append(delegate);
    }

    class AsReloadShell implements ReloadShell<I> {

        private final I shell;

        AsReloadShell(I shell) {
            this.shell = shell;
        }

        @Override
        public void setCurrentDelegate(@Nullable I delegate) {
            checkNotShell(delegate);
            ProxyHandlerToDelegate.this.delegate = delegate;
        }

        // Nothing we can do to stop race conditions, or cicles with other ReloadShell implementations
        private void checkNotShell(Object arg) {
            if (arg == null) {
                return;
            }
            if (shell == arg) {
                throw new IllegalArgumentException("Cannot set the delegate to the shell itself, or circularly");
            }
            if (Proxy.isProxyClass(arg.getClass())) {
                InvocationHandler argHandler = Proxy.getInvocationHandler(arg);
                if (argHandler instanceof ProxyHandlerToDelegate) {
                    checkNotShell(((ProxyHandlerToDelegate<?>) argHandler).delegate);
                }
            }
        }

        @Override
        public @Nullable I getCurrentDelegate() {
            return delegate;
        }

        @Override
        public @NonNull I getShell() {
            return shell;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '{' + getCurrentDelegate() + '}';
        }
    }
}
