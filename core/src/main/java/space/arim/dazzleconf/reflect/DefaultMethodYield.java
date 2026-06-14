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
import space.arim.dazzleconf.internals.ImmutableCollections;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class DefaultMethodYield implements MethodYield {

    private final DefaultReflectionProvider<?> reflectProvider;
    private final HashMap<String, Object> dataValues;
    /**
     * Methods whose default implementation to call. This works because java.lang.reflect.Proxy always passes the
     * overidden Method object, so keys match.
     */
    private final HashMap<Method, MethodHandle> implMethods;
    /**
     * Methods with multiple parameters, but returnValue() was used with them.
     * To satisfy the Instantiator contract, we must implement equality using the fixed return values.
     */
    private final List<Map.Entry<MethodId, Object>> multiParamFixedMethods;

    private boolean consumed;

    private DefaultMethodYield(DefaultReflectionProvider<?> reflectProvider, HashMap<String, Object> dataValues, HashMap<Method, MethodHandle> implMethods, List<Map.Entry<MethodId, Object>> multiParamFixedMethods) {
        this.reflectProvider = reflectProvider;
        this.dataValues = dataValues;
        this.implMethods = implMethods;
        this.multiParamFixedMethods = multiParamFixedMethods;
    }

    DefaultMethodYield(DefaultReflectionProvider<?> reflectProvider) {
        this(reflectProvider, new HashMap<>(), new HashMap<>(), new ArrayList<>());
    }

    <I> ProxyHandlerToValues<I> intoProxyHandler(Class<I> iface) {
        consumed = true;
        // Using the same data structures is safe, since consumed = true prevents modifications
        return new ProxyHandlerToValues<>(iface, dataValues, implMethods, multiParamFixedMethods);
    }

    private void checkNotConsumed() {
        if (consumed) {
            throw new IllegalStateException("consumed previously");
        }
    }

    private class ForImpl implements ForImplementable {

        private boolean closed;

        private void checkNotClosed() {
            if (closed) {
                throw new IllegalStateException("closed previously");
            }
            checkNotConsumed();
        }

        private IllegalStateException instructionAlreadySet(MethodId methodId) {
            throw new IllegalStateException("An instruction was already set for " + methodId);
        }

        @Override
        public void returnValue(@NonNull MethodId methodId, @NonNull Object value) {
            Objects.requireNonNull(methodId, "methodId");
            Objects.requireNonNull(value, "value");
            checkNotClosed();

            Method methodObj = DefaultReflectionProvider.getMethodFromCache(methodId);
            if (methodId.parameterCount() == 0) {
                if (implMethods.containsKey(methodObj)) {
                    throw instructionAlreadySet(methodId);
                }
                String dataKey = methodId.name();
                Object previous = dataValues.putIfAbsent(dataKey, value);
                if (previous != null) {
                    throw instructionAlreadySet(methodId);
                }
            } else {
                // Implement method(receiver, unused params...) -> fixed value
                MethodHandle methodHandle = MethodHandles.constant(Object.class, value);
                methodHandle = MethodHandles.dropArguments(methodHandle, 0, Object.class, Object[].class);
                assert methodHandle.type().equals(DefaultReflectionProvider.IMPL_METHOD_SIGNATURE);

                MethodHandle previousHandle = implMethods.putIfAbsent(methodObj, methodHandle);
                if (previousHandle != null) {
                    throw instructionAlreadySet(methodId);
                }
                multiParamFixedMethods.add(ImmutableCollections.mapEntryOf(methodId, value));
            }
        }

        @Override
        public void callDefault(@NonNull MethodId methodId) {
            Objects.requireNonNull(methodId, "methodId");
            checkNotClosed();

            Method methodObj = DefaultReflectionProvider.getMethodFromCache(methodId);
            if (!methodObj.isDefault()) {
                throw new IllegalArgumentException("Method is not default: " + methodId);
            }
            if (methodId.parameterCount() == 0) {
                Object previousDataValue = dataValues.get(methodId.name());
                if (previousDataValue != null) {
                    throw instructionAlreadySet(methodId);
                }
            }
            MethodHandle previousHandle = implMethods.putIfAbsent(methodObj, reflectProvider.genDefaultHandle(methodObj));
            if (previousHandle != null) {
                throw instructionAlreadySet(methodId);
            }
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    @Override
    public @NonNull ForImplementable forImplementable(@NonNull Class<?> implementable) {
        checkNotConsumed();
        return new ForImpl();
    }

    @Override
    public void clear() {
        checkNotConsumed();
        dataValues.clear();
        implMethods.clear();
    }

    @Override
    public @NonNull MethodYield copy() {
        checkNotConsumed();
        return new DefaultMethodYield(
                reflectProvider,
                new HashMap<>(dataValues), new HashMap<>(implMethods), new ArrayList<>(multiParamFixedMethods)
        );
    }

    @Override
    public void close() {
        consumed = true;
    }

    @Override
    public String toString() {
        return getClass().getName() + '{' +
                "dataValues=" + dataValues +
                ", implMethods=" + implMethods +
                ", multiParamFixedMethods=" + multiParamFixedMethods +
                ", consumed=" + consumed +
                '}';
    }

}
