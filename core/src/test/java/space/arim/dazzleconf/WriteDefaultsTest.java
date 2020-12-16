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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.factory.SerialisationFactory;

public class WriteDefaultsTest {
	
	private ConfigurationFactory<DummyConfig> factory;
	
	@BeforeEach
	public void setup() {
		factory = new SerialisationFactory<>(DummyConfig.class, ConfigurationOptions.defaults());
	}

	@Test
	public void testWriteDefaults() {
		DummyConfig defaultConf = factory.loadDefaults();
		var baos = new ByteArrayOutputStream();
		try {
			factory.write(defaultConf, baos);
		} catch (IOException ex) {
			fail(ex);
		}
		var bais = new ByteArrayInputStream(baos.toByteArray());
		DummyConfig reloaded;
		try {
			reloaded = factory.load(bais);
		} catch (IOException | InvalidConfigException ex) {
			throw Assertions.<RuntimeException>fail(ex);
		}
		new DummyConfigDefaults().assertDefaultValues(reloaded);
	}
	
}
