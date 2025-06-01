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

import java.util.*;

/**
 * A data value, with associated metadata as interoperable with backend formats.
 * <p>
 * Values must conform to the canonical requirements. That means String, primitives, lists of these types, or
 * another {@link DataTree} for nesting. Although a {@code DataEntry} itself is immutable, its contents may not be,
 * and mutable lists and mutable data trees are permitted to be the value.
 * <p>
 * Equality is defined for an entry based on its value. Comments and line numbers are <b>not</b> counted in
 * equality comparisons and hash code.
 */
public final class DataEntry {

    private final @NonNull Object value;
    private final Integer lineNumber;
    private final CommentData comments;

    /**
     * Creates from a nonnull value.
     * <p>
     * The value must conform to {@link #validateValue(Object)}
     * I.e., it must be one of primitive, <code>String</code>, <code>List</code> with valid elements, or
     * <code>DataTree</code>. Null values are not valid.
     *
     * @param value the value
     * @throws IllegalArgumentException if the value is not of the canonical types
     */
    public DataEntry(@NonNull Object value) {
        this(value, null, CommentData.empty());
    }

    private DataEntry(@NonNull Object value, Integer lineNumber, CommentData comments) {
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
    public @NonNull DataEntry withComments(@NonNull CommentData comments) {
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
    public @NonNull CommentData getComments() {
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataEntry && value.equals(((DataEntry) obj).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "value=" + value +
                ", lineNumber=" + lineNumber +
                ", comments=" + comments +
                '}';
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
        if (value instanceof List) {
            for (Object elem : (List<?>) value) {
                if (!validateValue(elem)) {
                    return false;
                }
            }
            return true;
        }
        return value instanceof DataTree || DataTree.validateKey(value);
    }
}
