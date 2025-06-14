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
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;

/**
 * A context for deserialization.
 * <p>
 * This context identifies the location where deserialization is taking place, as well as global settings like the key
 * mapper.
 * <p>
 * <b>Implementation</b>
 * <p>
 * Instances of this type are implemented by the library and supplied during type deserialization handling. Equality
 * is not defined, and no thread safety is provided.
 * <p>
 * This type should not be implemented by library consumers. New methods may be added in the future, and this interface
 * should be considered sealed. If library consumers decide to implement this interface, they might expose themselves
 * to {@code NoSuchMethodError}s if they pass their implementation to more up-to-date liaisons.
 */
public interface DeserializeContext extends ErrorContext.Source {

    /**
     * Gets the key mapper.
     * <p>
     * The key mapper is whichever key mapper is being used for the read operation.
     * <p>
     * If using {@link Configuration#configureWith(Backend)}, the key mapper may have been recommended by
     * {@link Backend#recommendKeyMapper()} even if no key mapper was set on the configuration. It is provided here
     * for purposes of deserializing child options in configuration subsections.
     *
     * @return the key mapper
     */
    @NonNull KeyMapper keyMapper();

    /**
     * Gets the absolute key path of the enclosing context.
     * <p>
     * This path will automatically include all key parts from the configuration root all the way until the current
     * entry. It should be used for diagnostic purposes, as it has no functional meaning, and might include debug
     * symbols (like "$0" for list entries).
     *
     * @return the absolute key path
     */
    @NonNull KeyPath absoluteKeyPath();

    /**
     * Signals that the data could use an update with respect to this object. For example, this might happen if
     * missing options were filled in with default values, and those default values need to be written to the backend.
     * <p>
     * This function does not <b>actually</b> perform any updating. It is merely a notification that this object
     * (or a part within it) is updatable. For actual in-place updates, make sure to implement
     * {@link SerializeDeserialize#deserializeUpdate(DeserializeInput, SerializeOutput)} and submit an updated value
     * to the {@code SerializeOutput} in the same place as where you call this method.
     * <p>
     * If the path being updated is a sub-path of the current context, then that sub-path should be provided as a
     * non-empty parameter. An empty path should be passed if no sub-path exists.
     *
     * @param subPath the sub path to be updated. May be empty if none exists. This path is relative to the
     *                location of the current context, meaning it should not overlap with {@link #absoluteKeyPath()}
     * @param updateReason the reason that such path might be updated. May be {@code UpdateReason.OTHER} if unknown
     */
    void flagUpdate(@NonNull KeyPath subPath, @NonNull UpdateReason updateReason);

}
