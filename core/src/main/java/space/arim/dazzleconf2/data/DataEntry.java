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

package space.arim.dazzleconf2.data;

import space.arim.dazzleconf.internal.util.ImmutableCollections;

import java.util.*;

/**
 * Immutable representation of a configuration entry as interoperable with backend formats.
 * Values must conform to the canonical requirements. That means String, primitives, or {@link DataTree} for nesting.
 * Maps and Collections involving these types are also allowed.
 */
public final class DataEntry {

    private final Object value;
    private final Map<CommentLocation, List<String>> comments;

    /**
     * Creates from a nonnull value
     * @param value the value
     */
    public DataEntry(Object value) {
        this(value, ImmutableCollections.emptyMap());
    }

    DataEntry(Object value, Map<CommentLocation, List<String>> comments) {
        checkCanonical(value);
        this.value = Objects.requireNonNull(value, "value");
        this.comments = comments;
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
            coll.forEach(DataEntry::checkCanonical);
            return;
        }
        checkCanonicalSingle(value);
    }

    private static void checkCanonicalSingle(Object value) {
        if (value instanceof String || value instanceof Integer || value instanceof Boolean
                || value instanceof Character || value instanceof Short || value instanceof Long
                || value instanceof Double || value instanceof Float) {
            return;
        }
        throw new IllegalArgumentException("");
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
    public DataEntry withComments(CommentLocation location, List<String> comments) {
        Map<CommentLocation, List<String>> newComments = new EnumMap<>(CommentLocation.class);
        newComments.put(location, ImmutableCollections.listOf(comments));
        return new DataEntry(value, newComments);
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
}
