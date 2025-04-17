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

import space.arim.dazzleconf.internal.util.ImmutableCollections;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A tree of in-memory configuration data. This tree is essentially a map of keys to values representing in-memory
 * configuration data, with added metadata of line numbers and comments.
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
 * Recall that keys <b>cannot</b> be DataTree or List. These requirements are enforced at runtime.
 * <p>
 * Mutability of this class is <b>not defined</b>. Please use {@link DataTreeImmut} or {@link DataTreeMut} if you need
 * guaranteed mutable or immutable versions.
 *
 */
public class DataTree {

    final Map<Object, Entry> data = new LinkedHashMap<>();

    DataTree() {}

    /**
     * Gets the entry at the specified key, or null if unset
     *
     * @param key the key
     * @return the entry
     */
    public Entry get(Object key) {
        return data.get(key);
    }

    /**
     * Gets all the keys used in this tree
     *
     * @return the key set view, which may or may not be modifiable
     */
    public Collection<Object> keySetView() {
        return Collections.unmodifiableSet(data.keySet());
    }

    /**
     * Runs an action for each key/value pair
     *
     * @param action the action
     */
    public void forEach(BiConsumer<? super Object, ? super Entry> action) {
        data.forEach(action);
    }

    /**
     * Gets this data tree as a mutable one. If not mutable, the data is copied to a new tree which is returned.
     *
     * @return this tree if mutable, or a copy if needed
     */
    public DataTreeMut makeMut() {
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
    public DataTreeImmut makeImmut() {
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
     */
    public static final class Entry {

        private final Object value;
        private final Integer lineNumber;
        private final Map<CommentLocation, List<String>> comments;

        /**
         * Creates from a nonnull value
         * @param value the value
         */
        public Entry(Object value) {
            this(value, null, ImmutableCollections.emptyMap());
        }

        private Entry(Object value, Integer lineNumber, Map<CommentLocation, List<String>> comments) {
            this.lineNumber = lineNumber;
            checkCanonical(value);
            this.value = Objects.requireNonNull(value, "value");
            this.comments = comments;
        }

        /**
         * Gets the value. Guaranteed to be one of the canonical types (see {@link DataTree})
         * @return the config value
         */
        public Object getValue() {
            return value;
        }

        /**
         * Sets the value and returns a new object
         * @param value the value
         * @return a new data entry with the value set
         */
        public Entry withValue(Object value) {
            return new Entry(value, lineNumber, comments);
        }

        /**
         * Attaches a line number and returns a new object
         *
         * @param lineNumber the line number
         * @return a new data entry with the line number set
         */
        public Entry withLineNumber(int lineNumber) {
            return new Entry(value, lineNumber, comments);
        }

        /**
         * Gets the line number if present
         *
         * @return the line number
         */
        public OptionalInt lineNumber() {
            if (lineNumber == null) {
                return OptionalInt.empty();
            } else {
                return OptionalInt.of(lineNumber);
            }
        }

        /**
         * Attaches comments and returns a new object
         * @param location the comments
         * @param comments the comments to specify at this location
         * @return a new data entry with the comments attached
         */
        public Entry withComments(CommentLocation location, List<String> comments) {
            Map<CommentLocation, List<String>> newComments = new EnumMap<>(CommentLocation.class);
            newComments.putAll(this.comments);
            newComments.put(location, ImmutableCollections.listOf(comments));
            return new Entry(value, lineNumber, newComments);
        }

        /**
         * Gets the comments on this entry at the specified location
         * @param location the location
         * @return the comments, or an empty list if none exist
         */
        public List<String> commentsAt(CommentLocation location) {
            return comments.getOrDefault(location, ImmutableCollections.emptyList());
        }
    }

    /**
     * Where comments are located
     */
    public enum CommentLocation {
        /**
         * Above the entry
         */
        ABOVE,
        /**
         * Inline to the entry. For example, "key: value # comment" is a valid inline comment in YAML.
         */
        INLINE,
        /**
         * Below the entry
         */
        BELOW
    }

    private <M extends DataTree> M newDeepCopy(Supplier<M> treeConstructor) {
        M copy = treeConstructor.get();
        data.forEach((key, entry) -> {
            Object value = entry.getValue();
            Object copiedValue;
            if (value instanceof DataTree) {
                copiedValue = ((DataTree) value).newDeepCopy(treeConstructor);
            } else {
                // Maps and Collections are immutable by contract
                copiedValue = value;
            }
            copy.data.put(key, entry.withValue(copiedValue));
        });
        return copy;
    }

    static void checkCanonical(Object value) {
        if (value instanceof List) {
            ((List<?>) value).forEach(DataTree::checkCanonical);
            return;
        }
        if (value instanceof DataTree) {
            return;
        }
        checkCanonicalSingle(value);
    }

    static void checkCanonicalSingle(Object value) {
        if (value instanceof String
                || value instanceof Boolean || value instanceof Byte || value instanceof Character
                || value instanceof Short || value instanceof Integer || value instanceof Long
                || value instanceof Float || value instanceof Double) {
            return;
        }
        if (value == null) {
            throw new NullPointerException("Unexpected null value");
        }
        throw new IllegalArgumentException("Not a canonical value: " + value);
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
