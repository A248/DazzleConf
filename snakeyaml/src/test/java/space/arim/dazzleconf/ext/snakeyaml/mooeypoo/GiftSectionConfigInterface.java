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

package space.arim.dazzleconf.ext.snakeyaml.mooeypoo;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfKey;

import java.util.Set;

public interface GiftSectionConfigInterface {

	@ConfKey("every_real_minutes")
	@ConfComments({"An interval in real-world minutes to trigger this command."})
	@ConfDefault.DefaultString("60")
	String every_real_minutes();

	@ConfKey("every_ingame_minutes")
	@ConfComments({"An interval in in-game minutes to trigger this command. If this has value, every_real_minutes is ignored."})
	@ConfDefault.DefaultString("0")
	String every_ingame_minutes();

	@ConfKey("message_to_user")
	@ConfComments({"Message sent to the user when the command is invoked."})
	@ConfDefault.DefaultString("")
	String message_to_user();

	@ConfKey("commands")
	@ConfComments({"Commands to run."})
	@ConfDefault.DefaultStrings({"give %player% minecraft:iron_ingot"})
	Set<String> commands();

	@ConfKey("permission")
	@ConfComments({"Permission the user must have to have the command invoked in the time period."})
	@ConfDefault.DefaultString("")
	String permission();

}
