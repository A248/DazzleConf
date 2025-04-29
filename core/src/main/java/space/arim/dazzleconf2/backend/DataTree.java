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
import space.arim.dazzleconf2.internals.ImmutableCollections;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.engine.KeyMapper;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.SerializeOutput;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A tree of in-memory configuration data. This tree is essentially a map of keys to values representing in-memory
 * configuration data, with added metadata of line numbers and comments.
 * <p>
 * Please keep in mind that a data tree interfaces with the configuration backend. As such, it uses the keys found in
 * the backend data, and it does <b>NOT</b> take into account method names or {@link KeyMapper}. It is highly
 * recommend to use <code>KeyMapper</code> where appropriate to interface with this class.
 * <p>
 * Keys are represented as <code>Object</code> and must be one of the canonical types (excl. lists or nested trees).
 * Values are wrapped by {@link Entry}, but are also <code>Object</code> and must be one of the canonical types.
 * <p>
 * Canonical types:<ul>
 * <li>String
 * <li>primitives represented by their boxed types
 * <li>DataTree for nesting
 * <li>Lists of the above types. The List implementation must be immutable.
 * </ul>
 * Recall that keys <b>cannot</b> be DataTree or List. These requirements are enforced at runtime, and they can be
 * checked using {@link #validateKey(Object)} and {@link #validateValue(Object)}
 * <p>
 * Mutability of this class is <b>not defined</b>. Please use {@link DataTreeImmut} or {@link DataTreeMut} if you need
 * guaranteed mutable or immutable versions.
 *
 */
public class DataTree {

    final Map<Object, Entry> data = new LinkedHashMap<>();

    DataTree() {}

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
    public @Nullable Entry get(@NonNull Object key) {
        return data.get(key);
    }

    /**
     * Gets all the keys used in this tree
     *
     * @return the key set view, which may or may not be modifiable
     */
    public @NonNull Collection<@NonNull Object> keySetView() {
        return Collections.unmodifiableSet(data.keySet());
    }

    /**
     * Runs an action for each key/value pair
     *
     * @param action the action
     */
    public void forEach(BiConsumer<? super @NonNull Object, ? super @NonNull Entry> action) {
        data.forEach(action);
    }

    /**
     * Gets this data tree as a mutable one. If not mutable, the data is copied to a new tree which is returned.
     *
     * @return this tree if mutable, or a copy if needed
     */
    public @NonNull DataTreeMut makeMut() {
        if (this instanceof DataTreeMut) {
            return (DataTreeMut) this;
        }
        return newDeepCopy(DataTreeMut::new);
    }

    /**
     * Gets this data tree as an immutable one. If not immutable, the data is copied to a new tree which is returned.
     *
     * @return this tree if immutable, or a copy if needed
     */
    public @NonNull DataTreeImmut makeImmut() {
        if (this instanceof DataTreeImmut) {
            return (DataTreeImmut) this;
        }
        return newDeepCopy(DataTreeImmut::new);
    }

    /**
     * Immutable representation of a configuration entry as interoperable with backend formats.
     * <p>
     * Values must conform to the canonical requirements. That means String, primitives, lists of these types, or
     * another {@link DataTree} for nesting.
     * <p>
     * Equality is defined for an entry based on its value. Comments and line numbers are <b>not</b> counted in
     * equality comparisons and hash code.
     */
    public static final class Entry {

        private final Object value;
        private final Integer lineNumber;
        private final Map<CommentLocation, List<String>> comments;

        /**
         * Creates from a nonnull value
         * @param value the value
         * @throws IllegalArgumentException if the value is not of the canonical types
         */
        public Entry(@NonNull Object value) {
            this(value, null, ImmutableCollections.emptyMap());
        }

        private Entry(Object value, Integer lineNumber, Map<CommentLocation, List<String>> comments) {
            if (!validateValue(value)) {
                throw new IllegalArgumentException("Not a canonical value: " + value);
            }
            this.lineNumber = lineNumber;
            this.value = value;
            this.comments = comments;
        }

        /**
         * Gets the value. Guaranteed to be one of the canonical types (see {@link DataTree})
         * @return the config value
         */
        public @NonNull Object getValue() {
            return value;
        }

        /**
         * Sets the value and returns a new object
         * @param value the value
         * @return a new data entry with the value set
         * @throws IllegalArgumentException if the value is not of the canonical types
         */
        public @NonNull Entry withValue(@NonNull Object value) {
            return new Entry(value, lineNumber, comments);
        }

        /**
         * Attaches a line number and returns a new object
         *
         * @param lineNumber the line number
         * @return a new data entry with the line number set
         */
        public @NonNull Entry withLineNumber(int lineNumber) {
            return new Entry(value, lineNumber, comments);
        }

        /**
         * Clears the line number and returns a new object
         *
         * @return a new data entry with no line number set
         */
        public @NonNull Entry clearLineNumber() {
            return  (lineNumber == null) ? this : new Entry(value, null, comments);
        }

        /**
         * Gets the line number if present
         *
         * @return the line number
         */
        public @Nullable Integer getLineNumber() {
            return lineNumber;
        }

        /**
         * Attaches comments and returns a new object.
         * <p>
         * If an empty comment list is specified, this function will clear the comments at the specified location.
         * @param location where the comments are situated
         * @param comments the comments to specify at this location
         * @return a new data entry with the comments attached
         */
        public @NonNull Entry withComments(@NonNull CommentLocation location, @NonNull List<@NonNull String> comments) {
            if (comments.isEmpty()) {
                return clearComments(location);
            }
            Map<CommentLocation, List<String>> newComments = new EnumMap<>(CommentLocation.class);
            newComments.putAll(this.comments);
            newComments.put(location, ImmutableCollections.listOf(comments));
            return new Entry(value, lineNumber, newComments);
        }

        /**
         * Clears comments and returns a new object
         *
         * @param locations which comments to clear
         * @return a new data entry with no comments at the location
         */
        public @NonNull Entry clearComments(@NonNull CommentLocation @NonNull...locations) {
            boolean foundAny = false;
            for (CommentLocation location : locations) {
                foundAny = foundAny || comments.containsKey(location);
            }
            if (!foundAny) {
                return this;
            }
            Map<CommentLocation, List<String>> newComments = new EnumMap<>(CommentLocation.class);
            newComments.putAll(this.comments);
            for (CommentLocation location : locations) {
                newComments.remove(location);
            }
            if (newComments.isEmpty()) {
                newComments = ImmutableCollections.emptyMap();
            }
            return new Entry(value, lineNumber, newComments);
        }

        /**
         * Gets the comments on this entry at the specified location
         * @param location the location
         * @return the comments, or an empty list if none exist
         */
        public @NonNull List<@NonNull String> getComments(@NonNull CommentLocation location) {
            return comments.getOrDefault(location, ImmutableCollections.emptyList());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof DataTree.Entry && value.equals(((DataTree.Entry) obj).value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    private <M extends DataTree> M newDeepCopy(Supplier<M> treeConstructor) {
        M copy = treeConstructor.get();
        data.forEach((key, entry) -> {
            Object value = entry.getValue();
            Object copiedValue;
            if (value instanceof DataTree) {
                copiedValue = ((DataTree) value).newDeepCopy(treeConstructor);
            } else {
                // Lists are immutable by contract
                copiedValue = value;
            }
            copy.data.put(key, entry.withValue(copiedValue));
        });
        return copy;
    }

    /**
     * Checks whether the given object is valid as a value in the data tree. Keys must be one of primitive,
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
    public boolean equals(Object o) {
        if (!(o instanceof DataTree)) return false;

        DataTree dataTree = (DataTree) o;
        return data.equals(dataTree.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public String toString() {
        return "DataTree{" + data + '}';
    }
}
