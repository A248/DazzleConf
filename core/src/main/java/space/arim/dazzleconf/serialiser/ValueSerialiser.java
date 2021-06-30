/* 
 * DazzleConf-core
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-core. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.serialiser;

import space.arim.dazzleconf.error.BadValueException;

/**
 * User{@literal -}implemented interface for serializing and deserializing configuration entries. <br>
 * <br>
 * It should always be the case that if deserialization succeeds, serializing the same value should succeed.
 * That is, {@code deserialise} and {@code serialise} should be inverse operations.
 * 
 * @author A248
 *
 * @param <T> the target type of deserialization
 */
public interface ValueSerialiser<T> {

	/**
	 * Gets the target type of deserialization
	 * 
	 * @return the target class type
	 */
	Class<T> getTargetClass();
	
	/**
	 * Deserializes a value from a flexible type representing a raw config value. <br>
	 * <br>
	 * The key ({@link FlexibleType#getAssociatedKey()}) is informative. It should not affect the deserialization, but should
	 * be included in thrown {@code BadValueException}s to inform the user which key is in question. To easily include
	 * the key in a {@code BadValueException}, use {@link FlexibleType#badValueExceptionBuilder()}
	 * 
	 * @param flexibleType the flexible type
	 * @return the deserialized value, never null
	 * @throws BadValueException if the value could not be deserialized
	 */
	T deserialise(FlexibleType flexibleType) throws BadValueException;
	
	/**
	 * Serializes a value to a raw config value. Should be the inverse operation of {@link #deserialise(FlexibleType)}
	 * in that if the result of this method were wrapped in a {@link FlexibleType}, it could be deserialized. <br>
	 * <br>
	 * If this serializer serializes values by converting from another custom type, then the provided {@code decomposer}
	 * should be used to reserialize such custom types.
	 * 
	 * @param value the value
	 * @param decomposer the decomposer used to help breakdown the value
	 * @return the serialized value
	 */
	Object serialise(T value, Decomposer decomposer);
	
}
