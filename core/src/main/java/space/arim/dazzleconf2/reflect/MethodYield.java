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

    /// To make iteration simpler, we avoid adding empty submaps
    private final @NonNull Map<Class<?>, Map<MethodId, Object>> backing;
    private transient int sizeEstimate;
    /// The current type for which a ForImplementable exists
    private transient Class<?> currentImplementable;
    private static final InvokeDefaultFunction INVOKE_DEFAULT_FUNCTION = new InvokeDefaultFunction();

    /**
     * Creates an empty yield.
     */
    public MethodYield() {
        this(new HashMap<>());
    }

    private MethodYield(Map<Class<?>, Map<MethodId, Object>> backing) {
        this.backing = backing;
    }

    /**
     * Begins adding yield values for the given interface.
     * <p>
     * The returned {@link ForImplementable} must be used, and closed, before this method can be called again.
     * Additionally, this method cannot be used more than once with the same {@code implementable} argument.
     *
     * @param implementable the interface being implemented
     * @return a handler to attach return values for it
     */
    public ForImplementable forImplementable(@NonNull Class<?> implementable) {
        Objects.requireNonNull(implementable, "implementable");
        if (backing.containsKey(implementable)) {
            throw new IllegalStateException("Called already for " + implementable);
        }
        if (currentImplementable != null) {
            throw new IllegalStateException("Existing handler must be closed");
        }
        currentImplementable = implementable;
        return new ForImplementable();
    }

    /**
     * Builder of return value instructions, given an implementable class. When the caller is finished calling methods
     * to add return values, they must close this object.
     *
     */
    public final class ForImplementable implements AutoCloseable {

        private final Map<MethodId, Object> methodMap = new HashMap<>();

        /**
         * Specifies to return the given value. If the given method already has an instruction, it is replaced.
         * <p>
         * If the given method is a covariant override of a method in a parent class, this function should be called
         * twice, with appropriately different {@code MethodId}s.
         *
         * @param method        a method within that interface
         * @param value         the value to return
         */
        public void returnValue(@NonNull MethodId method, @NonNull Object value) {
            Objects.requireNonNull(method, "method");
            Objects.requireNonNull(value, "value");
            methodMap.put(method, value);
        }

        /**
         * Specifies to invoke the default implementation. If the given method already has an instruction, it is
         * replaced.
         * <p>
         * If the given method exists in the current implementable class but was also overidden by a subclass,
         * this method should be used for both classes.
         *
         * @param method        a method within that interface
         */
        public void callDefaultImpl(@NonNull MethodId method) {
            Objects.requireNonNull(method, "method");
            methodMap.put(method, INVOKE_DEFAULT_FUNCTION);
        }

        @Override
        public void close() {
            if (currentImplementable == null) {
                return; // Closed already
            }
            if (!methodMap.isEmpty()) {
                backing.put(currentImplementable, methodMap);
                sizeEstimate += methodMap.size();
            }
            currentImplementable = null;
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
     * Gets all entries added. Each entry represents a single return value instruction
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
