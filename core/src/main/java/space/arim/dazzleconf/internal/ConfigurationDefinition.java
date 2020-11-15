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
package space.arim.dazzleconf.internal;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import space.arim.dazzleconf.annote.ConfHeader;
import space.arim.dazzleconf.internal.util.ImmutableCollections;
import space.arim.dazzleconf.serialiser.ValueSerialiserMap;

public class ConfigurationDefinition<C> {

	private final Class<C> configClass;
	private final Map<String, ConfEntry> entries;
	private final Set<Method> defaultMethods;
	private final ValueSerialiserMap serialisers;
	
	ConfigurationDefinition(Class<C> configClass, Map<String, ConfEntry> entries, Set<Method> defaultMethods,
			ValueSerialiserMap serialisers) {
		this.configClass = configClass;
		this.entries = entries;
		this.defaultMethods = ImmutableCollections.setOf(defaultMethods);
		this.serialisers = serialisers;
	}
	
	public Class<C> getConfigClass() {
		return configClass;
	}
	
	public List<String> getHeader() {
		ConfHeader header = getConfigClass().getAnnotation(ConfHeader.class);
		return (header == null) ? ImmutableCollections.emptyList() : ImmutableCollections.listOf(header.value());
	}
	
	public Collection<ConfEntry> getEntries() {
		return entries.values();
	}
	
	public boolean hasDefaultMethods() {
		return !defaultMethods.isEmpty();
	}
	
	public Set<Method> getDefaultMethods() {
		if (!hasDefaultMethods()) {
			throw new IllegalStateException("No default methods present");
		}
		return defaultMethods;
	}
	
	public ValueSerialiserMap getSerialisers() {
		return serialisers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + configClass.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		// Changes here should be updated likewise in ConfigurationInfo
		return configClass == ((ConfigurationDefinition<?>) object).configClass;
	}
	
}
