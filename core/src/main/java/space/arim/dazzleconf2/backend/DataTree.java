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
import org.checkerframework.dataflow.qual.Pure;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.SerializeOutput;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.BiConsumer;

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
 * order is fixed at creation. If built mutably, the order will be the insertion order of the elements. Note that
 * re-inserting an existing key will <i>not</i> change the order.
 * <p>
 * <b>Keys and values</b>
 * <p>
 * Values are wrapped by {@link DataEntry} and must be one of the canonical types. Keys are represented as
 * <code>Object</code> and must be one of the canonical types, excluding lists or trees.
 * <p>
 * Canonical types:<ul>
 * <li>String
 * <li>primitives represented by their boxed types
 * <li>DataTree for nesting
 * <li>Lists of {@code DataEntry}
 * </ul>
 * Keys <b>cannot</b> be DataTree or List. These requirements are enforced at runtime, and they can be
 * checked using {@link #validateKey(Object)} and {@link DataEntry#validateValue(Object)}.
 * <p>
 * Mutability of this class is <b>not defined</b>. Please use {@link DataTree.Mut} or {@link DataTree.Immut} if you need
 * mutable or immutable versions, or see the package javadoc for more information on the mutability model we use.
 *
 */
public abstract class DataTree {

    @NonNull LinkedHashMap<Object, DataEntry> data;

    DataTree(LinkedHashMap<Object, DataEntry> data) {
        this.data = data;
    }

    /**
     * Whether this data tree is devoid of key/value pairs
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * The number of key/value pairs in this data tree indicates its size
     *
     * @return the size of this data tree
     */
    public int size() {
        return data.size();
    }

    /**
     * Gets all the keys in this data tree.
     *
     * @return the key set, which may be immutable
     */
    public @NonNull Set<@NonNull Object> keySet() {
        return Collections.unmodifiableSet(data.keySet());
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
    @Pure
    public @Nullable DataEntry get(@NonNull Object key) {
        return data.get(key);
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
        StringBuilder output = new StringBuilder();
        toString(new DataToString(output).new Scope(0));
        return output.toString();
    }

    void toString(DataToString.Scope output) {
        output.append(getClass().getSimpleName());
        output.mapToString(data);
    }

    /**
     * A data tree which is immutable.
     * <p>
     * Note that although the tree itself is immutable, any {@code DataTree}s contained within it may or may not be.
     * Thus, this type serves more as a hint that the tree should not be mutated, than a solid guarantee that its
     * contents will never change.
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
            mutCopy.dataFrozen = true;
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

        // If the data in this Mut is shared with an Immut, it should not be modified
        private boolean dataFrozen;

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
            // Setting dataFrozen prevents future modifications
            dataFrozen = true;
            return new Immut(data);
        }

        private void ensureMutable() {
            if (dataFrozen) {
                data = new LinkedHashMap<>(data);
                dataFrozen = false;
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

        /**
         * Merges all data in the specified data tree into this one, merging nested sections.
         * <p>
         * If existing key/value pairs are shared between this tree and {@code source}, they will be overwritten and
         * copied from {@code source}. Key/value pairs that are unique to this tree will be retained.
         * <p>
         * <b>Merging nested trees and lists</b>
         * <p>
         * If this function encounters a key/value pair that is shared between this tree and {@code source}, and both
         * values are {@code DataTree}s, this function will act recursively and merge the two data trees. An
         * {@code UnsupportedOperationException} will be thrown if this is impossible due to the presence of
         * {@code DataTree.Immut}.
         * <p>
         * Lists are treated differently. If this function encounters a key/value pair that is shared, and both
         * values are lists, this function will overwrite the whole list.
         *
         * @param source the tree whose entries to copy into this one
         * @throws UnsupportedOperationException if this tree contains a {@code DataTree.Immut} or {@code List} that cannot be mutated
         */
        public void copyFrom(@NonNull DataTree source) {
            ensureMutable();
            source.forEach((key, copyEntry) -> {
                Object copyValue = copyEntry.getValue();
                if (copyValue instanceof DataTree) {
                    DataTree copyTree = (DataTree) copyValue;

                    DataEntry existingEntry = data.get(key);
                    if (existingEntry != null) {
                        Object existingValue = existingEntry.getValue();
                        if (existingValue instanceof DataTree.Mut) {
                            ((DataTree.Mut) existingValue).copyFrom(copyTree);
                            return;

                        } else if (existingValue instanceof DataTree.Immut) {
                            throw new IllegalStateException(
                                    "Tried to merge into data tree at " + key + " but it is immutable"
                            );
                        }
                    }
                }
                data.put(key, copyEntry);
            });
        }
    }

}
