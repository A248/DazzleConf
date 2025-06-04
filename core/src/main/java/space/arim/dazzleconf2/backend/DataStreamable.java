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

import java.util.Map;
import java.util.stream.Stream;

/**
 * A tree-like source which can be turned into a data tree or streamed like its entries.
 * <p>
 * Consumers of this interface should treat it as a single-use resource. That is, they are permitted call one of the
 * <code>getAs</code> methods <b>at most once</b>. If the implementation is not re-usable and the caller attempts to
 * re-use it, {@code IllegalStateException} may be thrown.
 * <p>
 * {@link DataTree} itself implements this interface and <i>is</i> guaranteed to be re-usable.
 */
public interface DataStreamable {

    /**
     * Gets as a data tree, and potentially consumes this resource.
     *
     * @return the data tree
     * @throws IllegalStateException if re-use was attempted and this {@code DataStreamable} is not re-usable
     */
    @NonNull DataTree getAsTree();

    /**
     * Gets this data as a closeable {@link Stream}, and potentially consumes this resource.
     * <p>
     * The key of each item is guaranteed to be a valid key taken from a {@code DataTree}, such that it passes
     * {@link DataTree#validateKey(Object)} successfully.
     * <p>
     * <b>Closing</b>
     * <p>
     * The returned stream <i>must</i> be closed, such as by wrapping it in a try-with-resources
     * statement. If the stream is not closed after usage, resource leaks may occur.
     *
     * @return a stream of keys and data entries
     * @throws IllegalStateException if re-use was attempted and this {@code DataStreamable} is not re-usable
     */
    @NonNull Stream<Map.@NonNull Entry<@NonNull Object, ? extends @NonNull Entry>> getAsStream();

    /**
     * An entry within a stream of data.
     * <p>
     * This type is a streamable version of {@link DataEntry}. It might either be a single entry, or a streamable
     * tree-like source itself (for nesting). Callers should use
     * <p>
     * Like the stream itself, an entry should be treated as a single-use resource. Callers are permitted to use one
     * of the "getAs" methods at most once.
     *
     */
    interface Entry {

        /**
         * Whether this entry represents a tree-like value.
         * <p>
         * If {@code true}, callers are permitted to use {@link #getAsDataStreamable()} to stream the tree-like data.
         *
         * @return true if a treelike entry, false if a single entry
         */
        boolean isTreeLike();

        /**
         * Gets as a data entry, and potentially consumes this entry.
         * <p>
         * If this data entry represents a tree, the full tree will be loaded into memory and a {@code DataEntry}
         * wrapping a {@code DataTree} will be returned.
         *
         * @return the data entry
         * @throws IllegalStateException if re-use was attempted and this {@code Entry} is not re-usable
         */
        @NonNull DataEntry getAsDataEntry();

        /**
         * Gets as a nested {@code DataStreamable}, and potentially consumes this entry.
         * <p>
         * <b>Will fail if this entry represents just a single value.</b>
         *
         * @return the data streamable
         * @throws IllegalStateException if this {@code Entry} is just a single entry, or if re-use was attempted and
         * this {@code Entry} is not re-usable
         */
        @NonNull DataStreamable getAsDataStreamable();
    }
}
