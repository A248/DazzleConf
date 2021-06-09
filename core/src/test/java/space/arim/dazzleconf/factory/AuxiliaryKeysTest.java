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

package space.arim.dazzleconf.factory;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.AuxiliaryKeys;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.annote.SubSection;
import space.arim.dazzleconf.error.InvalidConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuxiliaryKeysTest {

	private ConfigurationFactory<Config> factoryFromSource(Map<String, Object> source) {
		return new FixedLoaderFactory<>(Config.class, ConfigurationOptions.defaults(), source);
	}

	@Test
	public void noAuxiliaryKeysUsage() throws IOException, InvalidConfigException {
		String valueOne = "val1";
		String valueTwo = "val2";
		String nestedValue = "val3";
		Map<String, Object> source = Map.of(
				"valueOne", valueOne, "valueTwo", valueTwo, "subConfig", Map.of("nestedValue", nestedValue));
		Config auxiliary = Config.withValues("val1Aux", "val2Aux", "val3Aux");
		Config loaded = factoryFromSource(source).load(InputStream.nullInputStream(), auxiliary);
		assertEquals(valueOne, loaded.valueOne());
		assertEquals(valueTwo, loaded.valueTwo());
		assertEquals(nestedValue, loaded.subConfig().nestedValue());
		assertFalse(loaded instanceof AuxiliaryKeys, "loaded config must not have auxiliary keys marker");
	}

	// Top-level option's auxiliary value is used
	@Test
	public void auxiliaryKeysUsage() throws IOException, InvalidConfigException {
		String valueOne = "val1";
		String valueTwoAux = "val2Aux";
		String nestedValue = "val3";
		Map<String, Object> source = Map.of("valueOne", valueOne, "subConfig", Map.of("nestedValue", nestedValue));
		Config auxiliary = Config.withValues("val1Aux", valueTwoAux, "val3Aux");
		Config loaded = factoryFromSource(source).load(InputStream.nullInputStream(), auxiliary);
		assertEquals(valueOne, loaded.valueOne());
		assertEquals(valueTwoAux, loaded.valueTwo());
		assertEquals(nestedValue, loaded.subConfig().nestedValue());
		assertTrue(loaded instanceof AuxiliaryKeys, "loaded config must have auxiliary keys marker");
	}

	// Nested option's auxiliary value is used
	@Test
	public void auxiliaryKeysNestedUsage() throws IOException, InvalidConfigException {
		String valueOne = "val1";
		String valueTwoAux = "val2Aux";
		String nestedValueAux = "val3Aux";
		Map<String, Object> source = Map.of("valueOne", valueOne, "subConfig", Map.of());
		Config auxiliary = Config.withValues("val1Aux", valueTwoAux, nestedValueAux);
		Config loaded = factoryFromSource(source).load(InputStream.nullInputStream(), auxiliary);
		assertEquals(valueOne, loaded.valueOne());
		assertEquals(valueTwoAux, loaded.valueTwo());
		assertEquals(nestedValueAux, loaded.subConfig().nestedValue());
		assertTrue(loaded instanceof AuxiliaryKeys, "loaded config must have auxiliary keys marker interface");
	}

	// Top-level config section's auxiliary value is used
	@Test
	public void auxiliaryKeysNestedSectionUsage() throws IOException, InvalidConfigException {
		String valueOne = "val1";
		String valueTwoAux = "val2Aux";
		String nestedValueAux = "val3Aux";
		Map<String, Object> source = Map.of("valueOne", valueOne);
		Config auxiliary = Config.withValues("val1Aux", valueTwoAux, nestedValueAux);
		Config loaded = factoryFromSource(source).load(InputStream.nullInputStream(), auxiliary);
		assertEquals(valueOne, loaded.valueOne());
		assertEquals(valueTwoAux, loaded.valueTwo());
		assertEquals(nestedValueAux, loaded.subConfig().nestedValue());
		assertTrue(loaded instanceof AuxiliaryKeys, "loaded config must have auxiliary keys marker interface");
	}

	public interface Config {

		static Config withValues(String valueOne, String valueTwo, String nestedValue) {
			return new Config() {
				@Override
				public String valueOne() { return valueOne; }

				@Override
				public String valueTwo() { return valueTwo; }

				@Override
				public SubConfig subConfig() { return () -> nestedValue; }
			};
		}

		String valueOne();

		String valueTwo();

		@SubSection
		SubConfig subConfig();

		interface SubConfig {

			String nestedValue();
		}
	}
}
