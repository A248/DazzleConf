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

public class SubSectionCollectionConfigDefaults {

	private static final Map<String, DummyConfig.NestedConfig> DEFAULT_NESTED_CONFIG_MAP = Map.of(
			"keyone", new DummyConfig.NestedConfig() {

				@Override
				public String nestedValue() { return ""; }

				@Override
				public Set<String> someStringsForYou() { return Set.of(); }

				@Override
				public List<Integer> ordered123() { return List.of(); }

				@Override
				public NumericPair numericPair() { return new NumericPair(0, 0); }

				@Override
				public Map<String, NumericPair> extraPairs() { return Map.of(); }

				@Override
				public String stringUsingDefaultObjectAnnotation() { return ""; }

				@Override
				public Map<String, ComplexObject> complexValues() { return Map.of(); }
			}
	);
	public static Map<String, DummyConfig.NestedConfig> defaultNestedConfigMap() {
		return DEFAULT_NESTED_CONFIG_MAP;
	}

	public void assertDefaultValues(SubSectionCollectionConfig config) {
		assertEquals(defaultNestedConfigMap(), config.nestedConfigMap());
	}

}
