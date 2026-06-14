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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

final class ReflectHandleCache {

    private final ConcurrentMap<Class<?>, AtomicReference<Map<Method, MethodHandle>>> handleCache = new ConcurrentHashMap<>();

    ForType forType(MethodHandles.Lookup lookup, Class<?> enclosingType) {
        return new ForType(
                lookup,
                handleCache.computeIfAbsent(enclosingType, (cls) -> new AtomicReference<>(new HashMap<>()))
        );
    }

    static final class ForType {

        private final MethodHandles.Lookup lookup;
        private final AtomicReference<Map<Method, MethodHandle>> handleMap;

        private ForType(MethodHandles.Lookup lookup, AtomicReference<Map<Method, MethodHandle>> handleMap) {
            this.lookup = lookup;
            this.handleMap = handleMap;
        }

        MethodHandle getOrComputeHandle(Method method) {
            MethodHandle methodHandle = handleMap.get().get(method);
            if (methodHandle != null) {
                return methodHandle;
            }
            assert !Modifier.isStatic(method.getModifiers());
            MethodHandle computeHandle;
            try {
                computeHandle = lookup.unreflect(method);
            } catch (IllegalAccessException ex) {
                throw new DeveloperMistakeException("Configuration method inaccessible", ex);
            }
            computeHandle = computeHandle.asSpreader(Object[].class, method.getParameterCount());
            computeHandle = computeHandle.asType(MethodType.methodType(Object.class, new Class[] {Object.class, Object[].class}));
            // Use an optimistic update to install the map entry
            while (true) {
                Map<Method, MethodHandle> currentMap = handleMap.get();
                Map<Method, MethodHandle> newMap = new HashMap<>(currentMap);
                MethodHandle concurrentCompute = newMap.putIfAbsent(method, computeHandle);
                if (concurrentCompute != null) {
                    computeHandle = concurrentCompute;
                    break;
                }
                if (handleMap.compareAndSet(currentMap, newMap)) {
                    break;
                }
            }
            return computeHandle;
        }
    }
}
