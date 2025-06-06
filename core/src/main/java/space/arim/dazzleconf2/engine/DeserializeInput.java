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

package space.arim.dazzleconf2.engine;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;

/**
 * An object from a data tree, that is being processed for deserialization. See {@link DataTree}.
 * <p>
 * The object itself is provided by {@link #object()}.
 * <p>
 * <b>Implementation</b>
 * <p>
 * Instances of this type are implemented by the library and supplied to {@link SerializeDeserialize} implementations.
 * Equality is not defined, and no thread safety is provided.
 * <p>
 * This type should not be implemented by library consumers. New methods may be added in the future, and this interface
 * should be considered sealed. If library consumers decide to implement this interface, they might expose themselves
 * to {@code NoSuchMethodError}s if they pass their implementation to more up-to-date liaisons.
 */
public interface DeserializeInput extends DeserializeContext {

    /**
     * The actual object which is being deserialized.
     * <p>
     * This is guaranteed to be one of the canonical values used in {@link DataTree}, i.e. primitives,
     * <code>String</code>, or <code>DataTree</code>, or {@code List<DataEntry>}.
     *
     * @return the object
     */
    @NonNull Object object();

    /**
     * Requires the object to be a string
     *
     * @return the object as a string, or an error result if the type is mismatched
     */
    @NonNull LoadResult<@NonNull String> requireString();

    /**
     * Requires the object to be a data tree, i.e. a map of key/value pairs.
     * <p>
     * Callers may want to pair use of this method with the key mapper if they wish to read and write using string keys
     * on the {@code DataTree}.
     *
     * @return the object as a data tree, or an error result if the type is mismatched
     */
    @NonNull LoadResult<@NonNull DataTree> requireDataTree();

    /**
     * Makes a child and prepares it for deserialization.
     * <p>
     * The child value is supposed to be taken "from" this object. For example, an element in a list would be a child
     * object of the list.
     * <p>
     * The child value is checked to conform to {@link DataEntry#validateValue(Object)}.
     *
     * @param value the child value to wrap
     * @return deserializable input
     */
    @NonNull DeserializeInput makeChild(@NonNull Object value);

}
