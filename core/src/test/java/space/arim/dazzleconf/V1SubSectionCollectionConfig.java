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

public interface V1SubSectionCollectionConfig {

	@ConfDefault.DefaultObject("space.arim.dazzleconf.V1SubSectionCollectionConfigDefaults.defaultNestedConfigMap")
	Map<String, @SubSection V1NestedConfig> nestedConfigMap();

	@ConfDefault.DefaultObject("space.arim.dazzleconf.V1SubSectionCollectionConfigDefaults.defaultNestedConfigMapUsingDefaultSection")
	Map<String, @SubSection V1NestedConfig> nestedConfigMapUsingDefaultSection();

	@ConfDefault.DefaultObject("nestedConfigSetDefaults")
	Set<@SubSection V1NestedConfig> nestedConfigSet();

	static Set<V1NestedConfig> nestedConfigSetDefaults(V1NestedConfig defaultNestedConfig) {
		return Set.of(defaultNestedConfig);
	}

	@ConfDefault.DefaultObject("nestedConfigListDefaults")
	List<@SubSection V1NestedConfig> nestedConfigList();

	static List<V1NestedConfig> nestedConfigListDefaults(V1NestedConfig defaultNestedConfig) {
		return List.of(defaultNestedConfig);
	}

	@ConfDefault.DefaultObject("nestedConfigCollectionDefaults")
	Collection<@SubSection V1NestedConfig> nestedConfigCollection();

	static Collection<V1NestedConfig> nestedConfigCollectionDefaults(V1NestedConfig defaultNestedConfig) {
		return Set.of(defaultNestedConfig);
	}

}
