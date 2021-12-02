/*
 * DazzleConf
 * Copyright © 2021 Anand Beh
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

package space.arim.dazzleconf.internal.processor;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.error.ImproperEntryException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.NestedMapHelper;
import space.arim.dazzleconf.internal.error.ElementaryType;
import space.arim.dazzleconf.internal.error.UserError;

import java.util.Map;

public class MapProcessor<C> extends ProcessorBase<C> {

	private final NestedMapHelper mapHelper;

	public MapProcessor(ConfigurationOptions options, ConfigurationDefinition<C> definition,
			Map<String, Object> sourceMap, C auxiliaryValues) {
		this(options, definition, new NestedMapHelper(sourceMap), auxiliaryValues);
	}

	private MapProcessor(ConfigurationOptions options, ConfigurationDefinition<C> definition,
			NestedMapHelper mapHelper, C auxiliaryValues) {
		super(options, definition, auxiliaryValues);
		this.mapHelper = mapHelper;
	}
	
	@Override
	<N> N createChildConfig(ConfigurationOptions options, ConfigurationDefinition<N> childDefinition,
							String key, Object preValue, N nestedAuxiliaryValues) throws InvalidConfigException {
		if (!(preValue instanceof Map)) {
			throw new BadValueException.Builder()
					.key(key)
					.message(UserError.wrongType(ElementaryType.SECTION, preValue))
					.build();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> childMap = (Map<String, Object>) preValue;
		return createFromProcessor(
				new MapProcessor<>(options, childDefinition, childMap, nestedAuxiliaryValues));
	}

	@Override
	Object getValueFromSources(ConfEntry entry) throws ImproperEntryException {
		return mapHelper.get(entry.getKey());
	}

}
