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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfDefault.*;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.ImproperEntryException;
import space.arim.dazzleconf.error.MissingKeyException;
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.ImmutableCollections;
import space.arim.dazzleconf.internal.NestedConfEntry;

public class DefaultsProcessor extends ProcessorBase {

	public DefaultsProcessor(ConfigurationOptions options, ConfigurationDefinition<?> definition) {
		super(options, definition, null);
	}
	
	@Override
	ProcessorBase continueNested(ConfigurationOptions options, NestedConfEntry<?> childEntry,
			Object nestedAuxiliaryValues) throws ImproperEntryException {
		if (nestedAuxiliaryValues != null) {
			throw new AssertionError("DefaultsProcessor does not handle auxiliary entries");
		}
		return new DefaultsProcessor(options, childEntry.getDefinition());
	}

	@Override
	Object getValueFromSources(ConfEntry entry) throws MissingKeyException {
		Method method = entry.getMethod();

		DefaultBoolean ofBoolean = method.getAnnotation(ConfDefault.DefaultBoolean.class);
		if (ofBoolean != null) {
			return ofBoolean.value();
		}
		DefaultBooleans ofBooleans = method.getAnnotation(ConfDefault.DefaultBooleans.class);
		if (ofBooleans != null) {
			List<Boolean> booleans = new ArrayList<>();
			for (boolean b : ofBooleans.value()) { booleans.add(b); }
			return booleans;
		}
		DefaultInteger ofInteger = method.getAnnotation(ConfDefault.DefaultInteger.class);
		if (ofInteger != null) {
			return ofInteger.value();
		}
		DefaultIntegers ofIntegers = method.getAnnotation(ConfDefault.DefaultIntegers.class);
		if (ofIntegers != null) {
			List<Integer> integers = new ArrayList<>();
			for (int i : ofIntegers.value()) { integers.add(i); }
			return integers;
		}
		DefaultLong ofLong = method.getAnnotation(ConfDefault.DefaultLong.class);
		if (ofLong != null) {
			return ofLong.value();
		}
		DefaultLongs ofLongs = method.getAnnotation(ConfDefault.DefaultLongs.class);
		if (ofLongs != null) {
			List<Long> longs = new ArrayList<>();
			for (long l : ofLongs.value()) { longs.add(l); }
			return longs;
		}
		DefaultDouble ofDouble = method.getAnnotation(ConfDefault.DefaultDouble.class);
		if (ofDouble != null) {
			return ofDouble.value();
		}
		DefaultDoubles ofDoubles = method.getAnnotation(ConfDefault.DefaultDoubles.class);
		if (ofDoubles != null) {
			List<Double> doubles = new ArrayList<>();
			for (double d : ofDoubles.value()) { doubles.add(d); }
			return doubles;
		}
		DefaultString ofString = method.getAnnotation(ConfDefault.DefaultString.class);
		if (ofString != null) {
			return ofString.value();
		}
		DefaultStrings ofStrings = method.getAnnotation(ConfDefault.DefaultStrings.class);
		if (ofStrings != null) {
			return ImmutableCollections.listOf(ofStrings.value());
		}
		DefaultMap ofMap = method.getAnnotation(ConfDefault.DefaultMap.class);
		if (ofMap != null) {
			Map<String, String> result = new HashMap<>();
			String key = null;
			for (String value : ofMap.value()) {
				if (key == null) {
					key = value;
				} else {
					result.put(key, value);
					key = null;
				}
			}
			if (key != null) {
				throw new IllDefinedConfigException("@DefaultMap on " + entry.getQualifiedMethodName() + " is incomplete");
			}
			return result;
		}
		throw new IllDefinedConfigException("No default value annotation present on " + entry.getQualifiedMethodName());
	}

}
