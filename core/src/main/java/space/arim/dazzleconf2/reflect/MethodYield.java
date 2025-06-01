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

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.internals.ImmutableCollections;

import java.util.HashMap;
import java.util.Map;

/**
 * Bank of values yielded when calling an instantiated proxy.
 * <p>
 * This class is mutable. It is designed to be passed from its producer to its consumer.
 */
public final class MethodYield {

    private final @NonNull Map<Class<?>, Map<MethodId, Object>> backing;

    /**
     * Creates an empty yield.
     * <p>
     * Values can be added by calling {@link #addValue(Class, MethodId, Object)}.
     */
    public MethodYield() {
        this.backing = new HashMap<>();
    }

    /**
     * Creates a method yield that is identical to the provided instance.
     * <p>
     * Mutating this {@code MethodYield}, such as by adding or clearing values, will not affect the argument.
     *
     * @param copyFrom the method yield from which to copy
     */
    public MethodYield(@NonNull MethodYield copyFrom) {
        this.backing = new HashMap<>(copyFrom.backing);
    }

    /**
     * Modifies this builder, adding a yielded value as given
     *
     * @param implementable the interface being implemented
     * @param method        a method within that interface
     * @param value         the value to supply
     */
    public void addValue(@NonNull Class<?> implementable, @NonNull MethodId method, @NonNull Object value) {
        backing.computeIfAbsent(implementable, (k) -> new HashMap<>()).put(method, value);
    }

    /**
     * Clears all added values and starts over again
     */
    public void clearValues() {
        backing.clear();
    }

    /**
     * Gets yieldable values for the given implementable interface
     *
     * @param implementable the interface being implemented
     * @return the values for method calls on that interface's methods, immutable
     */
    public Map<@NonNull MethodId, @NonNull Object> valuesFor(@NonNull Class<?> implementable) {
        return backing.getOrDefault(implementable, ImmutableCollections.emptyMap());
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
