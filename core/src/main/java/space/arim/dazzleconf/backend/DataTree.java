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

package space.arim.dazzleconf.backend;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
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
 * <li>DataTree or DataList for nesting
 * </ul>
 * Keys <b>cannot</b> be DataTree or List. These requirements are enforced at runtime, and they can be
 * checked using {@link #validateKey(Object)} and {@link DataEntry#validateValue(Object)}.
 * <p>
 * <b>Mutability</b>
 * <p>
 * Mutability of this class is <b>not defined</b>. Please use {@link Mut} or {@link Immut} if you need mutable or
 * immutable versions, and please read the package documentation regarding the mutability model and thread safety of
 * these types.
 * <p>
 * Note that {@code instanceof} is not a reliable way to determine mutability or lack thereof. This class may
 * support additional subclasses for different purposes, such as lazy evaluation or alternative internal representations.
 * <p>
 * <b>Equality</b>
 * <p>
 * A data tree is equal to another if they have the same keys, and the entry at each key is equal. Order and mutability
 * are not considered.
 * <p>
 * Note that keys may not be equal if reloading data from a backend, depending on whether that backend converts all
 * keys to strings (e.g. "1" instead of 1). Additionally, note that {@code DataEntry} does not consider metadata, like
 * comments or line number, in its equality contract.
 *
 */
public abstract class DataTree {

    @NonNull LinkedHashMap<Object, DataEntry> data;

    DataTree(@NonNull LinkedHashMap<Object, DataEntry> data) {
        this.data = data;
    }

    /**
     * Whether this data tree is devoid of key/value pairs
     *
     * @return true if empty
     */
    @Pure
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * The number of key/value pairs in this data tree indicates its size
     *
     * @return the size of this data tree
     */
    @Pure
    public int size() {
        return data.size();
    }

    /**
     * Gets all the keys in this data tree.
     * <p>
     * Note that if this data tree is mutable, then mutating it while iterating over the returned set may cause
     * concurrent modification exceptions.
     *
     * @return the key set, which reflects at least the state of the data tree when this method is called. It may be a
     * mutable copy, an immutable copy, a mutable view, or an immutable view.
     */
    @SideEffectFree
    public @NonNull Set<@NonNull Object> keySet() {
        return Collections.unmodifiableSet(data.keySet());
    }

    /**
     * Gets the entry at the specified key, or null if unset.
     * <p>
     * If accessing keys based on method names, it is strongly recommended to use the {@link KeyMapper} to map
     * method names to keys. The key mapper is available during both deserialization and serialization.
     *
     * @param key the key
     * @return the entry
     */
    @Pure
    public abstract @Nullable DataEntry get(@NonNull Object key);

    /**
     * Runs an action for each key/value pair.
     * <p>
     * Iteration maintains the order with which this data tree was created.
     *
     * @param action the action
     */
    @SideEffectFree
    public abstract void forEach(BiConsumer<? super @NonNull Object, ? super @NonNull DataEntry> action);

    /**
     * Gets this data tree as an immutable one.
     * <p>
     * The data contained within this {@code DataTree} is copied to an immutable instance. Because {@link Immut}
     * requires deep immutability, any entries will be made immutable if they were not already, as they are copied to
     * the new instance.
     * <p>
     * If this instance is already an {@code DataTree.Immut}, then it may be returned without changes.
     * <p>
     * <b>Implementation Notes</b>
     * <p>
     * The receiver of this method is unaffected according to visible side effects. If mutable, it may still be used.
     * However, the implementation of this method is optimized for the case that it is not used anymore.
     *
     * @return an immutable data tree
     */
    @SideEffectFree
    public abstract @NonNull Immut intoImmut();

    /**
     * Gets this data list as a mutable one.
     * <p>
     * If not mutable, the data is copied to a new tree, which will be made deeply mutable. That is, this function will
     * also be called on any {@code DataTree}s encountered in this tree's entries, and {@link DataList#intoMut()} will
     * be used on any copied lists likewise.
     * <p>
     * If this instance is already {@code DataTree.Immut}, then it may be returned without changes.
     *
     * @return this tree if mutable, or a mutable copy if needed
     */
    @SideEffectFree
    public abstract @NonNull Mut intoMut();

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
        if (o == this) return true;
        if (!(o instanceof DataTree)) return false;

        DataTree dataTree = (DataTree) o;
        return data.equals(dataTree.data);
    }

    @Override
    public final int hashCode() {
        return data.hashCode();
    }

    @Override
    public final String toString() {
        return DataToString.implToString(this);
    }

    void dataToString(DataToString.Scope output) {
        output.mapToString(data);
    }

    abstract void setAllIn(Mut destination);

    abstract void putAllInNonEmpty(Mut destination);

    /**
     * A data tree which is immutable.
     * <p>
     * This type guarantees that all of its entries are immutable (deep immutability). That is, {@code DataTree}s and
     * {@code DataList}s contained within this tree's entries will always be immutable.
     */
    public static final class Immut extends DataTree {

        /**
         * Creates an empty data tree
         */
        public Immut() {
            super(new LinkedHashMap<>(1, 0.99f));
        }

        Immut(@NonNull LinkedHashMap<Object, DataEntry> data) {
            super(data);
        }

        @Override
        public @Nullable DataEntry get(@NonNull Object key) {
            return data.get(key);
        }

        @Override
        public void forEach(BiConsumer<? super @NonNull Object, ? super @NonNull DataEntry> action) {
            data.forEach(action);
        }

        @Override
        public @NonNull Immut intoImmut() {
            return this;
        }

        @Override
        public @NonNull Mut intoMut() {
            Mut mut = new Mut(data);
            mut.state = Mut.FROZEN_SHARED_IMMUT_SHALLOW;
            return mut;
        }

        @Override
        void setAllIn(Mut destination) {
            if (destination.state == Mut.FROZEN_SHARED_MUT) {
                destination.sharedMut.state = Mut.NORMAL;
                destination.sharedMut.sharedMut = null;
                destination.sharedMut = null;
            }
            destination.data = data;
            destination.state = Mut.FROZEN_SHARED_IMMUT_SHALLOW;
        }

        @Override
        void putAllInNonEmpty(Mut destination) {
            destination.ensureMutable();
            destination.data.putAll(data);
        }
    }

    /**
     * A data tree which can be modified.
     * <p>
     * Note that although the tree itself is mutable, {@code DataTree}s and {@code DataList}s contained within its
     * entries may or may not be. Thus, the type itself does not guarantee that its values are mutable.
     * <p>
     * That said, callers are encouraged to maintain the deep mutability of this {@code Mut}. They should avoid storing
     * {@link Immut} or {@link DataList.Immut} in it without good reason.
     *
     */
    public static final class Mut extends DataTree {

        // If the data in this Mut is shared with an Immut, it should not be modified
        // If this Mut was created by intoMut(), it needs to provide deep mutability (FROZEN_SHARED_IMMUT_SHALLOW)
        private int state;
        private DataTree.Mut sharedMut;

        private static final int NORMAL = 0;
        // Created by intoMut(), must stay immutable then transform into deep mutability
        private static final int FROZEN_SHARED_IMMUT_SHALLOW = 1;
        // Shared with an Immut, must stay immutable simply
        private static final int FROZEN_SHARED_IMMUT_AS_IS = 2;
        // Shared with the Mut in sharedMut
        private static final int FROZEN_SHARED_MUT = 3;

        /**
         * Creates
         */
        public Mut() {
            super(new LinkedHashMap<>());
        }

        Mut(@NonNull LinkedHashMap<Object, DataEntry> data) {
            super(data);
        }

        @Override
        public @Nullable DataEntry get(@NonNull Object key) {
            DataEntry entry = data.get(key);
            if (state == FROZEN_SHARED_IMMUT_SHALLOW && entry != null) {
                Object value = entry.getValue();
                if (value instanceof DataTree.Immut || value instanceof DataList.Immut) {
                    // Preserve deep mutability of copied entries
                    ensureMutable();
                    entry = data.get(key);
                }
            }
            return entry;
        }

        @Override
        public void forEach(BiConsumer<? super @NonNull Object, ? super @NonNull DataEntry> action) {
            if (state == FROZEN_SHARED_IMMUT_SHALLOW) {
                // Preserve deep mutability of copied entries
                ensureMutable();
            }
            data.forEach(action);
        }

        @Override
        public @NonNull Immut intoImmut() {
            switch (state) {
                case FROZEN_SHARED_IMMUT_SHALLOW:
                case FROZEN_SHARED_IMMUT_AS_IS:
                    return new Immut(data);
                case NORMAL:
                case FROZEN_SHARED_MUT:
                default:
                    break;
            }
            LinkedHashMap<Object, DataEntry> immutData = new LinkedHashMap<>();
            data.forEach((key, entry) -> {
                immutData.put(key, entry.intoImmutDeep());
            });
            return new Immut(immutData);
        }

        @Override
        public @NonNull Mut intoMut() {
            return this;
        }

        private void ensureMutable() {
            switch (state) {
                case FROZEN_SHARED_IMMUT_SHALLOW:
                    LinkedHashMap<Object, DataEntry> newData = new LinkedHashMap<>(data.size());
                    data.forEach((key, entry) -> newData.put(key, entry.intoMutDeep()));
                    data = newData;
                    break;
                case FROZEN_SHARED_MUT:
                    sharedMut.state = NORMAL;
                    sharedMut.sharedMut = null;
                    sharedMut = null; // FT
                case FROZEN_SHARED_IMMUT_AS_IS:
                    data = new LinkedHashMap<>(data);
                    break;
                default:
                    break;
            }
            state = NORMAL;
        }

        /**
         * Sets this data tree to be <i>exactly equal</i> to an existing one.
         * <p>
         * All data from the argument tree is copied into this one, and all data present in this tree is overwritten
         * or cleared.
         * <p>
         * If the argument is equal to {@code this}, this call is a no-op. Otherwise, it is equivalent to clearing this
         * data tree then setting all its entries from the argument.
         *
         * @param dataTree the tree to set from
         */
        public void setAll(@NonNull DataTree dataTree) {
            if (dataTree == this) {
                return;
            }
            dataTree.setAllIn(this);
        }

        @Override
        void setAllIn(Mut destination) {
            if (destination.state == FROZEN_SHARED_MUT) {
                if (destination.sharedMut == this) {
                    // Hooray
                    return;
                }
                destination.sharedMut.state = NORMAL;
                destination.sharedMut.sharedMut = null;
            }
            switch (state) {
                case FROZEN_SHARED_IMMUT_SHALLOW:
                case FROZEN_SHARED_IMMUT_AS_IS:
                    destination.data = data;
                    destination.state = state;
                    return;
                case FROZEN_SHARED_MUT:
                    destination.data = new LinkedHashMap<>(data);
                    destination.state = NORMAL;
                    return;
                case NORMAL:
                default:
                    break;
            }
            destination.data = data;
            destination.state = FROZEN_SHARED_MUT;
            destination.sharedMut = this;
            sharedMut = destination;
            state = FROZEN_SHARED_MUT;
        }

        /**
         * Sets the entry at the specified key. Replaces any existing entry at the key
         *
         * @param key the key
         * @param entry the entry
         * @return the previous entry at the key, or null if there is none
         * @throws IllegalArgumentException if the provided key is not a valid canonical type
         */
        public @Nullable DataEntry put(@NonNull Object key, @NonNull DataEntry entry) {
            Objects.requireNonNull(entry, "entry");
            if (!validateKey(key)) {
                throw new IllegalArgumentException("Not a canonical key: " + key);
            }
            ensureMutable();
            return data.put(key, entry);
        }

        /**
         * Puts multiple entries into this data tree, from another one
         *
         * @param dataTree the tree to put from
         */
        public void putAll(@NonNull DataTree dataTree) {
            if (dataTree == this) {
                return;
            }
            if (isEmpty()) {
                dataTree.setAllIn(this);
            } else {
                dataTree.putAllInNonEmpty(this);
            }
        }

        @Override
        void putAllInNonEmpty(Mut destination) {
            switch (state) {
                case FROZEN_SHARED_IMMUT_SHALLOW:
                    ensureMutable(); // FT
                case NORMAL:
                case FROZEN_SHARED_IMMUT_AS_IS:
                case FROZEN_SHARED_MUT:
                    destination.ensureMutable();
                    destination.data.putAll(data);
                    break;
            }
        }

        /**
         * Clears any entry at the specified key
         *
         * @param key the key
         * @return the previous entry that was at the key, or null if there is none
         * @throws IllegalArgumentException if the provided key is not a valid canonical type
         */
        public @Nullable DataEntry remove(@NonNull Object key) {
            if (!validateKey(key)) {
                throw new IllegalArgumentException("Not a canonical key: " + key);
            }
            ensureMutable();
            return data.remove(key);
        }

        /**
         * Clears all data
         */
        public void clear() {
            switch (state) {
                case NORMAL:
                    data.clear();
                    break;
                case FROZEN_SHARED_MUT:
                    sharedMut.state = NORMAL;
                    sharedMut.sharedMut = null;
                    sharedMut = null; // FT
                case FROZEN_SHARED_IMMUT_SHALLOW:
                case FROZEN_SHARED_IMMUT_AS_IS:
                    data = new LinkedHashMap<>();
                    state = NORMAL;
                    break;
            }
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
         * @throws UnsupportedOperationException if this tree contains a {@code DataTree.Immut} that cannot be mutated
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
