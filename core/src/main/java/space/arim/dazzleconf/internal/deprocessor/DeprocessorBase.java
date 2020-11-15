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
import space.arim.dazzleconf.internal.NestedConfEntry;
import space.arim.dazzleconf.internal.SingleConfEntry;
import space.arim.dazzleconf.internal.util.ConfigurationInvoker;

/**
 * Base class for deprocessors. A deprocessor is responsible for serialising config values. While this
 * class handles most of such, subclasses are tasked with handling the config values post deprocessing.
 * 
 * @author A248
 *
 * @param <C> the type of the configuration
 */
abstract class DeprocessorBase<C> {

	private final ConfigurationDefinition<C> definition;
	private final ConfigurationInvoker<C> configDataInvoker;
	
	DeprocessorBase(ConfigurationDefinition<C> definition, C configData) {
		this.definition = definition;
		configDataInvoker = new ConfigurationInvoker<>(configData);
	}
	
	abstract void finishSingle(SingleConfEntry entry, Object value);
	
	abstract <N> void continueNested(NestedConfEntry<N> childEntry, N childConf);
	
	void deprocess() {
		for (ConfEntry entry : definition.getEntries()) {
			Object value = configDataInvoker.getEntryValue(entry);
			if (entry instanceof NestedConfEntry) {
				continueNestedCast((NestedConfEntry<?>) entry, value);
			} else {
				deprocessSingleEntry((SingleConfEntry) entry, value);
			}
		}
	}
	
	private <N> void continueNestedCast(NestedConfEntry<N> childEntry, Object childConf) {
		continueNested(childEntry, childEntry.getDefinition().getConfigClass().cast(childConf));
	}
	
	private void deprocessSingleEntry(SingleConfEntry entry, Object value) {
		DecomposerImpl decomposer = new DecomposerImpl(entry.getKey(), definition.getSerialisers());
		Object postValue = new Decomposition(entry, value, decomposer).deprocessObject();
		finishSingle(entry, postValue);
	}
	
}
