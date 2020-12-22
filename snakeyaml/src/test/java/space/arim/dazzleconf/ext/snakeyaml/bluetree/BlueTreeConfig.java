/*
 * DazzleConf
 * Copyright © 2020 Anand Beh
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

package space.arim.dazzleconf.ext.snakeyaml.bluetree;

import java.util.List;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfDefault.DefaultString;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.sorter.AnnotationBasedSorter.Order;

public interface BlueTreeConfig {

    @ConfKey("isEnabled")
    @ConfDefault.DefaultBoolean(false)
    @ConfComments("Should we use a database?")
    @Order(1)
    boolean isEnabled();

    @ConfKey("Host")
    @DefaultString("localhost")
    @ConfComments("Host for your database, usually localhost.")
    @Order(2)
    String Host();

    @ConfKey("Port")
    @ConfDefault.DefaultInteger(3306)
    @ConfComments("Port for your Database, usually 3306")
    @Order(3)
    int Port();

    @ConfKey("DatabaseName")
    @DefaultString("DiscordSRVUtilsData")
    @ConfComments("Database name. The host should tell you the name normally.")
    @Order(4)
    String DatabaseName();

    static final List<String> EXPECTED_LINES = List.of(
    			"",
    			" # Should we use a database?",
				"isEnabled: false",
				"",
				" # Host for your database, usually localhost.",
				"Host: 'localhost'",
				"",
				" # Port for your Database, usually 3306",
				"Port: 3306",
				"",
				" # Database name. The host should tell you the name normally.",
				"DatabaseName: 'DiscordSRVUtilsData'");

}