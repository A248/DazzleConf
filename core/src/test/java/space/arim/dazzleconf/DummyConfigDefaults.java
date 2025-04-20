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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

public class DummyConfigDefaults {

	private static final Map<String, ComplexObject> DEFAULT_VALUE_COMPLEX = Map.of(
			"object1", new ComplexObject(3, "name1", true),
			"otherkey", new ComplexObject(-5, "name2", false)
		);
	public static Map<String, ComplexObject> defaultValueComplex() {
		return DEFAULT_VALUE_COMPLEX;
	}

	public void assertDefaultValues(DummyConfig defaultConf) {
		assertEquals("let's see", defaultConf.myString());
		assertEquals(3, defaultConf.myInteger());
		assertTrue(defaultConf.configBool());
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
		assertEquals(Map.of(ValueEnum.ANOTHER, "value", ValueEnum.THIRD, "more"), defaultConf.enumMap());
		assertEquals(Set.of("string1", "string2"), defaultConf.someStrings());

		assertDefaultNestedConfigValues(defaultConf.subSection());
	}

	void assertDefaultNestedConfigValues(NestedConfig nestedConf) {
		assertEquals("ahaha", nestedConf.nestedValue());
		assertEquals(Set.of("1string", "2string", "3string"), nestedConf.someStringsForYou());
		assertEquals(List.of(1, 2, 3), nestedConf.ordered123());
		assertEquals(new NumericPair(1, 3), nestedConf.numericPair());
		assertEquals(Map.of("key1", new NumericPair(4, 18), "key2", new NumericPair(2, 8)), nestedConf.extraPairs());
		assertEquals(defaultValueComplex(), nestedConf.complexValues());
		assertEquals(NestedConfig.numericPairDefaultInSameClassDefault(), nestedConf.numericPairDefaultInSameClass());
	}

	public void assumeDefaultValues(DummyConfig defaultConf) {
		try {
			assertDefaultValues(defaultConf);
		} catch (AssertionFailedError ex) {
			throw new TestAbortedException("Aborting due to failed assumption of default values", ex);
		}
	}

	public static ConfigurationOptions createOptions() {
		return new ConfigurationOptions.Builder()
				.addSerialiser(new ComplexObjectSerialiser())
				.setDottedPathInConfKey(true)
				.build();
	}

}
