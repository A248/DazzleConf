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

package space.arim.dazzleconf2.backend;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.SerializeOutput;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A tree of in-memory configuration data. This tree is essentially a map of keys to values representing in-memory
 * configuration data, with added metadata of line numbers and comments.
 * <p>
 * <b>Interfacing and order</b>
 * <p>
 * A data tree is read from and written to configuration backend. As such, it uses the keys found in the backend data,
 * and it does <b>NOT</b> take into account method names or {@link KeyMapper}. It is highly recommend to use
 * <code>KeyMapper</code> where appropriate to interface with key strings.
 * <p>
 * Additionally, a data tree maintains an order which is reflected in iteration operations. If created immutably, this
 * order is fixed at creation. If built mutably, the order will be the insertion order of the elements.
 * <p>
 * <b>Keys and values</b>
 * <p>
 * Keys are represented as <code>Object</code> and must be one of the canonical types (excl. lists or nested trees).
 * Values are wrapped by {@link DataEntry}, but are also <code>Object</code> and must be one of the canonical types.
 * <p>
 * Canonical types:<ul>
 * <li>String
 * <li>primitives represented by their boxed types
 * <li>DataTree for nesting
 * <li>Lists of the above types. The List implementation must be immutable.
 * </ul>
 * Recall that keys <b>cannot</b> be DataTree or List. These requirements are enforced at runtime, and they can be
 * checked using {@link #validateKey(Object)} and {@link #validateValue(Object)}.
 * <p>
 * Mutability of this class is <b>not defined</b>. Please use {@link DataTree.Immut} or {@link DataTree.Mut} if you need
 * guaranteed mutable or immutable versions, or see the package javadoc for more information on the mutability model we use.
 *
 */
public abstract class DataTree implements DataStreamable {

    @NonNull LinkedHashMap<Object, DataEntry> data;

    DataTree(LinkedHashMap<Object, DataEntry> data) {
        this.data = data;
    }

    /**
     * Gets the entry at the specified key, or null if unset.
     * <p>
     * If accessing keys based on method names, it is strongly recommended to use the <code>KeyMapper</code> to map
     * method names to keys. See {@link DeserializeInput#keyMapper()} for deserialization or
     * {@link SerializeOutput#keyMapper()} for serialization.
     *
     * @param key the key
     * @return the entry
     */
    public @Nullable DataEntry get(@NonNull Object key) {
        return data.get(key);
    }

    /**
     * Gets all the keys used in this tree.
     * <p>
     * The returned collection is a mutable copy; it is unaffected by the mutability of this {@code DataTree}, and
     * concurrent updates to this tree will not affect it either.
     *
     * @return the key set, a mutable copy
     */
    public @NonNull Collection<@NonNull Object> getKeys() {
        return new LinkedHashSet<>(data.keySet());
    }

    /**
     * Runs an action for each key/value pair.
     * <p>
     * Iteration maintains the order with which this data tree was created.
     *
     * @param action the action
     */
    public void forEach(BiConsumer<? super @NonNull Object, ? super @NonNull DataEntry> action) {
        data.forEach(action);
    }

    /**
     * Gets this data tree as a mutable one.
     * <p>
     * If not mutable, the data is copied to a new tree which is returned. This copying may be performed lazily, such
     * as by deferring to the first mutative operation on the returned object.
     *
     * @return this tree if mutable, or a mutable copy if needed
     */
    public abstract DataTree.@NonNull Mut intoMut();

    /**
     * Gets this data tree as an immutable one.
     * <p>
     * The data contained within this {@code DataTree} is evacuated and moved into a new instance. After the call, the
     * old instance (this object) is poisoned and must not be used.
     * <p>
     * If this instance is already {@code DataTree.Immut}, then it may be returned without changes.
     *
     * @return an immutable data tree
     */
    public abstract DataTree.@NonNull Immut intoImmut();

    /**
     * Checks whether the given object is valid as a value in the data tree. Values must be one of primitive,
     * <code>String</code>, <code>List</code> with valid elements, or <code>DataTree</code>. Null values are not valid.
     *
     * @param value the value
     * @return true if a valid canonical value, false if not
     */
    public static boolean validateValue(@Nullable Object value) {
        if (value instanceof List) {
            for (Object elem : (List<?>) value) {
                if (!validateValue(elem)) {
                    return false;
                }
            }
            return true;
        }
        return value instanceof DataTree || validateKey(value);
    }

    /**
     * Checks whether the given object is valid as a key in the data tree. Keys must be either primitive or
     * <code>String</code>. Null values are not accepted as keys.
     *
     * @param value the value
     * @return true if a valid canonical key, false if not
     */
    public static boolean validateKey(@Nullable Object value) {
        return value instanceof String
                || value instanceof Boolean || value instanceof Byte || value instanceof Character
                || value instanceof Short || value instanceof Integer || value instanceof Long
                || value instanceof Float || value instanceof Double;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof DataTree)) return false;

        DataTree dataTree = (DataTree) o;
        return data.equals(dataTree.data);
    }

    @Override
    public final int hashCode() {
        return data.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + data + '}';
    }

    /**
     * A data tree which is unmistakably immutable.
     * <p>
     * Note that although the tree itself is immutable, any {@code DataTree}s contained within it may or may not be.
     */
    public static final class Immut extends DataTree {

        /**
         * Creates an empty data tree
         */
        public Immut() {
            super(new LinkedHashMap<>(1, 0.99f));
        }

        Immut(LinkedHashMap<Object, DataEntry> data) {
            super(data);
        }

        @Override
        public @NonNull Mut intoMut() {
            Mut mutCopy = new Mut(data);
            mutCopy.state = Mut.COPY_BEFORE_MUTATE;
            return mutCopy;
        }

        @Override
        public @NonNull Immut intoImmut() {
            return this;
        }
    }

    /**
     * A data tree which can unmistakably be modified
     *
     */
    public static final class Mut extends DataTree {

        private int state;

        private static final int REGULAR = 0;
        private static final int COPY_BEFORE_MUTATE = 1;
        private static final int POISONED = 2;

        /**
         * Creates
         */
        public Mut() {
            super(new LinkedHashMap<>());
        }

        Mut(LinkedHashMap<Object, DataEntry> data) {
            super(data);
        }

        @Override
        public @NonNull Mut intoMut() {
            return this;
        }

        @Override
        public @NonNull Immut intoImmut() {
            // SAFETY
            // Setting state = POISONED prevents future modifications
            state = POISONED;
            return new Immut(data);
        }

        private void ensureMutable() {
            if (state == POISONED) {
                throw new IllegalStateException("poisoned from #intoImmut");
            }
            if (state == COPY_BEFORE_MUTATE) {
                data = new LinkedHashMap<>(data);
                state = REGULAR;
            }
        }

        /**
         * Sets the entry at the specified key
         *
         * @param key the key
         * @param entry the entry; if null, clears any existing entry
         * @throws IllegalArgumentException if the provided key is not a valid canonical type
         */
        public void set(@NonNull Object key, @Nullable DataEntry entry) {
            if (!validateKey(key)) {
                throw new IllegalArgumentException("Not a canonical key: " + key);
            }
            ensureMutable();
            if (entry == null) {
                data.remove(key);
            } else {
                data.put(key, entry);
            }
        }

        /**
         * Clears any entry at the specified key
         *
         * @param key the key
         * @throws IllegalArgumentException if the provided key is not a valid canonical type
         */
        public void remove(@NonNull Object key) {
            if (!validateKey(key)) {
                throw new IllegalArgumentException("Not a canonical key: " + key);
            }
            ensureMutable();
            data.remove(key);
        }

        /**
         * Clears all data
         */
        public void clear() {
            ensureMutable();
            data.clear();
        }
    }

    // Implementation of self as DataStreamable

    /**
     * Returns this data tree, itself
     *
     * @return this object
     */
    @Override
    public @NonNull DataTree getAsTree() {
        return this;
    }

    /**
     * Gets the data in this tree as a {@link Stream}.
     * <p>
     * The stream is ordered according to the insertion order of this tree.
     *
     * @return a stream of keys and data entries
     */
    @Override
    public @NonNull Stream<Map.@NonNull Entry<@NonNull Object, @NonNull DataEntry>> getAsStream() {
        return data.entrySet().stream();
    }

}
