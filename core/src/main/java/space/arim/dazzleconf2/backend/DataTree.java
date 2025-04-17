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

/**
 * A mutable tree of configuration format data. Keys and values are subject to a few constraints.
 * <p>
 * For example, keys must be either String or primitive. They cannot be maps or collections. Values have the added
 * privilege of being collections, but those collections should always be mutable.
 *
 */
public final class DataTree {

    private final Map<Object, Entry> data = new HashMap<>();

    /**
     * Creates
     */
    public DataTree() {}

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
     * Sets the entry at the specified key
     *
     * @param key the key
     * @param entry the entry; if null, clears any existing entry
     */
    public void set(Object key, Entry entry) {
        checkCanonicalSingle(key);
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
     */
    public void remove(Object key) {
        checkCanonicalSingle(key);
        data.remove(key);
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
     * Clears all data
     */
    public void clear() {
        data.clear();
    }

    /**
     * Immutable representation of a configuration entry as interoperable with backend formats.
     * Values must conform to the canonical requirements. That means String, primitives, or {@link DataTree} for nesting.
     * Maps and Collections involving these types are also allowed.
     */
    public static final class Entry {

        private final Object value;
        private final Map<CommentLocation, List<String>> comments;

        /**
         * Creates from a nonnull value
         * @param value the value
         */
        public Entry(Object value) {
            this(value, ImmutableCollections.emptyMap());
        }

        private Entry(Object value, Map<CommentLocation, List<String>> comments) {
            checkCanonical(value);
            this.value = Objects.requireNonNull(value, "value");
            this.comments = comments;
        }

        /**
         * Gets the value
         * @return the config value
         */
        public Object getValue() {
            return value;
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
            return new Entry(value, newComments);
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

    private static void checkCanonical(Object value) {
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            map.forEach((k, v) -> {
                checkCanonicalSingle(k);
                checkCanonical(v);
            });
            return;
        }
        if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            coll.forEach(DataTree::checkCanonical);
            return;
        }
        checkCanonicalSingle(value);
    }

    private static void checkCanonicalSingle(Object value) {
        if (value instanceof String
                || value instanceof Boolean || value instanceof Byte || value instanceof Short
                || value instanceof Character || value instanceof Integer || value instanceof Long
                || value instanceof Double || value instanceof Float) {
            return;
        }
        if (value instanceof DataTree) {
            return;
        }
        if (value == null) {
            throw new NullPointerException("Unexpected null value");
        }
        throw new IllegalArgumentException("Not a canonical value: " + value);
    }
}
