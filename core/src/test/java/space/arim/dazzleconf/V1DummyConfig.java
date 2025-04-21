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

import space.arim.dazzleconf.annote.ConfDefault.DefaultBoolean;
import space.arim.dazzleconf.annote.ConfDefault.DefaultInteger;
import space.arim.dazzleconf.annote.ConfDefault.DefaultMap;
import space.arim.dazzleconf.annote.ConfDefault.DefaultString;
import space.arim.dazzleconf.annote.ConfDefault.DefaultStrings;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.annote.ConfSerialisers;
import space.arim.dazzleconf.annote.SubSection;
import space.arim.dazzleconf.serialiser.URLValueSerialiser;

import java.net.URL;
import java.util.Map;
import java.util.Set;

@ConfSerialisers(URLValueSerialiser.class)
public interface V1DummyConfig {

	@DefaultString("let's see")
	String myString();
	
	@DefaultInteger(3)
	int myInteger();
	
	@DefaultBoolean(true)
	boolean configBool();
	
	@DefaultString("FIRST_ENTRY")
	V1ValueEnum enumFirstEntry();
	
	@DefaultString("Another")
	V1ValueEnum enumIgnoreCase();
	
	@SubSection
    V1NestedConfig subSection();
	
	default int defaultMethod() {
		return (configBool()) ? myInteger() : 0;
	}
	
	@ConfKey("otherSubSection.key")
	@DefaultString("no need to create an entirely new interface for a small subsection")
	String simpleSubsection();
	
	@ConfKey("subSection.additional-key")
	@DefaultString("did not see that coming")
	String combinedSubsection();
	
	@DefaultString("https://google.com")
	URL someUrl();

	@DefaultMap({"ANOTHER", "value", "THIRD", "more"})
	Map<V1ValueEnum, String> enumMap();

	@DefaultStrings({"string1", "string2"})
	Set<String> someStrings();
	
}
