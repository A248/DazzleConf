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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * The default implementation of {@link ReflectionService}.
 * <p>
 * It uses standard reflection to find and call methods, and {@code java.lang.reflect.Proxy} to generate classes
 * implementing a configuration interface. Only interface types are supported.
 */
public final class DefaultReflectionService implements ReflectionService {

    /**
     * Creates the service
     */
    public DefaultReflectionService() {}

    @Override
    public @NonNull <I> ReflectionProvider<I> newProvider(Class<I> type, MethodHandles.@NonNull Lookup lookup) {
        Objects.requireNonNull(lookup, "lookup");
        if (!type.isInterface()) {
            throw new UnsupportedOperationException(
                    "This library's main implementation works exclusively with interfaces. " +
                            type + " is not an interface."
            );
        }
        return new DefaultReflectionProvider<>(type, lookup);
    }

    @Override
    public boolean hasProduced(@NonNull Object instance) {
        return Proxy.isProxyClass(instance.getClass()) && Proxy.getInvocationHandler(instance) instanceof ProxyHandler;
    }
}
