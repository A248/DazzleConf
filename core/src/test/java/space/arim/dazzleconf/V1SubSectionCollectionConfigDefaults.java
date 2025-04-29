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

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class V1SubSectionCollectionConfigDefaults {

	private static final Map<String, V1NestedConfig> DEFAULT_NESTED_CONFIG_MAP = Map.of(
			"key-one", new V1NestedConfig() {

				@Override
				public String nestedValue() { return ""; }

				@Override
				public Set<String> someStringsForYou() { return Set.of(); }

				@Override
				public List<Integer> ordered123() { return List.of(); }

				@Override
				public V1NumericPair numericPair() { return new V1NumericPair(0, 0); }

				@Override
				public Map<String, V1NumericPair> extraPairs() { return Map.of(); }

				@Override
				public Map<String, V1ComplexObject> complexValues() { return Map.of(); }

				@Override
				public V1NumericPair numericPairDefaultInSameClass() { return new V1NumericPair(0, 0); }
			}
	);
	public static Map<String, V1NestedConfig> defaultNestedConfigMap() {
		return DEFAULT_NESTED_CONFIG_MAP;
	}

	public static Map<String, V1NestedConfig> defaultNestedConfigMapUsingDefaultSection(V1NestedConfig defaultNestedConfig) {
		return Map.of("key-two", defaultNestedConfig);
	}

	public void assertDefaultValues(V1SubSectionCollectionConfig config) {
		assertEquals(defaultNestedConfigMap(), config.nestedConfigMap());

		V1NestedConfig configAtKeyTwo = config.nestedConfigMapUsingDefaultSection().get("key-two");
		new V1DummyConfigDefaults().assertDefaultNestedConfigValues(configAtKeyTwo);
	}

}
