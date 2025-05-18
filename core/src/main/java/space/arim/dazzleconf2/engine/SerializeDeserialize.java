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
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.reflect.TypeToken;

/**
 * A serializer.
 * <p>
 * This class is responsible for loading values and converting them into the appropriate type, and vice versa.
 * <p>
 * Implementations can also perform updates of the source value. Such updating is performed by the default method
 * {@link #deserializeUpdate(DeserializeInput, SerializeOutput)}, which more advanced {@code SerializeDeserialize}
 * implementations should override.
 * <p>
 * <b>Dependencies</b>
 * <p>
 * Many serializers for complex types will rely on dependent serializers, such as for assembly line-style processing
 * or for handling parts of a greater object. In that case, exceptions thrown by dependent serializers need not be
 * caught: they can simply be re-thrown to the caller.
 * <p>
 * Additionally, dependent serializers can be relied upon to adhere to contracts or reasonable expectations.
 * If they do not, an implementation is permitted to throw its own {@code DeveloperMistakeException} stating as such.
 * An example of a reasonable expectation would be assuming that a serializer for {@code Integer} (unannotated) can
 * successfully process the integer <code>1</code>.
 * <p>
 * <b>API Note</b>
 * <p>
 * This class is the closest equivalent to version 1's "ValueSerialiser." Please use
 * {@link ConfigurationBuilder#addSimpleSerializer(TypeToken, SerializeDeserialize)} if you would like a convenient
 * method to attach an implementation of this class.
 *
 * @param <V> the deserialized type
 */
public interface SerializeDeserialize<V> {

    /**
     * Deserializes using the given operable object.
     * <p>
     * Yields a {@code V} if successful, or an error result otherwise.
     * <p>
     * <b>Handling Input</b>
     * <p>
     * The input value is passed as {@link DeserializeInput#object()}. This object is guaranteed to be one of the
     * canonical types according to {@link DataEntry#validateValue(Object)}.
     * <p>
     * The convenience methods {@code requireString()} and {@code requireDataTree()} on {@code DeserializeInput} allow
     * implementors of this method to access these types as preconditions, while rejecting the input if it is of
     * another type.
     * <p>
     * <b>Handling Errors and Imperfection</b>
     * <p>
     * If the input data is not of proper type or representation, implementations must return an error result.
     * Implementations are recommended to use the factory methods on {@code DeserializeInput} to build and throw
     * error values.
     * <p>
     * Implementations may use a degree of leniency in parsing input values. For example, <code>"1"</code> (despite
     * being a string) might be treated the same as <code>1</code> by an integer serializer. Leniency is a decision of
     * the implementation.
     * <p>
     * At the same time, data which is truly malformatted -- not recognizable as the output type in any sense --
     * <b>must</b> be treated by returning an error result. Fallback behavior, like returning a substitute value,
     * is not allowed and should be controlled by other areas of the library.
     *
     * @param deser the object being deserialized, and its associated context
     * @return the deserialized value if successful, or an error result if not
     * @throws DeveloperMistakeException if a dependent serializer behaved incorrectly
     */
    @NonNull LoadResult<@NonNull V> deserialize(@NonNull DeserializeInput deser);

    /**
     * Deserializes using the given operable object, and updates the input value if needed.
     * <p>
     * Please see {@link #deserialize(DeserializeInput)} for deserialization-related considerations. Generally, this
     * function and that function should behave identically with respect to the value deserialized; that is, equivalent
     * inputs should produce equivalent {@code LoadResult}s.
     * <p>
     * <b>Performing Updates</b>
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
     * By default, this method simply calls {@code deserialize} and does not perform an updates. More advanced
     * {@code SerializeDeserialize} implementations should override this function and decide to update if needed.
     *
     * @param deser the object being deserialized, and its associated context
     * @param updateTo if the input object needs to be updated, this is where the updated value should be placed
     * @return the deserialized value if successful, or an error result if not
     * @throws DeveloperMistakeException if a dependent serializer behaved incorrectly
     */
    default @NonNull LoadResult<@NonNull V> deserializeUpdate(@NonNull DeserializeInput deser,
                                                              @NonNull SerializeOutput updateTo) {
        return deserialize(deser);
    }

    /**
     * Serializes this value.
     * <p>
     * The serialized object must be one of the canonical types, see {@link DataEntry#validateValue(Object)}. The
     * implementation <b>MUST</b> output a value before returning; if it does not, a {@code DeveloperMistakeException}
     * may be thrown by another library component.
     *
     * @param value the value
     * @param ser where to place the serialized type
     * @throws DeveloperMistakeException if a dependent serializer behaved incorrectly
     */
    void serialize(@NonNull V value, @NonNull SerializeOutput ser);

}
