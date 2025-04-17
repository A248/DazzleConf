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

import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.DataTree;

import java.util.List;

/**
 * A serializer.
 *
 * @param <V> the deserialized type
 */
public interface SerializeDeserialize<V> {

    /**
     * Deserializes using the given operable object
     *
     * @param object the operable object
     * @return the deserialized value if successful, or an error result if not
     */
    LoadResult<V> deserialize(OperableObject object);

    /**
     * Serializes this value. The serialized object must be one of the canonical types, see {@link DataTree}
     *
     * @param value the value
     * @param output where to place the serialized type
     */
    void serialize(V value, SerializeOutput output);

    interface SerializeOutput {

        /**
         * Gets the key mapper being used
         *
         * @return the key mapper
         */
        KeyMapper keyMapper();

        /**
         * Outputs a string
         * @param value the string, nonnull
         */
        void outString(String value);

        /**
         * Outputs a boolean
         * @param value the boolean
         */
        void outBoolean(boolean value);

        /**
         * Outputs a byte
         * @param value the byte
         */
        void outByte(byte value);

        /**
         * Outputs a character
         * @param value the character
         */
        void outChar(char value);

        /**
         * Outputs a short
         * @param value the short
         */
        void outShort(short value);

        /**
         * Outputs an integer
         * @param value the integer
         */
        void outInt(int value);

        /**
         * Outputs a long
         * @param value the long
         */
        void outLong(long value);

        /**
         * Outputs a float
         * @param value the float
         */
        void outFloat(float value);

        /**
         * Outputs a double
         * @param value the double
         */
        void outDouble(double value);

        /**
         * Outputs a list.
         * <p>
         * The list elements will be checked to conform with the canonical types. Note that mixing types in the same
         * list, however, is allowed.
         * @param value the list, nonnull
         */
        void outList(List<?> value);

        /**
         * Outputs a data tree
         * @param value the data tree, nonnull
         */
        void outDataTree(DataTree value);
    }
}
