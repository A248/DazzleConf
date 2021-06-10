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
package space.arim.dazzleconf.internal.processor;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.internal.util.ImmutableCollections;
import space.arim.dazzleconf.serialiser.FlexibleType;
import space.arim.dazzleconf.serialiser.FlexibleTypeFunction;
import space.arim.dazzleconf.serialiser.FlexibleTypeMapEntryFunction;
import space.arim.dazzleconf.serialiser.ValueSerialiser;
import space.arim.dazzleconf.serialiser.ValueSerialiserMap;

final class FlexibleTypeImpl implements FlexibleType {

	private final String key;
	/**
	 * The raw object. In some cases, values returned from @DefaultObject methods
	 * may already be an instance of the desired type.
	 *
	 */
	private final Object value;
	private transient final ConfigurationOptions options;
	private transient final ValueSerialiserMap serialisers;
	
	FlexibleTypeImpl(String key, Object value, ConfigurationOptions options,
			ValueSerialiserMap serialisers) {
		this.key = key;
		this.value = value;
		this.options = options;
		this.serialisers = serialisers;
	}
	
	@Override
	public String getAssociatedKey() {
		return key;
	}

	@Override
	public String getString() throws BadValueException {
		return value.toString();
	}

	@Override
	public boolean getBoolean() throws BadValueException {
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		if (value instanceof String) {
			String parsable = (String) value;
			if (parsable.equalsIgnoreCase("true") || parsable.equalsIgnoreCase("yes")) {
				return true;
			}
			if (parsable.equalsIgnoreCase("false") || parsable.equalsIgnoreCase("no")) {
				return false;
			}
		}
		throw badValueExceptionBuilder().message("value " + value + " is not a boolean").build();
	}

	@Override
	public byte getByte() throws BadValueException {
		return getNumber().byteValue();
	}

	@Override
	public short getShort() throws BadValueException {
		return getNumber().shortValue();
	}

	@Override
	public int getInteger() throws BadValueException {
		return getNumber().intValue();
	}

	@Override
	public long getLong() throws BadValueException {
		return getNumber().longValue();
	}

	@Override
	public float getFloat() throws BadValueException {
		return getNumber().floatValue();
	}

	@Override
	public double getDouble() throws BadValueException {
		return getNumber().doubleValue();
	}
	
	private Number getNumber() throws BadValueException {
		if (value instanceof Number) {
			return (Number) value;
		}
		if (value instanceof String) {
			Number parsed;
			try {
				parsed = NumberFormat.getInstance().parse((String) value);
			} catch (ParseException ex) {
				throw badValueExceptionBuilder()
						.message("value " + value + " is not a Number and cannot be converted to one").cause(ex)
						.build();
			}
			return parsed;
		}
		throw badValueExceptionBuilder().message("value " + value + " is not a Number").build();
	}

	@Override
	public char getCharacter() throws BadValueException {
		if (value instanceof Character) {
			return (Character) value;
		}
		String string = getString();
		if (string.length() != 1) {
			throw badValueExceptionBuilder().message("value " + value + " is not a single character").build();
		}
		return string.charAt(0);
	}

	@Override
	public <T extends Enum<T>> T getEnum(Class<T> enumClass) throws BadValueException {
		Objects.requireNonNull(enumClass, "enumClass");
		if (enumClass.isInstance(value)) {
			return enumClass.cast(value);
		}
		String parsable = getString();
		boolean strictParseEnums = options.strictParseEnums();
		for (T enumConstant : enumClass.getEnumConstants()) {
			String name = enumConstant.name();
			boolean equal = (strictParseEnums) ? name.equals(parsable) : name.equalsIgnoreCase(parsable);
			if (equal) {
				return enumConstant;
			}
		}
		throw badValueExceptionBuilder().message("value " + parsable + " is not a " + enumClass.getName()).build();
	}
	
	@SuppressWarnings("unchecked")
	private <T, E extends Enum<E>> T getEnumUnchecked(Class<T> enumClass) throws BadValueException {
		return enumClass.cast(getEnum((Class<E>) enumClass));
	}

	@Override
	public List<FlexibleType> getList() throws BadValueException {
		return getList((flexType) -> flexType);
	}
	
	@Override
	public <E> List<E> getList(FlexibleTypeFunction<? extends E> elementProcessor) throws BadValueException {
		return (List<E>) this.<E>getCollection0(true, elementProcessor);
	}

	@Override
	public Set<FlexibleType> getSet() throws BadValueException {
		return getSet((flexType) -> flexType);
	}
	
	@Override
	public <E> Set<E> getSet(FlexibleTypeFunction<? extends E> elementProcessor) throws BadValueException {
		return (Set<E>) this.<E>getCollection0(false, elementProcessor);
	}

	@Override
	public Collection<FlexibleType> getCollection() throws BadValueException {
		return getCollection((flexType) -> flexType);
	}
	
	@Override
	public <E> Collection<E> getCollection(FlexibleTypeFunction<? extends E> elementProcessor) throws BadValueException {
		return getCollection0(false, elementProcessor);
	}
	
	private <E> Collection<E> getCollection0(boolean ordered, FlexibleTypeFunction<? extends E> elementProcessor)
			throws BadValueException {
		Objects.requireNonNull(elementProcessor, "elementProcessor");
		Collection<?> collection;
		if (value instanceof Collection) {
			collection = (Collection<?>) value;
		} else if (options.createSingleElementCollections()) {
			E singleResult = elementProcessor.getResult(this);
			return (ordered) ? ImmutableCollections.listOf(singleResult) : ImmutableCollections.setOf(singleResult);
		} else {
			throw badValueExceptionBuilder().message(
					"value " + value + " must be a collection or group of elements (List, Collection, Set)").build();
		}
		Collection<E> result = (ordered) ? new ArrayList<>(collection.size()) : new HashSet<>(collection.size());
		for (Object element : collection) {
			result.add(elementProcessor.getResult(deriveFlexibleObject(element)));
		}
		return (ordered) ? ImmutableCollections.listOf(result) : ImmutableCollections.setOf(result);
	}
	
	@Override
	public Map<FlexibleType, FlexibleType> getMap() throws BadValueException {
		return getMap(ImmutableCollections::mapEntryOf);
	}
	
	@Override
	public <K, V> Map<K, V> getMap(FlexibleTypeMapEntryFunction<? extends K, ? extends V> entryProcessor) throws BadValueException {
		Objects.requireNonNull(entryProcessor, "entryProcessor");
		if (!(value instanceof Map)) {
			throw badValueExceptionBuilder().message("value " + value + " is not a Map").build();
		}
		Map<?, ?> map = (Map<?, ?>) value;
		Map<K, V> result = new HashMap<>(map.size());
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Map.Entry<? extends K, ? extends V> processed = entryProcessor.getResult(
					deriveFlexibleObject(entry.getKey()), deriveFlexibleObject(entry.getValue()));
			result.put(processed.getKey(), processed.getValue());
		}
		return ImmutableCollections.mapOf(result);
	}
	
	private FlexibleTypeImpl deriveFlexibleObject(Object value) {
		return new FlexibleTypeImpl(key, value, options, serialisers);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Class<T> clazz) throws BadValueException {
		return (T) getObject0(Objects.requireNonNull(clazz, "clazz"));
	}
	
	private <G> Object getObject0(Class<G> goal) throws BadValueException {
		if (goal == Object.class) {
			return value;
		}
		// Boolean, String, and Character
		if (goal == boolean.class || goal == Boolean.class) {
			return getBoolean();
		} else if (goal == String.class) {
			return getString();
		} else if (goal == char.class || goal == Character.class) {
			return getCharacter();
		}

		// Numbers
		if (goal == Number.class) {
			return getNumber();
		}
		if (goal == int.class || goal == Integer.class) {
			return getInteger();
		} else if (goal == long.class || goal == Long.class) {
			return getLong();
		} else if (goal == short.class || goal == Short.class) {
			return getShort();
		} else if (goal == byte.class || goal == Byte.class) {
			return getByte();
		}
		if (goal == double.class || goal == Double.class) {
			return getDouble();
		} else if (goal == float.class || goal == Float.class) {
			return getFloat();
		}

		// Enums
		if (goal.isEnum()) {
			return getEnumUnchecked(goal);
		}

		// All other types
		if (goal.isInstance(value)) {
			return goal.cast(value);
		}
		return goal.cast(fromSerialiser(getSerialiser(goal)));
	}
	
	private <G> ValueSerialiser<G> getSerialiser(Class<G> goal) {
		return serialisers.getSerialiserFor(goal).orElseThrow(
				() -> new IllDefinedConfigException("No ValueSerialiser for " + goal + " at entry " + key));
	}
	
	private <G> G fromSerialiser(ValueSerialiser<G> serialiser) throws BadValueException {
		G deserialised = serialiser.deserialise(this);
		if (deserialised == null) {
			throw new IllDefinedConfigException(
					"At key " + key + ", ValueSerialiser#deserialise for " + serialiser + " returned null");
		}
		return deserialised;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + key.hashCode();
		result = prime * result + value.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof FlexibleTypeImpl)) {
			return false;
		}
		FlexibleTypeImpl other = (FlexibleTypeImpl) object;
		return key.equals(other.key) && value.equals(other.value);
	}

	@Override
	public String toString() {
		return "FlexibleTypeImpl [key=" + key + ", value=" + value + "]";
	}
	
}
