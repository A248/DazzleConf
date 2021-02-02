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
package space.arim.dazzleconf.internal.deprocessor;

import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.NestedMapHelper;
import space.arim.dazzleconf.internal.util.ConfigurationInvoker;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class DeprocessorBase<C> {

	private final ConfigurationDefinition<C> definition;
	private final ConfigurationInvoker<C> configDataInvoker;

	final NestedMapHelper mapHelper = new NestedMapHelper(new LinkedHashMap<>());
	
	DeprocessorBase(ConfigurationDefinition<C> definition, C configData) {
		this.definition = definition;
		this.configDataInvoker = new ConfigurationInvoker<>(configData);
	}

	abstract Object wrapValue(ConfEntry entry, Object value);

	abstract <N> DeprocessorBase<N> createChildDeprocessor(ConfigurationDefinition<N> childDefinition, N childConfig);

	public Map<String, Object> deprocess() {
		for (ConfEntry entry : definition.getEntries()) {
			String key = entry.getKey();
			Object deprocessedValue = getDeprocessedValue(entry, configDataInvoker.getEntryValue(entry));
			mapHelper.put(key, wrapValue(entry, deprocessedValue));
		}
		return mapHelper.getTopLevelMap();
	}

	private Object getDeprocessedValue(ConfEntry entry, Object value) {
		DecomposerImpl decomposer = new DecomposerImpl(entry.getKey(), definition.getSerialisers());
		Decomposition decomposition = new Decomposition(this, entry, value, decomposer);
		return decomposition.deprocessObject();
	}

	<N> Map<String, Object> deprocessNested(ConfigurationDefinition<N> childDefinition, N childConfig) {
		return createChildDeprocessor(childDefinition, childConfig).deprocess();
	}
	
}
