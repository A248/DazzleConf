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

/**
 * Nested configuration entry
 * 
 * @author A248
 *
 * @param <N> the type of the nested configuration
 */
public class NestedConfEntry<N> extends ConfEntry {

	private final ConfigurationDefinition<N> configDefinition;
	
	NestedConfEntry(Method method, String key, List<String> comments, ConfigurationDefinition<N> configDefinition) {
		super(method, key, comments);
		this.configDefinition = configDefinition;
	}
	
	public ConfigurationDefinition<N> getDefinition() {
		return configDefinition;
	}

}
