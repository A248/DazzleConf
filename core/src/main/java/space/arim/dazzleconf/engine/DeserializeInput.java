/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

package space.arim.dazzleconf.engine;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.LoadResult;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataList;
import space.arim.dazzleconf.backend.DataTree;

/**
 * An object from a data tree, that is being processed for deserialization.
 * <p>
 * The object itself is provided by {@link #object()}.
 * <p>
 * <b>Library implementor</b>
 * <p>
 * Instances of this type are implemented by the library and supplied where relevant. It must not be implemented by
 * library consumers, as new methods may be added in the future, and user implementations might expose themselves to
 * {@code NoSuchMethodError}s if they interoperate with more up-to-date code.
 */
public interface DeserializeInput extends DeserializeContext {

    /**
     * The entry of the object being deserialized.
     *
     * @return the entry
     */
    @NonNull DataEntry entry();

    /**
     * The actual object which is being deserialized.
     * <p>
     * This is merely {@code entry().getValue()}, meaning it is guaranteed to be one of the canonical values given by
     * {@link DataEntry}: primitives, String, DataList, or DataTree.
     *
     * @return the object
     */
    default @NonNull Object object() {
        return entry().getValue();
    }

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
     * Requires the object to be a list of data entries.
     *
     * @return the object as a data list, or an error result if the type is mismatched
     */
    @NonNull LoadResult<@NonNull DataList> requireDataList();

    /**
     * Makes a child at the given subkey and prepares it for deserialization.
     * <p>
     * The child entry is supposed to be taken "from" this object. For example, an element in a list would be a child
     * entry of the list.
     * <p>
     * The {@code keyPart} argument provides a user recognizable string identifying where the child value is located.
     * For example, list items can provide the index of the element.
     *
     * @param entry the child entry to wrap
     * @param locIdentifier an identifier for the child entry's location, based on {@code toString()}
     * @return deserializable input
     * @deprecated Use {@link #newInputAt(Object, DataEntry)} instead, by swapping the same arguments to this function
     */
    @Deprecated
    default @NonNull DeserializeInput makeChild(@NonNull DataEntry entry, @NonNull Object locIdentifier) {
        return newInputAt(locIdentifier, entry);
    }

}
