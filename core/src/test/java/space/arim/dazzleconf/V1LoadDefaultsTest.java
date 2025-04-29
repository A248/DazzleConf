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
package space.arim.dazzleconf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import space.arim.dazzleconf.factory.DefaultsOnlyFactory;

public class V1LoadDefaultsTest {

	private ConfigurationFactory<V1DummyConfig> factory;
	
	@BeforeEach
	public void setup() {
		factory = new DefaultsOnlyFactory<>(V1DummyConfig.class, V1DummyConfigDefaults.createOptions());
	}
	
	@Test
	public void testLoadDefaults() {
		V1DummyConfig defaultConf = factory.loadDefaults();
		new V1DummyConfigDefaults().assertDefaultValues(defaultConf);
	}
	
}
