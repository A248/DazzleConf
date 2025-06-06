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

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.internals.ImmutableCollections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Bank of values yielded when calling an instantiated proxy.
 * <p>
 * This class is mutable: it is designed to be passed from its producer to its consumer. If defensive copies are
 * needed, see {@link #copy()}.
 */
@API(status = API.Status.MAINTAINED)
public final class MethodYield {

    private final @NonNull Map<Class<?>, Map<MethodId, Object>> backing;
    private transient int sizeEstimate;

    /**
     * Creates an empty yield.
     * <p>
     * Values can be added by calling {@link #addEntry(Class, MethodId, Object)}.
     */
    public MethodYield() {
        this(new HashMap<>());
    }

    private MethodYield(Map<Class<?>, Map<MethodId, Object>> backing) {
        this.backing = backing;
    }

    /**
     * Modifies this builder, adding a yielded value as given.
     * <p>
     * If the given method already has a value set for it, it is replaced.
     * <p>
     * If the given method exists in {@code implementable} but was overidden by a subclass, the subclass should be
     * passed instead.
     *
     * @param implementable the interface being implemented
     * @param method        a method within that interface
     * @param value         the value to return, or {@link InvokeDefaultFunction} to call the default implementation
     */
    public void addEntry(@NonNull Class<?> implementable, @NonNull MethodId method, @NonNull Object value) {
        Objects.requireNonNull(implementable, "implementable");
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(value, "value");

        Map<MethodId, Object> methodMap = backing.computeIfAbsent(implementable, (k) -> new HashMap<>());
        if (methodMap.put(method, value) == null) {
            sizeEstimate++;
        }
    }

    /**
     * Clears all added values and starts over again
     */
    public void clear() {
        backing.clear();
        sizeEstimate = 0;
    }

    /**
     * Gets the yieldable values
     *
     * @param implementable the interface being implemented
     * @return the values for method calls on that interface's methods, immutable
     */
    public Map<@NonNull MethodId, @NonNull Object> valuesFor(@NonNull Class<?> implementable) {
        return backing.getOrDefault(implementable, ImmutableCollections.emptyMap());
    }

    /**
     * Gets all entries added with {@link #addEntry(Class, MethodId, Object)}
     *
     * @return the iterable entries
     */
    public @NonNull Iterable<@NonNull Entry> entries() {
        return new Iterable<Entry>() {
            @Override
            public @NonNull Iterator<Entry> iterator() {
                return new Iter(backing.entrySet().iterator());
            }

            @Override
            public void forEach(Consumer<? super Entry> action) {
                backing.forEach((implementable, methodMap) -> {
                    methodMap.entrySet().forEach(methodAndValue -> {
                        action.accept(new Entry(implementable, methodAndValue));
                    });
                });
            }
        };
    }

    /**
     * Gets the estimated number of entries
     *
     * @return the estimated size of this {@code MethodYield}
     */
    public int sizeEstimate() {
        return sizeEstimate;
    }

    /**
     * An entry added to the method yield
     *
     */
    public static final class Entry {

        private final Class<?> implementable;
        private final Map.Entry<MethodId, Object> methodAndValue;

        Entry(Class<?> implementable, Map.Entry<MethodId, Object> methodAndValue) {
            this.implementable = implementable;
            this.methodAndValue = methodAndValue;
        }

        /**
         * Gets the interface being implemented
         *
         * @return the interface
         */
        public @NonNull Class<?> implementable() {
            return implementable;
        }

        /**
         * Gets the method
         *
         * @return the method
         */
        public @NonNull MethodId method() {
            return methodAndValue.getKey();
        }

        /**
         * Gets the return value for the method, or {@link InvokeDefaultFunction} to call the default implementation
         *
         * @return the return value; primitive values will be boxed
         */
        public @NonNull Object returnValue() {
            return methodAndValue.getValue();
        }
    }

    private static final class Iter implements Iterator<Entry> {

        private final Iterator<Map.Entry<Class<?>, Map<MethodId, Object>>> backing;
        private Class<?> currentType;
        private Iterator<Map.Entry<MethodId, Object>> currentMap;

        private Iter(Iterator<Map.Entry<Class<?>, Map<MethodId, Object>>> backing) {
            this.backing = backing;
        }

        @Override
        public boolean hasNext() {
            return (currentMap != null && currentMap.hasNext()) || backing.hasNext();
        }

        @Override
        public Entry next() {
            if (currentMap == null || !currentMap.hasNext()) {
                Map.Entry<Class<?>, Map<MethodId, Object>> nextMap = backing.next();
                currentType = nextMap.getKey();
                currentMap = nextMap.getValue().entrySet().iterator();
            }
            return new Entry(currentType, currentMap.next());
        }
    }

    /**
     * Returns a deep copy of this method yield's contents.
     * <p>
     * Mutating this {@code MethodYield}, such as by adding or clearing values, will not affect the copy; likewise
     * mutating the copy will not affect this instance.
     *
     * @return a copy of this method yield's contents
     */
    public @NonNull MethodYield copy() {
        Map<Class<?>, Map<MethodId, Object>> copyBacking = new HashMap<>(this.backing.size());
        backing.forEach((clazz, values) -> {
            copyBacking.put(clazz, new HashMap<>(values));
        });
        MethodYield copy = new MethodYield(copyBacking);
        copy.sizeEstimate = sizeEstimate;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MethodYield)) return false;

        MethodYield that = (MethodYield) o;
        return backing.equals(that.backing);
    }

    @Override
    public int hashCode() {
        return backing.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + backing + '}';
    }
}
