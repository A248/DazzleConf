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
import space.arim.dazzleconf.annote.ConfSerialisers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ConfSerialisers(NumericPairSerialiser.class)
public interface NestedConfig {

	@ConfDefault.DefaultString("ahaha")
	String nestedValue();

	@ConfDefault.DefaultStrings({"1string", "2string", "3string"})
	Set<String> someStringsForYou();

	@ConfDefault.DefaultIntegers({1, 2, 3})
	List<Integer> ordered123();

	@ConfDefault.DefaultString("1:3")
	NumericPair numericPair();

	@ConfDefault.DefaultMap({"key1", "4:18", "key2", "2:8"})
	Map<String, NumericPair> extraPairs();

	@ConfDefault.DefaultObject("space.arim.dazzleconf.DummyConfigDefaults.defaultValueString")
	String stringUsingDefaultObjectAnnotation();

	@ConfDefault.DefaultObject("space.arim.dazzleconf.DummyConfigDefaults.defaultValueComplex")
	Map<String, ComplexObject> complexValues();

}
