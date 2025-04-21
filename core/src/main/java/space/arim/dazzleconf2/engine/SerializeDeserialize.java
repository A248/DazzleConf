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
    LoadResult<V> deserialize(DeserializeInput object);

    /**
     * Serializes this value. The serialized object must be one of the canonical types, see {@link DataTree}
     *
     * @param value the value
     * @param output where to place the serialized type
     */
    void serialize(V value, SerializeOutput output);

}
