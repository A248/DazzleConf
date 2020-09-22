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
import java.util.List;
import java.util.Set;

import space.arim.dazzleconf.ConfigurationOptions;

public class ConfigurationInfo<C> extends ConfigurationDefinition<C> {

	private final ConfigurationOptions options;
	
	ConfigurationInfo(Class<C> configClass, ConfigurationOptions options, List<ConfEntry> entries,
			Set<Method> defaultMethods) {
		super(configClass, entries, defaultMethods);
		this.options = options;
	}
	
	public ConfigurationOptions getOptions() {
		return options;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + options.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof ConfigurationInfo)) {
			return false;
		}
		ConfigurationInfo<?> other = (ConfigurationInfo<?>) object;
		return getConfigClass() == other.getConfigClass() && options.equals(other.options);
	}

	
}
