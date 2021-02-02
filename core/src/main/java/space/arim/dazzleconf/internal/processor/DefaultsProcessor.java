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

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.ConfDefault.*;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.type.ReturnType;
import space.arim.dazzleconf.internal.type.SimpleSubSectionReturnType;
import space.arim.dazzleconf.internal.util.ImmutableCollections;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DefaultsProcessor<C> extends ProcessorBase<C> {

	public DefaultsProcessor(ConfigurationOptions options, ConfigurationDefinition<C> definition) {
		super(options, definition, null);
	}
	
	@Override
	<N> N createChildConfig(ConfigurationOptions options, ConfigurationDefinition<N> childDefinition,
							String key, Object preValue, N nestedAuxiliaryValues) throws InvalidConfigException {
		if (nestedAuxiliaryValues != null) {
			throw new AssertionError("Internal error: DefaultsProcessor does not handle auxiliary entries");
		}
		if (preValue instanceof ConfigurationDefinition) {
			// Simple sub-section
			return createFromProcessor(new DefaultsProcessor<>(options, childDefinition));
		} else {
			// Sub-section in a collection
			Class<N> configClass = childDefinition.getConfigClass();
			if (!configClass.isInstance(preValue)) {
				throw new IllDefinedConfigException(
						"Default value at " + key + " must be an instance of " + configClass);
			}
			return configClass.cast(preValue);
		}
	}

	@Override
	Object getValueFromSources(ConfEntry entry) throws InvalidConfigException {
		ReturnType<?> returnType = entry.returnType();
		if (returnType instanceof SimpleSubSectionReturnType) {
			// Signal to #createChildProcessor that this is a simple sub-section
			return ((SimpleSubSectionReturnType<?>) returnType).configDefinition();
		}
		Method method = entry.getMethod();
		{
			DefaultBoolean ofBoolean = method.getAnnotation(DefaultBoolean.class);
			if (ofBoolean != null) {
				return ofBoolean.value();
			}
			DefaultBooleans ofBooleans = method.getAnnotation(DefaultBooleans.class);
			if (ofBooleans != null) {
				return toList(ofBooleans.value());
			}
		}
		{
			DefaultInteger ofInteger = method.getAnnotation(DefaultInteger.class);
			if (ofInteger != null) {
				return ofInteger.value();
			}
			DefaultIntegers ofIntegers = method.getAnnotation(DefaultIntegers.class);
			if (ofIntegers != null) {
				return toList(ofIntegers.value());
			}
		}
		{
			DefaultLong ofLong = method.getAnnotation(DefaultLong.class);
			if (ofLong != null) {
				return ofLong.value();
			}
			DefaultLongs ofLongs = method.getAnnotation(DefaultLongs.class);
			if (ofLongs != null) {
				return toList(ofLongs.value());
			}
		}
		{
			DefaultDouble ofDouble = method.getAnnotation(DefaultDouble.class);
			if (ofDouble != null) {
				return ofDouble.value();
			}
			DefaultDoubles ofDoubles = method.getAnnotation(DefaultDoubles.class);
			if (ofDoubles != null) {
				return toList(ofDoubles.value());
			}
		}
		{
			DefaultString ofString = method.getAnnotation(DefaultString.class);
			if (ofString != null) {
				return ofString.value();
			}
			DefaultStrings ofStrings = method.getAnnotation(DefaultStrings.class);
			if (ofStrings != null) {
				return ImmutableCollections.listOf(ofStrings.value());
			}
		}
		DefaultObjectHelper helper = new DefaultObjectHelper(entry, this);
		DefaultMap ofMap = method.getAnnotation(DefaultMap.class);
		if (ofMap != null) {
			return helper.toMap(ofMap.value());
		}
		DefaultObject ofMethod = method.getAnnotation(DefaultObject.class);
		if (ofMethod != null) {
			return helper.toObject(ofMethod.value());
		}
		throw helper.badDefault("No default value annotation present");
	}
	
	private static List<Boolean> toList(boolean[] booleanArray) {
		List<Boolean> booleans = new ArrayList<>(booleanArray.length);
		for (boolean b : booleanArray) {
			booleans.add(b);
		}
		return booleans;
	}
	
	private static List<Integer> toList(int[] integerArray) {
		List<Integer> integers = new ArrayList<>(integerArray.length);
		for (int i : integerArray) {
			integers.add(i);
		}
		return integers;
	}
	
	private static List<Long> toList(long[] longArray) {
		List<Long> longs = new ArrayList<>(longArray.length);
		for (long l : longArray) {
			longs.add(l);
		}
		return longs;
	}
	
	private static List<Double> toList(double[] doubleArray) {
		List<Double> doubles = new ArrayList<>(doubleArray.length);
		for (double d : doubleArray) {
			doubles.add(d);
		}
		return doubles;
	}

}
