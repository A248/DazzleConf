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

import space.arim.dazzleconf.annote.CollectionSize;
import space.arim.dazzleconf.annote.IntegerRange;
import space.arim.dazzleconf.annote.NumericRange;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.type.CollectionKind;
import space.arim.dazzleconf.internal.type.CollectionReturnType;
import space.arim.dazzleconf.internal.type.MapReturnType;
import space.arim.dazzleconf.internal.type.ReturnType;
import space.arim.dazzleconf.internal.type.SimpleSubSectionReturnType;
import space.arim.dazzleconf.internal.type.SubSectionCollectionReturnType;
import space.arim.dazzleconf.internal.type.SubSectionMapReturnType;
import space.arim.dazzleconf.internal.util.ImmutableCollections;
import space.arim.dazzleconf.internal.util.UncheckedInvalidConfigException;
import space.arim.dazzleconf.serialiser.FlexibleType;
import space.arim.dazzleconf.serialiser.FlexibleTypeFunction;
import space.arim.dazzleconf.serialiser.FlexibleTypeMapEntryFunction;

import java.util.Collection;
import java.util.Map;

class Composition {

	private final ProcessorBase<?> processor;
	private final ConfEntry entry;
	private final Object preValue;
	private final FlexibleType flexType;
	
	Composition(ProcessorBase<?> processor, ConfEntry entry, Object preValue, FlexibleType flexType) {
		this.processor = processor;
		this.entry = entry;
		this.preValue = preValue;
		this.flexType = flexType;
	}
	
	Object processObject() throws InvalidConfigException {
		ReturnType<?> returnType = entry.returnType();
		if (returnType instanceof SimpleSubSectionReturnType) {
			return processor.createNested(entry, (SimpleSubSectionReturnType<?>) returnType, preValue);
		}
		/*
		 * For numerics types, collections, and maps, the validation annotations
		 * @IntegerRange, @NumericRange, and @CollectionSize need to be checked
		 */
		if (returnType instanceof CollectionReturnType) {
			return getCollection((CollectionReturnType<?, ?>) returnType);
		}
		if (returnType instanceof MapReturnType) {
			return getMap((MapReturnType<?, ?>) returnType);
		}
		Class<?> goal = returnType.typeInfo().rawType();
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
		// Everything else
		return flexType.getObject(goal);
	}
	
	private <E, R extends Collection<E>> R getCollection(CollectionReturnType<E, R> returnType)
			throws InvalidConfigException {
		FlexibleTypeFunction<E> function;
		if (returnType instanceof SubSectionCollectionReturnType) {
			SubSectionCollectionReturnType<E, R> subSectionReturnType = (SubSectionCollectionReturnType<E, R>) returnType;
			function = (element) -> {
				try {
					return processor.createNested(entry, subSectionReturnType, element.getObject(Object.class));
				} catch (InvalidConfigException ex) {
					throw new UncheckedInvalidConfigException(ex);
				}
			};
		} else {
			Class<E> rawElementType = returnType.elementTypeInfo().rawType();
			function = (element) -> element.getObject(rawElementType);
		}
		Collection<E> collection;
		try {
			collection = getCollectionUsing(returnType.collectionKind(), function);
		} catch (UncheckedInvalidConfigException ex) {
			throw ex.getCause();
		}
		checkSize(collection.size());
		@SuppressWarnings("unchecked")
		R castedCollection = (R) collection;
		return castedCollection;
	}

	private <E> Collection<E> getCollectionUsing(CollectionKind kind,
												 FlexibleTypeFunction<E> function) throws BadValueException {
		switch (kind) {
		case COLLECTION:
			return flexType.getCollection(function);
		case SET:
			return flexType.getSet(function);
		case LIST:
			return flexType.getList(function);
		default:
			throw new IllegalArgumentException("Internal error: Unknown collection kind " + kind);
		}
	}
	
	private <K, V> Map<K, V> getMap(MapReturnType<K, V> returnType) throws InvalidConfigException {
		Class<K> rawKeyType = returnType.keyTypeInfo().rawType();
		FlexibleTypeMapEntryFunction<K, V> function;
		if (returnType instanceof SubSectionMapReturnType) {
			SubSectionMapReturnType<K, V> subSectionReturnType = (SubSectionMapReturnType<K, V>) returnType;
			function = (flexibleKey, flexibleValue) -> {
				K key = flexibleKey.getObject(rawKeyType);
				V value;
				try {
					value = processor.createNested(entry, subSectionReturnType, flexibleValue.getObject(Object.class));
				} catch (InvalidConfigException ex) {
					throw new UncheckedInvalidConfigException(ex);
				}
				return ImmutableCollections.mapEntryOf(key, value);
			};
		} else {
			Class<V> rawValueType = returnType.valueTypeInfo().rawType();
			function = (flexibleKey, flexibleValue) -> {
				K key = flexibleKey.getObject(rawKeyType);
				V value = flexibleValue.getObject(rawValueType);
				return ImmutableCollections.mapEntryOf(key, value);
			};
		}
		Map<K, V> map;
		try {
			map = flexType.getMap(function);
		} catch (UncheckedInvalidConfigException ex) {
			throw ex.getCause();
		}
		checkSize(map.size());
		return map;
	}
	
	private void checkSize(int size) throws BadValueException {
		CollectionSize sizing = entry.getMethod().getAnnotation(CollectionSize.class);
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
		NumericRange numericRange = entry.getMethod().getAnnotation(NumericRange.class);
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
		IntegerRange intRange = entry.getMethod().getAnnotation(IntegerRange.class);
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
