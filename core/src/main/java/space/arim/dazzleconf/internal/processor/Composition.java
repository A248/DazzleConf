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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import space.arim.dazzleconf.annote.CollectionSize;
import space.arim.dazzleconf.annote.IntegerRange;
import space.arim.dazzleconf.annote.NumericRange;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.internal.SingleConfEntry;
import space.arim.dazzleconf.internal.util.ImmutableCollections;
import space.arim.dazzleconf.serialiser.FlexibleType;
import space.arim.dazzleconf.serialiser.FlexibleTypeFunction;

class Composition {

	private final SingleConfEntry entry;
	private final FlexibleType flexType;
	
	Composition(SingleConfEntry entry, FlexibleType flexType) {
		this.entry = entry;
		this.flexType = flexType;
	}
	
	Object processObject() throws BadValueException {
		return processObjectWithGoal(entry.getMethod().getReturnType());
	}
	
	private Method method() {
		return entry.getMethod();
	}
	
	private <G> Object processObjectWithGoal(Class<G> goal)
			throws BadValueException {
		// Numerics types can't be delegated to FlexibleType, since @IntegerRange/@NumericRange need to be checked
		if (goal == int.class || goal == Integer.class) {
			return getAsNumber().intValue();
		} else if (goal == long.class || goal == Long.class) {
			return getAsNumber().longValue();
		} else if (goal == short.class || goal == Short.class) {
			return getAsNumber().shortValue();
		} else if (goal == byte.class || goal == Byte.class) {
			return getAsNumber().byteValue();
		}
		if (goal == double.class || goal == Double.class) {
			return getAsNumber().doubleValue();
		} else if (goal == float.class || goal == Float.class) {
			return getAsNumber().floatValue();
		}
		/*
		 * Same goes for Collections and Maps with @CollectionSize.
		 * Collections and Maps also need to call getList/getSet/getCollection/getMap.
		 */
		if (goal == List.class || goal == Set.class || goal == Collection.class) {
			Class<?> elementType = entry.getCollectionElementType();
			return getCollection(goal, elementType);
		}
		if (goal == Map.class) {
			Class<?> keyType = entry.getMapKeyType();
			Class<?> valueType = entry.getMapValueType();
			return getMap(keyType, valueType);
		}

		// Everything else
		return flexType.getObject(goal);
	}
	
	private <E> Collection<E> getCollection(Class<?> goal, Class<E> elementType)
			throws BadValueException {

		FlexibleTypeFunction<E> function = (element) -> element.getObject(elementType);
		Collection<E> collection;
		if (goal == List.class) {
			collection = flexType.getList(function);
		} else if (goal == Set.class) {
			collection = flexType.getSet(function);
		} else if (goal == Collection.class) { 
			collection = flexType.getCollection(function);
		} else {
			throw new IllegalArgumentException("Internal error: Unknown goal " + goal + ", expected List/Set/Collection");
		}
		checkSize(collection.size());
		return collection;
	}
	
	private <K, V> Map<K, V> getMap(Class<K> keyType, Class<V> valueType)
			throws BadValueException {
		Map<K, V> map = flexType.getMap((flexibleKey, flexibleValue) -> {
			K key = flexibleKey.getObject(keyType);
			V value = flexibleValue.getObject(valueType);
			return ImmutableCollections.mapEntryOf(key, value);
		});
		checkSize(map.size());
		return map;
	}
	
	private void checkSize(int size) throws BadValueException {
		CollectionSize sizing = method().getAnnotation(CollectionSize.class);
		if (sizing != null) {
			if (size < sizing.min()) {
				throw flexType.badValueExceptionBuilder()
						.message("value's size " + size + " is less than minimum size " + sizing.min()).build();
			}
			if (size > sizing.max()) {
				throw flexType.badValueExceptionBuilder().message(
						"value's size " + size + " is more than maximum size " + sizing.max()).build();
			}
		}
	}
	
	private Number getAsNumber() throws BadValueException {
		Number number = flexType.getObject(Number.class);
		checkRange(number);
		return number;
	}
	
	private void checkRange(Number number) throws BadValueException {
		NumericRange numericRange = method().getAnnotation(NumericRange.class);
		if (numericRange != null) {
			double asDouble = number.doubleValue();
			if (asDouble < numericRange.min()) {
				throw flexType.badValueExceptionBuilder()
						.message("value's size " + asDouble + " is less than minimum size " + numericRange.min()).build();
			}
			if (asDouble > numericRange.max()) {
				throw flexType.badValueExceptionBuilder()
						.message("value's size " + asDouble + " is more than maximum size " + numericRange.max()).build();
			}
		}
		IntegerRange intRange = method().getAnnotation(IntegerRange.class);
		if (intRange != null) {
			long asLong = number.longValue();
			if (asLong < intRange.min()) {
				throw flexType.badValueExceptionBuilder()
						.message("value's size " + asLong + " is less than minimum size " + intRange.min()).build();
			}
			if (asLong > intRange.max()) {
				throw flexType.badValueExceptionBuilder()
						.message("value's size " + asLong + " is more than maximum size " + intRange.max()).build();
			}
		}
	}
	
}
