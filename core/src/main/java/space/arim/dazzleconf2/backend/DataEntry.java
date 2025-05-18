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
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.internals.ImmutableCollections;

import java.util.*;

/**
 * Immutable representation of a configuration entry as interoperable with backend formats.
 * <p>
 * Values must conform to the canonical requirements. That means String, primitives, lists of these types, or
 * another {@link DataTree} for nesting.
 * <p>
 * Equality is defined for an entry based on its value. Comments and line numbers are <b>not</b> counted in
 * equality comparisons and hash code.
 */
public final class DataEntry {

    private final Object value;
    private final Integer lineNumber;
    private final Comments comments;

    /**
     * Creates from a nonnull value.
     * <p>
     * The value must conform to {@link DataTree#validateValue(Object)}
     * I.e., it must be one of primitive, <code>String</code>, <code>List</code> with valid elements, or
     * <code>DataTree</code>. Null values are not valid.
     *
     * @param value the value
     * @throws IllegalArgumentException if the value is not of the canonical types
     */
    public DataEntry(@NonNull Object value) {
        this(value, null, Comments.EMPTY);
    }

    private DataEntry(Object value, Integer lineNumber, Comments comments) {
        if (!validateValue(value)) {
            throw new IllegalArgumentException("Not a canonical value: " + value);
        }
        this.lineNumber = lineNumber;
        this.value = value;
        this.comments = comments;
    }

    /**
     * Gets the value. Guaranteed to be one of the canonical types (see {@link DataTree})
     *
     * @return the config value
     */
    public @NonNull Object getValue() {
        return value;
    }

    /**
     * Sets the value and returns a new object
     *
     * @param value the value
     * @return a new data entry with the value set
     * @throws IllegalArgumentException if the value is not of the canonical types
     */
    public @NonNull DataEntry withValue(@NonNull Object value) {
        return new DataEntry(value, lineNumber, comments);
    }

    /**
     * Sets the line number and returns a new object
     *
     * @param lineNumber the line number
     * @return a new data entry with the line number set
     */
    public @NonNull DataEntry withLineNumber(int lineNumber) {
        return new DataEntry(value, lineNumber, comments);
    }

    /**
     * Clears the line number and returns a new object
     *
     * @return a new data entry with no line number set
     */
    public @NonNull DataEntry clearLineNumber() {
        return (lineNumber == null) ? this : new DataEntry(value, null, comments);
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
     * Sets the comments and returns a new object
     *
     * @param comments the comments
     * @return a new data entry with the comments set
     */
    public @NonNull DataEntry withComments(@NonNull Comments comments) {
        return new DataEntry(value, lineNumber, comments);
    }

    /**
     * Sets comments at the given location and returns a new object.
     * <p>
     * If an empty list is specified, this function will clear the comments at the specified location.
     *
     * @param location where the comments are situated
     * @param lines the comment lines to specify at this location
     * @return a new data entry with the comments set
     */
    public @NonNull DataEntry withComments(@NonNull CommentLocation location, @NonNull List<@NonNull String> lines) {
        return withComments(comments.setAt(location, lines));
    }

    /**
     * Gets the comments present
     *
     * @return the comments
     */
    public @NonNull Comments getComments() {
        return comments;
    }

    /**
     * Gets the comments present on this entry at the specified location.
     * <p>
     * The returned value may be immutable, or it may be a mutable copy.
     *
     * @param location the location
     * @return the comment lines, or an empty list if none exist
     */
    public @NonNull List<@NonNull String> getComments(@NonNull CommentLocation location) {
        return comments.getAt(location);
    }

    /**
     * The comments on a data entry. Immutable.
     * <p>
     * This class stores a map of comment location to the lines of text at that location.
     *
     */
    public static final class Comments {

        private final Map<CommentLocation, List<String>> contents;

        private static final Comments EMPTY = new Comments();

        Comments(Map<CommentLocation, List<String>> contents) {
            this.contents = contents;
        }

        private Comments() {
            this.contents = ImmutableCollections.emptyMap();
        }

        /**
         * Gets whether there are no comments
         *
         * @return true if no comments exist
         */
        public boolean isEmpty() {
            return contents.isEmpty();
        }

        /**
         * Gets the comments at the specified location.
         * <p>
         * The returned value may be immutable, or it may be a mutable copy.
         *
         * @param location the location
         * @return the comment lines, or an empty list if none exist
         */
        public @NonNull List<@NonNull String> getAt(@NonNull CommentLocation location) {
            return contents.getOrDefault(location, ImmutableCollections.emptyList());
        }

        /**
         * Attaches comments and returns a new object.
         * <p>
         * If an empty list is specified, this function will clear the comments at the specified location.
         *
         * @param location where the comments are situated
         * @param lines the comment lines to specify at this location
         * @return a new instance of this class with the comments set
         */
        public @NonNull Comments setAt(@NonNull CommentLocation location, @NonNull List<@NonNull String> lines) {
            if (lines.isEmpty()) {
                return clearAt(location);
            }
            Map<CommentLocation, List<String>> newContents = new EnumMap<>(CommentLocation.class);
            newContents.putAll(this.contents);
            newContents.put(location, ImmutableCollections.listOf(lines));
            return new Comments(newContents);
        }

        /**
         * Appends comments at the following location and returns a new object.
         * <p>
         * If existing comments are set, they will come first.
         *
         * @param location where the comments will be situated
         * @param lines the comment lines to add at this location
         * @return a new instance of this class with the comments appended
         */
        public @NonNull Comments appendAt(@NonNull CommentLocation location, @NonNull List<@NonNull String> lines) {
            if (lines.isEmpty()) {
                return this;
            }
            Map<CommentLocation, List<String>> newContents = new EnumMap<>(CommentLocation.class);
            newContents.putAll(this.contents);
            List<String> existing = newContents.get(location);
            List<String> combined;
            if (existing == null) {
                combined = ImmutableCollections.listOf(lines);
            } else {
                combined = new ArrayList<>(existing.size() + lines.size());
                combined.addAll(existing);
                combined.addAll(lines);
                combined = ImmutableCollections.listOf(combined);
            }
            newContents.put(location, combined);
            return new Comments(newContents);
        }

        /**
         * Clears comments at some locations and returns a new object
         *
         * @param locations which comments to clear
         * @return a new instance of this class with no comments at the specified locations
         */
        public @NonNull Comments clearAt(@NonNull CommentLocation @NonNull ... locations) {
            boolean foundAny = false;
            for (CommentLocation location : locations) {
                foundAny = foundAny || contents.containsKey(location);
            }
            if (!foundAny) {
                return this;
            }
            Map<CommentLocation, List<String>> newContents = new EnumMap<>(CommentLocation.class);
            newContents.putAll(this.contents);
            for (CommentLocation location : locations) {
                newContents.remove(location);
            }
            if (newContents.isEmpty()) {
                return EMPTY;
            }
            return new Comments(newContents);
        }

        /**
         * Appends another set of comments to this one.
         * <p>
         * Any existing comments on this object will be placed first in the resulting product.
         *
         * @param other the other instance to append to this one
         * @return a new instance with the given comments appended
         */
        public @NonNull Comments append(@NonNull Comments other) {
            if (isEmpty()) {
                return other;
            }
            if (other.isEmpty()) {
                return this;
            }
            Map<CommentLocation, List<String>> combined = new EnumMap<>(CommentLocation.class);
            for (CommentLocation location : CommentLocation.values()) {
                List<String> ourLines = this.contents.get(location);
                List<String> theirLines = other.contents.get(location);
                if (ourLines == null && theirLines == null) {
                    // Nothing to do
                    continue;
                }
                List<String> combinedLines;
                if (ourLines == null) {
                    combinedLines = theirLines;
                } else if (theirLines == null) {
                    combinedLines = ourLines;
                } else {
                    combinedLines = new ArrayList<>(ourLines.size() + theirLines.size());
                    combinedLines.addAll(ourLines);
                    combinedLines.addAll(theirLines);
                    combinedLines = ImmutableCollections.listOf(combinedLines);
                }
                combined.put(location, combinedLines);
            }
            return new Comments(combined);
        }

        /**
         * Makes an instance of this class according to {@link space.arim.dazzleconf2.engine.Comments} annotations.
         * <p>
         * This function collects all the comments, in order, into a single object and returns it. If the argument
         * array is null or empty, an empty object is returned.
         *
         * @param source the annotations to use
         * @return a comments object
         */
        public static @NonNull Comments buildFrom(space.arim.dazzleconf2.engine.@NonNull Comments @Nullable [] source) {
            if (source == null || source.length == 0) {
                return EMPTY;
            }
            // A mutable result, containing mutable Lists
            Map<CommentLocation, List<String>> builtContents = new EnumMap<>(CommentLocation.class);
            for (space.arim.dazzleconf2.engine.Comments addFrom : source) {
                // Extract annotation data
                CommentLocation location = addFrom.location();
                String[] value = addFrom.value();
                if (value.length == 0) {
                    continue;
                }
                // Add to existing comments
                builtContents
                        .computeIfAbsent(location, k -> new ArrayList<>(value.length))
                        .addAll(Arrays.asList(value));
            }
            if (builtContents.isEmpty()) {
                return EMPTY;
            }
            // Make immutable
            for (CommentLocation location : CommentLocation.values()) {
                builtContents.computeIfPresent(location, (loc, lines) -> ImmutableCollections.listOf(lines));
            }
            return new Comments(builtContents);
        }

        /**
         * Returns an empty instance of {@code Comments}
         *
         * @return an empty instance
         */
        public static Comments empty() {
            return EMPTY;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Comments && contents.equals(((Comments) obj).contents);
        }

        @Override
        public int hashCode() {
            return contents.hashCode();
        }

        @Override
        public String toString() {
            return "DataEntry.Comments" + '{' + contents + '}';
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataEntry && value.equals(((DataEntry) obj).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Checks whether the given object is valid as a value.
     * <p>
     * Values must be one of primitive, <code>String</code>, <code>List</code> with valid elements,
     * or <code>DataTree</code>. Null values are not valid.
     *
     * @param value the value
     * @return true if a valid canonical value, false if not
     */
    public static boolean validateValue(@Nullable Object value) {
        return DataTree.validateValue(value);
    }
}
