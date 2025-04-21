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
import space.arim.dazzleconf2.ConfigurationBuilder;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.reflect.TypeToken;

/**
 * A serializer.
 * <p>
 * This class is responsible for loading values and converting them into the appropriate type, and vice versa.
 * <p>
 * <b>API Note</b>
 * This class is the closest equivalent to version 1's "ValueSerialiser." Please use
 * {@link ConfigurationBuilder#addSimpleSerializer(TypeToken, SerializeDeserialize)} if you would like a convenient
 * method to attach an implementation of this class.
 *
 * @param <V> the deserialized type
 */
public interface SerializeDeserialize<V> {

    /**
     * Deserializes using the given operable object
     *
     * @param deser the object being deserialized, and its associated context
     * @return the deserialized value if successful, or an error result if not
     */
    @NonNull LoadResult<@NonNull V> deserialize(@NonNull DeserializeInput deser);

    /**
     * Deserializes using the given operable object, and updates the input value if needed.
     * <p>
     * Sometimes types have a canonical representation in human readable configuration files, but they also have
     * alternate (and potentially less efficient) ways of writing them. For example, <code>"1"</code> is a number for
     * human purposes, but machines would interpret it as a string. This function exists to "correct" such usages by
     * overwriting the preferred representation of the given type.
     * <p>
     * Proceeding with the previous example, an implementation for integers might load both <code>1</code> and
     * <code>"1"</code> as an integer. But only if <code>"1"</code> was the input, the implementation might want to
     * signal that this value should be updated:
     * <pre>
     *     {@code
     *     if (deser.getValue() instanceof Integer) {
     *         // No update necessary
     *         return (Integer) deser.getValue();
     *     }
     *     return deser.requireString().flatMap((stringVal) -> {
     *         int intVal;
     *         try {
     *             intVal = Integer.parseInt(intVal);
     *         } catch (NumberFormatException ignored) {
     *             return deser.throwError("Not a valid integer " + stringVal);
     *         }
     *         // Updating happens here
     *         updateTo.outInt(intVal);

     *         return LoadResult.of(intVal);
     *     });
     *     }
     * </pre>
     * <p>
     * <b>Default Implementation</b>
     * <p>
     * By default, this method simply combines {@link #deserialize} and {@link #serialize} in order to reserialize the
     * loaded value every single time. In the common case, this results in double the work (deserializing <b>and</b>
     * serializing) whenever a configuration is loaded. This may be somewhat inefficient, but it is both feasible for
     * most simple types and a surefire way to ensure accuracy. More advanced {@code SerializeDeserialize}
     * implementations should override this function to handle when writing back the object is really needed.
     *
     * @param deser the object being deserialized, and its associated context
     * @param updateTo if the input object needs to be updated, this is where it should be placed
     * @return the deserialized value if successful, or an error result if not
     */
    default @NonNull LoadResult<@NonNull V> deserializeUpdate(@NonNull DeserializeInput deser,
                                                              @NonNull SerializeOutput updateTo) {
        LoadResult<V> result = deserialize(deser);
        if (result.isSuccess()) {
            serialize(result.getOrThrow(), updateTo);
        }
        return result;
    }

    /**
     * Serializes this value.
     * <p>
     * The serialized object must be one of the canonical types, see {@link DataTree}. The implementation <b>MUST</b>
     * output a value before returning; if it does not, {@code DeveloperMistakeException} may be thrown by another
     * library component.
     *
     * @param value the value
     * @param ser where to place the serialized type
     */
    void serialize(@NonNull V value, @NonNull SerializeOutput ser);

}
