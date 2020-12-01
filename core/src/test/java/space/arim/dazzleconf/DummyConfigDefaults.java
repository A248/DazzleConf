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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import space.arim.dazzleconf.DummyConfig.NestedConfig;

public class DummyConfigDefaults {

	public void assertDefaultValues(DummyConfig defaultConf) {
		assertEquals("let's see", defaultConf.myString());
		assertEquals(3, defaultConf.myInteger());
		assertEquals(true, defaultConf.configBool());
		assertEquals(ValueEnum.FIRST_ENTRY, defaultConf.enumFirstEntry());
		assertEquals(ValueEnum.ANOTHER, defaultConf.enumIgnoreCase());
		assertEquals(defaultConf.myInteger(), defaultConf.defaultMethod());
		assertEquals("no need to create an entirely new interface for a small subsection", defaultConf.simpleSubsection());
		assertEquals("did not see that coming", defaultConf.combinedSubsection());
		try {
			assertEquals(new URL("https://google.com"), defaultConf.someUrl());
		} catch (MalformedURLException ex) {
			fail(ex);
		}

		NestedConfig nestedConf = defaultConf.subSection();
		assertEquals("ahaha", nestedConf.nestedValue());
		assertEquals(Set.of("1string", "2string", "3string"), nestedConf.someStringsForYou());
		assertEquals(List.of(1, 2, 3), nestedConf.ordered123());
		assertEquals(new NumericPair(1, 3), nestedConf.numericPair());
	}

	public void assumeDefaultValues(DummyConfig defaultConf) {
		try {
			assertDefaultValues(defaultConf);
		} catch (AssertionFailedError ex) {
			throw new TestAbortedException("Aborting due to failed assumption of default values", ex);
		}
	}

}
