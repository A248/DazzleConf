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
 * The comments on a data entry or configuration class.
 * <p>
 * This class is immutable, meaning that methods which appear to change data return a new object instead.
 */
public final class CommentData {

    private final @NonNull Map<CommentLocation, List<String>> contents;

    static final CommentData EMPTY = new CommentData();

    CommentData(@NonNull Map<CommentLocation, List<String>> contents) {
        this.contents = contents;
    }

    private CommentData() {
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
     * @param lines    the comment lines to specify at this location
     * @return a new instance of this class with the comments set
     */
    public @NonNull CommentData setAt(@NonNull CommentLocation location, @NonNull List<@NonNull String> lines) {
        if (lines.isEmpty()) {
            return clearAt(location);
        }
        Map<CommentLocation, List<String>> newContents = new EnumMap<>(CommentLocation.class);
        newContents.putAll(this.contents);
        newContents.put(location, ImmutableCollections.listOf(lines));
        return new CommentData(newContents);
    }

    /**
     * Attaches comments and returns a new object.
     * <p>
     * If an empty array is specified, this function will clear the comments at the specified location.
     *
     * @param location where the comments are situated
     * @param lines    the comment lines to specify at this location
     * @return a new instance of this class with the comments set
     */
    public @NonNull CommentData setAt(@NonNull CommentLocation location, @NonNull String @NonNull ... lines) {
        return setAt(location, Arrays.asList(lines));
    }

    /**
     * Appends comments at the following location and returns a new object.
     * <p>
     * If existing comments are set, they will come first.
     *
     * @param location where the comments will be situated
     * @param lines    the comment lines to add at this location
     * @return a new instance of this class with the comments appended
     */
    public @NonNull CommentData appendAt(@NonNull CommentLocation location, @NonNull List<@NonNull String> lines) {
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
        return new CommentData(newContents);
    }

    /**
     * Appends comments at the following location and returns a new object.
     * <p>
     * If existing comments are set, they will come first.
     *
     * @param location where the comments will be situated
     * @param lines    the comment lines to add at this location
     * @return a new instance of this class with the comments appended
     */
    public @NonNull CommentData appendAt(@NonNull CommentLocation location, @NonNull String @NonNull ... lines) {
        return appendAt(location, Arrays.asList(lines));
    }

    /**
     * Clears comments at some locations and returns a new object
     *
     * @param locations which comments to clear
     * @return a new instance of this class with no comments at the specified locations
     */
    public @NonNull CommentData clearAt(@NonNull CommentLocation @NonNull ... locations) {
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
        return new CommentData(newContents);
    }

    /**
     * Appends another set of comments to this one.
     * <p>
     * Any existing comments on this object will be placed first in the resulting product.
     *
     * @param other the other instance to append to this one
     * @return a new instance with the given comments appended
     */
    public @NonNull CommentData append(@NonNull CommentData other) {
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
        return new CommentData(combined);
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
    public static @NonNull CommentData buildFrom(space.arim.dazzleconf2.engine.@NonNull Comments @Nullable [] source) {
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
        return new CommentData(builtContents);
    }

    /**
     * Returns an empty instance of {@code Comments}
     *
     * @return an empty instance
     */
    public static CommentData empty() {
        return EMPTY;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CommentData && contents.equals(((CommentData) obj).contents);
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
