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

import java.util.Map;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.ImproperEntryException;
import space.arim.dazzleconf.error.MissingKeyException;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.NestedConfEntry;
import space.arim.dazzleconf.internal.NestedMapHelper;
import space.arim.dazzleconf.internal.SingleConfEntry;

public class MapProcessor<C> extends ProcessorBase<C> {

	private final NestedMapHelper mapHelper;
	
	public MapProcessor(ConfigurationOptions options, ConfigurationDefinition<C> definition,
			Map<String, Object> sourceMap, Object auxiliaryValues) {
		this(options, definition, new NestedMapHelper(sourceMap), auxiliaryValues);
	}
	
	@Override
	<N> ProcessorBase<N> continueNested(ConfigurationOptions options, NestedConfEntry<N> childEntry,
			Object nestedAuxiliaryValues) throws ImproperEntryException {
		Map<String, Object> childMap = getChildMapFromSources(childEntry, mapHelper);
		return new MapProcessor<>(options, childEntry.getDefinition(), childMap, nestedAuxiliaryValues);
	}
	
	private MapProcessor(ConfigurationOptions options, ConfigurationDefinition<C> definition,
			NestedMapHelper mapHelper, Object auxiliaryValues) {
		super(options, definition, auxiliaryValues);
		this.mapHelper = mapHelper;
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, Object> getChildMapFromSources(NestedConfEntry<?> entry, NestedMapHelper mapHelper)
			throws ImproperEntryException {
		String key = entry.getKey();
		Object childObject = mapHelper.get(key);
		if (!(childObject instanceof Map)) {
			throw new BadValueException.Builder().key(key)
					.message("Object " + childObject + " is not a configuration section").build();
		}
		return (Map<String, Object>) childObject;
	}
	
	@Override
	Object getValueFromSources(SingleConfEntry entry) throws MissingKeyException {
		return mapHelper.get(entry.getKey());
	}
	
}
