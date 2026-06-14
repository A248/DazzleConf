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
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A list of in-memory configuration data.
 * <p>
 * This is the list version of {@link DataTree}. Element values are wrapped by {@link DataEntry} and must be one of the
 * canonical types:<ul>
 * <li>String
 * <li>primitives represented by their boxed types
 * <li>DataTree or DataList for nesting
 * </ul>
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
 * A data list is equal to another if they have the same entries in the same order. Mutability is not considered. Note
 * that {@code DataEntry} does not consider metadata, like comments or line number, in its equality contract.
 *
 */
public abstract class DataList {

    /**
     * Whether this data list is devoid of elements
     *
     * @return true if empty
     */
    @Pure
    public abstract boolean isEmpty();

    /**
     * The number of elements in this data tree indicates its size
     *
     * @return the size of this data tree
     */
    @Pure
    public abstract int size();

    /**
     * Gets this data tree as an immutable one.
     * <p>
     * The data contained within this {@code DataList} is copied to an immutable instance. Because {@link Immut}
     * requires deep immutability, any entries will be made immutable if they were not already, as they are copied to
     * the new instance.
     * <p>
     * If this instance is already an {@code DataList.Immut}, then it may be returned without changes.
     * <p>
     * <b>Implementation Notes</b>
     * <p>
     * The receiver of this method is unaffected according to visible side effects. If mutable, it may still be used.
     * However, the implementation of this method is optimized for the case that it is not used anymore.
     *
     * @return an immutable data list
     */
    @SideEffectFree
    public abstract @NonNull Immut intoImmut();

    /**
     * Gets this data list as a mutable one.
     * <p>
     * If not mutable, the data is copied to a new list, which will be made deeply mutable. That is, this function will
     * also be called on any {@code DataList}s encountered in this list's entries, and {@link DataTree#intoMut()} will
     * be used on any copied trees likewise.
     * <p>
     * If this instance is already {@code DataList.Immut}, then it may be returned without changes.
     *
     * @return this list if mutable, or a mutable copy if needed
     */
    @SideEffectFree
    public abstract @NonNull Mut intoMut();

    /**
     * Gets the entry at the specified index.
     *
     * @param idx the index
     * @return the entry
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    @Pure
    public abstract @NonNull DataEntry get(int idx);

    /**
     * Runs an action for each element.
     *
     * @param action the action
     */
    @SideEffectFree
    public abstract void forEach(Consumer<? super @NonNull DataEntry> action);

    abstract List<DataEntry> readOnlyList();

    @Override
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DataList)) return false;

        DataList dataList = (DataList) o;
        return readOnlyList().equals(dataList.readOnlyList());
    }

    @Override
    public final int hashCode() {
        return readOnlyList().hashCode();
    }

    @Override
    public final String toString() {
        return DataToString.implToString(this);
    }

    void dataToString(DataToString.Scope output)  {
        output.listToString(readOnlyList());
    }

    /**
     * A data list which is immutable.
     * <p>
     * This type guarantees that all of its elements are immutable (deep immutability). That is, {@link DataTree}s and
     * {@code DataList}s contained within this list's element entries will always be immutable.
     */
    public static final class Immut extends DataList {

        private final DataEntry[] data;

        /**
         * Creates an empty data list
         */
        public Immut() {
            this(new DataEntry[0], true);
        }

        /**
         * Creates from the given elements.
         * <p>
         * The array is copied to ensure immutability, and each entry is checked that it is not null.
         *
         * @param entries the elements, none of which can be null
         */
        public Immut(@NonNull DataEntry @NonNull ...entries) {
            DataEntry[] data = entries.clone();
            for (DataEntry datum : data) {
                Objects.requireNonNull(datum);
            }
            this.data = data;
        }

        /**
         * Creates from the given elements.
         * <p>
         * The collection is copied to ensure immutability, and each entry is checked that it is not null. The
         * encounter order of the collection becomes the order of this list.
         *
         * @param entries the elements, none of which can be null
         */
        public Immut(@NonNull Collection<@NonNull DataEntry> entries) {
            DataEntry[] data = entries.toArray(new DataEntry[0]);
            for (DataEntry datum : data) {
                Objects.requireNonNull(datum);
            }
            this.data = data;
        }

        /// Privileged constructor
        Immut(DataEntry[] data, boolean privileged) {
            this.data = data;
        }

        @Override
        public boolean isEmpty() {
            return data.length == 0;
        }

        @Override
        public int size() {
            return data.length;
        }

        @Override
        public @NonNull Immut intoImmut() {
            return this;
        }

        @Override
        public @NonNull Mut intoMut() {
            ArrayList<DataEntry> list = new ArrayList<>(data.length);
            for (DataEntry datum : data) {
                list.add(datum.intoMutDeep());
            }
            return new Mut(list);
        }

        @Override
        public @NonNull DataEntry get(int idx) {
            return data[idx];
        }

        @Override
        public void forEach(Consumer<? super @NonNull DataEntry> action) {
            for (DataEntry datum : data) {
                action.accept(datum);
            }
        }

        @Override
        List<DataEntry> readOnlyList() {
            return Arrays.asList(data);
        }
    }

    /**
     * A data list which can be modified.
     * <p>
     * Note that although the list itself is mutable, {@code DataTree}s and {@code DataList}s contained within its
     * element entries may or may not be. Thus, the type itself does not guarantee that its values are mutable.
     *
     */
    public static final class Mut extends DataList {

        private final ArrayList<DataEntry> data;

        Mut(ArrayList<DataEntry> data) {
            this.data = data;
        }

        /**
         * Creates
         *
         */
        public Mut() {
            this(new ArrayList<>());
        }

        /**
         * Creates with a capacity hint
         *
         * @param initialCapacity how many elements are expected to be held
         */
        public Mut(int initialCapacity) {
            this(new ArrayList<>(initialCapacity));
        }

        /**
         * Creates from the given elements.
         * <p>
         * The array is copied to ensure immutability, and each entry is checked that it is not null.
         *
         * @param entries the elements, none of which can be null
         */
        public Mut(@NonNull DataEntry @NonNull ...entries) {
            this(entries.length);
            for (DataEntry datum : entries) {
                Objects.requireNonNull(datum);
                data.add(datum);
            }
        }

        /**
         * Creates from the given elements.
         * <p>
         * The collection is copied to ensure immutability, and each entry is checked that it is not null. The
         * encounter order of the collection becomes the order of this list.
         *
         * @param entries the elements, none of which can be null
         */
        public Mut(@NonNull Collection<@NonNull DataEntry> entries) {
            this(entries.size());
            for (DataEntry datum : entries) {
                Objects.requireNonNull(datum);
                data.add(datum);
            }
        }

        @Override
        public boolean isEmpty() {
            return data.isEmpty();
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public @NonNull Immut intoImmut() {
            int size = data.size();
            DataEntry[] array = new DataEntry[size];
            for (int n = 0; n < size; n++) {
                array[n] = data.get(n).intoImmutDeep();
            }
            return new Immut(array, true);
        }

        @Override
        public @NonNull Mut intoMut() {
            return this;
        }

        @Override
        public @NonNull DataEntry get(int idx) {
            return data.get(idx);
        }

        @Override
        public void forEach(Consumer<? super @NonNull DataEntry> action) {
            data.forEach(action);
        }

        @Override
        List<DataEntry> readOnlyList() {
            return data;
        }

        /**
         * Adds the given entry, expanding this list to accommodate
         *
         * @param entry the entry
         */
        public void add(@NonNull DataEntry entry) {
            data.add(Objects.requireNonNull(entry));
        }

        /**
         * Replaces the element at the given index
         *
         * @param idx the index
         * @param entry the entry to place there
         * @throws IndexOutOfBoundsException if the index is out of bounds
         */
        public void set(int idx, @NonNull DataEntry entry) {
            data.set(idx, entry);
        }

        /**
         * Clears all data
         */
        public void clear() {
            data.clear();
        }
    }
}
