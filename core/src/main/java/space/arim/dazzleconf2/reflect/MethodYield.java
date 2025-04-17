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

import space.arim.dazzleconf.internal.util.ImmutableCollections;

import java.util.HashMap;
import java.util.Map;

/**
 * Bank of values yielded when calling an instantiated proxy
 */
public final class MethodYield {

    private final Map<Class<?>, Map<MethodId, Object>> backing;

    MethodYield(Map<Class<?>, Map<MethodId, Object>> backing) {
        Map<Class<?>, Map<MethodId, Object>> copied = new HashMap<>(
                (int) (backing.size() / 0.98f), 0.999f
        );
        for (Map.Entry<Class<?>, Map<MethodId, Object>> entry : backing.entrySet()) {
            copied.put(entry.getKey(), ImmutableCollections.mapOf(entry.getValue()));
        }
        this.backing = ImmutableCollections.mapOf(copied);
    }

    /**
     * Gets yieldable values for the given implementable interface
     *
     * @param implementable the interface being implemented
     * @return the values for method calls on that interface's methods, immutable
     */
    public Map<MethodId, Object> valuesFor(Class<?> implementable) {
        return backing.getOrDefault(implementable, ImmutableCollections.emptyMap());
    }

    /**
     * Builder for method yield
     */
    public static final class Builder {

        private final Map<Class<?>, Map<MethodId, Object>> backing = new HashMap<>();

        /**
         * Modifies this builder, adding a yielded value as given
         *
         * @param implementable the interface being implemented
         * @param method a method within that interface
         * @param value the value to supply
         * @return this builder
         */
        public Builder addValue(Class<?> implementable, MethodId method, Object value) {
            backing.computeIfAbsent(implementable, (k) -> new HashMap<>());
            return this;
        }

        /**
         * Clears all added values and starts over again
         *
         * @return this builder
         */
        public Builder clearValues() {
            backing.clear();
            return this;
        }

        /**
         * Finishes construction
         * @return the built method yield
         */
        public MethodYield build() {
            return new MethodYield(backing);
        }
    }
}
