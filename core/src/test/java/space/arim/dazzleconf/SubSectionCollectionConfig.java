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

package space.arim.dazzleconf;

import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.SubSection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SubSectionCollectionConfig {

	@ConfDefault.DefaultObject("space.arim.dazzleconf.SubSectionCollectionConfigDefaults.defaultNestedConfigMap")
	Map<String, @SubSection NestedConfig> nestedConfigMap();

	@ConfDefault.DefaultObject("space.arim.dazzleconf.SubSectionCollectionConfigDefaults.defaultNestedConfigMapUsingDefaultSection")
	Map<String, @SubSection NestedConfig> nestedConfigMapUsingDefaultSection();

	@ConfDefault.DefaultObject("nestedConfigSetDefaults")
	Set<@SubSection NestedConfig> nestedConfigSet();

	static Set<NestedConfig> nestedConfigSetDefaults(NestedConfig defaultNestedConfig) {
		return Set.of(defaultNestedConfig);
	}

	@ConfDefault.DefaultObject("nestedConfigListDefaults")
	List<@SubSection NestedConfig> nestedConfigList();

	static List<NestedConfig> nestedConfigListDefaults(NestedConfig defaultNestedConfig) {
		return List.of(defaultNestedConfig);
	}

	@ConfDefault.DefaultObject("nestedConfigCollectionDefaults")
	Collection<@SubSection NestedConfig> nestedConfigCollection();

	static Collection<NestedConfig> nestedConfigCollectionDefaults(NestedConfig defaultNestedConfig) {
		return Set.of(defaultNestedConfig);
	}

}
