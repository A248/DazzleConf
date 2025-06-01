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
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;

import java.util.Locale;

/**
 * An object from a data tree, that is being processed for deserialization. See {@link DataTree}
 *
 */
public interface DeserializeInput {

    /**
     * The locale to format error messages in.
     *
     * @return the locale
     */
    @NonNull Locale getLocale();

    /**
     * The actual object which is being deserialized.
     * <p>
     * This is guaranteed to be one of the canonical values used in {@link DataTree}, i.e. primitives,
     * <code>String</code>, or <code>DataTree</code>, or an immutable <code>List</code> of one of those types.
     *
     * @return the object
     */
    @NonNull Object object();

    /**
     * Gets the absolute key path where the object was loaded from. This will automatically include all key parts from
     * the configuration root all the way until the current entry.
     *
     * @return an absolute key path
     */
    @NonNull KeyPath absoluteKeyPath();

    /**
     * Gets the key mapper.
     * <p>
     * The key mapper is whichever key mapper is being used (or might have been recommended by
     * {@link Backend#recommendKeyMapper()} even if no key mapper was set on the configuration. It is provided here
     * for purposes of deserializing child options in configuration subsections.
     *
     * @return the key mapper, never
     */
    @NonNull KeyMapper keyMapper();

    /**
     * Requires the object to be a string
     *
     * @return the object as a string, or an error result if the type is mismatched
     */
    @NonNull LoadResult<@NonNull String> requireString();

    /**
     * Requires the object to be a data tree, i.e. a map of key/value pairs.
     *
     * @return the object as a data tree, or an error result if the type is mismatched
     */
    @NonNull LoadResult<@NonNull DataTree> requireDataTree();

    /**
     * Signals that the data tree could use an update with respect to this object. For example, this might happen if
     * missing options were filled in with default values, and those default values need to be written to the backend.
     * <p>
     * This function does not <b>actually</b> perform any updating. It is merely a notification that this object
     * (or a part within it) could be updated. For actual in-place updates, make sure to implement
     * {@link SerializeDeserialize#deserializeUpdate(DeserializeInput, SerializeOutput)}
     * <p>
     * If the path being updated is a sub-path of this one, then that sub-path should be provided as a non-empty
     * parameter. An empty path should be passed if no sub-path exists.
     *
     * @param subPath the sub path to be updated. May be empty if none exists. This path is relative to the
     *                location of the current object, meaning it should not overlap with {@link #absoluteKeyPath()}
     * @param updateReason the reason that such path might be updated. May be {@code UpdateReason.OTHER} if unknown
     */
    void flagUpdate(@NonNull KeyPath subPath, @NonNull UpdateReason updateReason);

    /**
     * Makes a child and prepares it for deserialization. The child value is supposed to be taken "from" this object.
     * For example, an element in a list would be a child object of the list.
     *
     * @param value the child value to wrap
     * @return deserializable input
     */
    @NonNull DeserializeInput makeChild(@NonNull Object value);

    /**
     * Builds an error context based on the implementation
     *
     * @param message the main error messge
     * @return an error context
     */
    @NonNull ErrorContext buildError(@NonNull CharSequence message);

    /**
     * Builds an error context, wraps it in a <code>LoadResult</code> and returns it.
     * <p>
     * This function does not actually throw anything. It is named as such so that your code will look like this:
     * <pre>
     *     {@code
     *         return operable.throwError("failure");
     *     }
     * </pre>
     *
     * @param message the main error message
     * @return an error result
     * @param <R> the type of the result value (can be anything since the result will be an error)
     */
    <R> @NonNull LoadResult<R> throwError(@NonNull CharSequence message);
}
