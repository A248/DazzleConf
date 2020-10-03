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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;

/**
 * A type which may be adapted to some other. Inbuilt type conversion is extended by the {@link ValueSerialiser}s. <br>
 * <br>
 * All methods throw {@link BadValueException} if type conversion failed at some point.
 * 
 * @author A248
 *
 */
public interface FlexibleType {

	/**
	 * Gets the key associated with this flexible type. This is the key used to construct {@link BadValueException}s
	 * 
	 * @return the key
	 */
	String getAssociatedKey();
	
	/**
	 * Helper method to begin creating a {@link BadValueException} from {@link #getAssociatedKey()}
	 * 
	 * @return an exception builder for {@link BadValueException} with the key already set
	 */
	default BadValueException.Builder badValueExceptionBuilder() {
		return new BadValueException.Builder().key(getAssociatedKey());
	}
	
	/**
	 * Gets the value as a string
	 * 
	 * @return the string
	 * @throws BadValueException if type conversion failed
	 */
	String getString() throws BadValueException;
	
	/**
	 * Gets the value as a boolean
	 * 
	 * @return the boolean
	 * @throws BadValueException if type conversion failed
	 */
	boolean getBoolean() throws BadValueException;
	
	/**
	 * Gets the value as a byte
	 * 
	 * @return the byte
	 * @throws BadValueException if type conversion failed
	 */
	byte getByte() throws BadValueException;
	
	/**
	 * Gets the value as a short
	 * 
	 * @return the short
	 * @throws BadValueException if type conversion failed
	 */
	short getShort() throws BadValueException;
	
	/**
	 * Gets the value as an integer
	 * 
	 * @return the integer
	 * @throws BadValueException if type conversion failed
	 */
	int getInteger() throws BadValueException;
	
	/**
	 * Gets the value as a long
	 * 
	 * @return the long
	 * @throws BadValueException if type conversion failed
	 */
	long getLong() throws BadValueException;
	
	/**
	 * Gets the value as a float
	 * 
	 * @return the float
	 * @throws BadValueException if type conversion failed
	 */
	float getFloat() throws BadValueException;
	
	/**
	 * Gets the value as a double
	 * 
	 * @return the double
	 * @throws BadValueException if type conversion failed
	 */
	double getDouble() throws BadValueException;
	
	/**
	 * Gets the value as a character
	 * 
	 * @return the character
	 * @throws BadValueException if type conversion failed
	 */
	char getCharacter() throws BadValueException;
	
	/**
	 * Gets the value as an enum type
	 * 
	 * @param <T> the enum type
	 * @param enumClass the enum class
	 * @return the enum value
	 * @throws BadValueException if conversion to the enum failed
	 * @throws NullPointerException if {@code enumClass} is null
	 */
	<T extends Enum<T>> T getEnum(Class<T> enumClass) throws BadValueException;
	
	/**
	 * Gets the value as a list of further flexible types. This operation may fail if the underlying config value
	 * is not a collection and {@link ConfigurationOptions#createSingleElementCollections()}
	 * is {@code false}.
	 * 
	 * @return the list of flexible types
	 * @throws BadValueException if this value cannot be made into a list
	 */
	List<FlexibleType> getList() throws BadValueException;
	
	/**
	 * Gets the value as a list of further flexible types ({@link #getList()}) and processes it by converting
	 * each flexible type element to a result element.
	 * 
	 * @param <E> the element type
	 * @param elementProcessor the processor to convert elements
	 * @return the list result
	 * @throws BadValueException if the list cannot be made or any flexible type cannot be made into a result
	 * @throws NullPointerException if {@code elementProcessor} is null
	 */
	<E> List<E> getList(FlexibleTypeFunction<E> elementProcessor) throws BadValueException;
	
	/**
	 * Gets the value as a set of further flexible types. This operation may fail if the underlying config value
	 * is not a collection and {@link ConfigurationOptions#createSingleElementCollections()}
	 * is {@code false}.
	 * 
	 * @return the set of flexible types
	 * @throws BadValueException if this value cannot be made into a set
	 */
	Set<FlexibleType> getSet() throws BadValueException;
	
	/**
	 * Gets the value as a set of further flexible types ({@link #getSet()}) and processes it by converting
	 * each flexible type element to a result element.
	 * 
	 * @param <E> the element type
	 * @param elementProcessor the processor to convert elements
	 * @return the set result
	 * @throws BadValueException if the set cannot be made or any flexible type cannot be made into a result
	 * @throws NullPointerException if {@code elementProcessor} is null
	 */
	<E> Set<E> getSet(FlexibleTypeFunction<E> elementProcessor) throws BadValueException;
	
	/**
	 * Gets the value as a collection of further flexible types. This operation may fail if the underlying config value
	 * is not a collection and {@link ConfigurationOptions#createSingleElementCollections()}
	 * is {@code false}.
	 * 
	 * @return the collection of flexible types
	 * @throws BadValueException if this value cannot be made into a collection
	 */
	Collection<FlexibleType> getCollection() throws BadValueException;
	
	/**
	 * Gets the value as a collection of further flexible types ({@link #getCollection()}) and processes it by
	 * converting each flexible type element to a result element.
	 * 
	 * @param <E> the element type
	 * @param elementProcessor the processor to convert elements
	 * @return the collection result
	 * @throws BadValueException if the collection cannot be made or any flexible type cannot be made into a result
	 * @throws NullPointerException if {@code elementProcessor} is null
	 */
	<E> Collection<E> getCollection(FlexibleTypeFunction<E> elementProcessor) throws BadValueException;
	
	/**
	 * Gets the value as a map of further flexible types. This operation may fail if the underlying
	 * config value is not a map.
	 * 
	 * @return the map of flexible types
	 * @throws BadValueException if this value cannot be made into a map
	 */
	Map<FlexibleType, FlexibleType> getMap() throws BadValueException;
	
	/**
	 * Gets the value as a map of further flexible types ({@link #getMap()}) and processes it by converting each
	 * pair of flexible types to a map entry.
	 * 
	 * @param <K> the value type
	 * @param <V> the value type
	 * @param entryProcessor the processor to convert map entries
	 * @return the map result
	 * @throws BadValueException if the map cannot be made or any flexible type cannot be made into a result
	 * @throws NullPointerException if {@code entryProcessor} is null
	 */
	<K, V> Map<K, V> getMap(FlexibleTypeMapEntryFunction<K, V> entryProcessor) throws BadValueException;
	
	/**
	 * Gets this type as some arbitrary object. This may call other configured {@link ValueSerialiser}s which
	 * have been configured for the defined type. <br>
	 * <br>
	 * If {@code clazz} is String, a primitive, or an enum, this is the same as calling {@link #getString()},
	 * the corresponding primitive getter, or {@link #getEnum(Class)}.
	 * 
	 * @param <T> the target type of the object
	 * @param clazz the target type's class
	 * @return the object
	 * @throws BadValueException if type conversion failed
	 * @throws NullPointerException if {@code clazz} is null
	 */
	<T> T getObject(Class<T> clazz) throws BadValueException;
	
}
