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
 * Functional interface for serialising and deserialising configuration entries. <br>
 * <br>
 * It should always be the case that if deserialisation succeeds, serialising the same value should succeed.
 * That is, {@code deserialise} and {@code serialise} should be inverse operations.
 * 
 * @author A248
 *
 * @param <T> the target type of deserialisation
 */
public interface ValueSerialiser<T> {

	/**
	 * Gets the target type of deserialisation
	 * 
	 * @return the target class type
	 */
	Class<T> getTargetClass();
	
	/**
	 * Deserialises a value from a flexible type representing a raw config value. <br>
	 * <br>
	 * The key ({@link FlexibleType#getAssociatedKey()}) is informative. It should not affect the deserialisation, but should
	 * be included in thrown {@code BadValueException}s to inform the user which key is in question.
	 * 
	 * @param flexibleType the flexible type
	 * @return the deserialised value, never {@code null}
	 * @throws BadValueException if the value could not be deserialised
	 */
	T deserialise(FlexibleType flexibleType) throws BadValueException;
	
	/**
	 * Serialises a value to a raw config value. Should be the inverse operation of {@link #deserialise(FlexibleType)}
	 * in that if the result of this method were wrapped in a {@link FlexibleType}, it could be deserialised.
	 * 
	 * @param value the value
	 * @return the serialised value
	 */
	Object serialise(T value);
	
}
