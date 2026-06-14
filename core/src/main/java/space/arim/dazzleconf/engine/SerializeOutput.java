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
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataList;
import space.arim.dazzleconf.backend.DataTree;

/**
 * The output for serialization, which is handed to {@link SerializeDeserialize#serialize}.
 * <p>
 * Serializers <b>MUST</b> call one of the output methods in this class. The output methods start with "out" and are
 * distinguished by the argument type, which helps callers ensure that they are passing a valid output type.
 * If no object is output when {@code serialize} returns, that is considered a library usage error, and an exception
 * may be thrown at a later time.
 * <p>
 * <b>Advanced usage</b>
 * <p>
 * This {@code SerializeOutput} stores the last object output to it. Calling more than one "out" method will overwrite
 * the value stored in this instance, which will in turn change the returned output. The output is extracted (and
 * simultaneously cleared) by calling {@link #getAndClearLastOutput()}.
 * <p>
 * <b>Library implementor</b>
 * <p>
 * Instances of this type are implemented by the library and supplied where relevant. It must not be implemented by
 * library consumers, as new methods may be added in the future, and user implementations might expose themselves to
 * {@code NoSuchMethodError}s if they interoperate with more up-to-date code.
 */
public interface SerializeOutput extends SerializeContext {

    /**
     * Outputs a string
     *
     * @param value the string, nonnull
     */
    void outString(@NonNull String value);

    /**
     * Outputs a boolean
     *
     * @param value the boolean
     */
    void outBoolean(boolean value);

    /**
     * Outputs a byte
     *
     * @param value the byte
     */
    void outByte(byte value);

    /**
     * Outputs a character
     *
     * @param value the character
     */
    void outChar(char value);

    /**
     * Outputs a short
     *
     * @param value the short
     */
    void outShort(short value);

    /**
     * Outputs an integer
     *
     * @param value the integer
     */
    void outInt(int value);

    /**
     * Outputs a long
     *
     * @param value the long
     */
    void outLong(long value);

    /**
     * Outputs a float
     *
     * @param value the float
     */
    void outFloat(float value);

    /**
     * Outputs a double
     *
     * @param value the double
     */
    void outDouble(double value);

    /**
     * Outputs a data tree
     *
     * @param value the data tree
     */
    void outDataTree(@NonNull DataTree value);

    /**
     * Outputs a data list
     *
     * @param value the data list
     */
    void outDataList(@NonNull DataList value);

    /**
     * Outputs the explicit absence of a type.
     * <p>
     * This method will store {@link NoOutput#INSTANCE} as the output object..
     *
     */
    void outNone();

    /**
     * Outputs the given object.
     * <p>
     * This function should not be used in normal circumstances. It is a low-level means of setting the output
     * object, intended for when the caller has a ready object but does not know its exact type.
     * <p>
     * The caller guarantees either:
     * <ol>
     *     <li>The passed object is valid according to {@link DataEntry#validateValue(Object)}. That is, a primitive
     *     value, string, {@code DataList}, or {@code DataTree}.</li>
     *     <li>Or the object is {@link NoOutput#INSTANCE}.</li>
     * </ol>
     * If this condition is not met, behavior is <b>not defined</b> and an exception may be thrown at a later point.
     *
     * @param value the object
     */
    void outObjectUnchecked(@NonNull Object value);

    /**
     * Gets the last output, clears it in this {@code SerializeOutput}, and returns it to the caller.
     * <p>
     * One of the following can be returned by this method:
     * <ul>
     *     <li>Primitive values (boxed)</li>
     *     <li>{@code java.lang.String}</li>
     *     <li>{@link DataList}</li>
     *     <li>{@link DataTree}</li>
     *     <li>{@link NoOutput#INSTANCE} to represent explicit lack of output</li>
     *     <li>{@code null} if none of the "out" methods on this type was called, since the output was last cleared.</li>
     * </ul>
     * Because this method also clears the stored value, calling it twice will always yield {@code null}.
     *
     * @return the last object output, or null if there is none
     */
    @Nullable Object getAndClearLastOutput();

}
