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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
    private final Map<CommentLocation, List<String>> comments;

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
        this(value, null, ImmutableCollections.emptyMap());
    }

    private DataEntry(Object value, Integer lineNumber, Map<CommentLocation, List<String>> comments) {
        if (!DataTree.validateValue(value)) {
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
     * Attaches a line number and returns a new object
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
     * Attaches comments and returns a new object.
     * <p>
     * If an empty comment list is specified, this function will clear the comments at the specified location.
     *
     * @param location where the comments are situated
     * @param comments the comments to specify at this location
     * @return a new data entry with the comments attached
     */
    public @NonNull DataEntry withComments(@NonNull CommentLocation location, @NonNull List<@NonNull String> comments) {
        if (comments.isEmpty()) {
            return clearComments(location);
        }
        Map<CommentLocation, List<String>> newComments = new EnumMap<>(CommentLocation.class);
        newComments.putAll(this.comments);
        newComments.put(location, ImmutableCollections.listOf(comments));
        return new DataEntry(value, lineNumber, newComments);
    }

    /**
     * Clears comments and returns a new object
     *
     * @param locations which comments to clear
     * @return a new data entry with no comments at the location
     */
    public @NonNull DataEntry clearComments(@NonNull CommentLocation @NonNull ... locations) {
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
        return new DataEntry(value, lineNumber, newComments);
    }

    /**
     * Gets the comments on this entry at the specified location
     *
     * @param location the location
     * @return the comments, or an empty list if none exist
     */
    public @NonNull List<@NonNull String> getComments(@NonNull CommentLocation location) {
        return comments.getOrDefault(location, ImmutableCollections.emptyList());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataEntry && value.equals(((DataEntry) obj).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
