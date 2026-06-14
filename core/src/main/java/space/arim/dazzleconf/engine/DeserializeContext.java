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
import space.arim.dazzleconf.ConfigurationDefinition;
import space.arim.dazzleconf.ErrorContext;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.KeyPath;

/**
 * A context for deserialization.
 * <p>
 * This context identifies the location where deserialization is taking place, as well as global settings like the key
 * mapper. It extends {@link ConfigurationDefinition.ReadOptions} and can be immediately used as such.
 * <p>
 * <b>Library implementor</b>
 * <p>
 * Instances of this type are implemented by the library and supplied where relevant. It must not be implemented by
 * library consumers, as new methods may be added in the future, and user implementations might expose themselves to
 * {@code NoSuchMethodError}s if they interoperate with more up-to-date code.
 */
public interface DeserializeContext extends ConfigurationDefinition.ReadOptions, ErrorContext.Source, OperationContext<DeserializeContext> {

    /**
     * Creates a child context at the given key, with an object prepared for deserialization there.
     * <p>
     * This is the standard method for obtaining a deserialiation input when, logically, an object that is "from" the
     * current one must be deserialized as well. For example, elements of a list should call this method once for each
     * element, with the list index as the {@code locIdentifier}.
     * <p>
     * This method is equivalent to:
     * <pre>
     *     {@code
     *         deriveContext(locIdentifier).newInputHere(entry);
     *     }
     * </pre>
     *
     * @param locIdentifier an identifier for the child input's location, based on {@code toString()}
     * @param entry the entry to prepare to deserialize
     * @return the deserialize input
     */
    default @NonNull DeserializeInput newInputAt(@NonNull Object locIdentifier, @NonNull DataEntry entry) {
        return deriveContext(locIdentifier).newInputHere(entry);
    }

    /**
     * Prepares another object for deserialization.
     * <p>
     * Before calling this method, it is <b>almost always</b> needed to create a child context via
     * {@link #deriveContext(Object)}. Otherwise, the object will be deserialized at the same key as the current
     * context, which can create confusing error messages.
     *
     * @param entry the entry to prepare for deserialization
     * @return the deserialize input
     */
    @NonNull DeserializeInput newInputHere(@NonNull DataEntry entry);

    /**
     * Signals that the data could use an update with respect to this object. For example, this might happen if
     * missing options were filled in with default values, and those default values need to be written to the backend.
     * <p>
     * This function does not actually perform any updating. It is merely a notification that this object (or a part
     * within it) is updatable. For actual in-place updates, make sure to implement
     * {@link SerializeDeserialize#deserializeUpdate(DeserializeInput, SerializeOutput)} and submit an updated value
     * to the {@code SerializeOutput} in the same place as where you call this method.
     * <p>
     * If the path being updated is a sub-path of the current context, then that sub-path should be provided as a
     * non-empty parameter. An empty path should be passed if no sub-path exists.
     *
     * @param subPath the sub path to be updated. May be empty if none exists. This path is relative to the
     *                location of the current context, meaning it should not overlap with {@link #keyPath()}
     * @param updateReason the reason that such path might be updated. May be {@code UpdateReason.OTHER} if unknown
     */
    @Override
    void notifyUpdate(@NonNull KeyPath subPath, @NonNull UpdateReason updateReason);

}
