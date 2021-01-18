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

package space.arim.dazzleconf.ext.snakeyaml.ness;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.annote.SubSection;

import java.util.List;

public interface NessConfig {

	@ConfKey("dev-mode")
	@ConfComments("Enable developer mode")
	@ConfDefault.DefaultBoolean(false)
	boolean isDevMode();

	@ConfKey("enabled-checks")
	@ConfComments({
			"",
			"",
			"Enabled checks",
			"",
			"Comment out a check to disable",
			""})
	@ConfDefault.DefaultStrings({
			"AbilititiesSpoofed",
			"Aimbot",
			"AimbotGCD",
			"AutoClicker",
			"AntiKb",
			"BlockBreakAction",
			"Timer",
			"Criticals",
			"ChestStealer",
			"EntityFly",
			"FastLadder",
			"FastPlace",
			"FlyGhostMode",
			"FlyHighJump",
			"FlyInvalidJumpMotion",
			"FlyInvalidServerGravity",
			"FlyInvalidClientGravity",
			"FlyFalseGround",
			"FlyHighDistance",
			"Freecam",
			"GhostHand",
			"LiquidInteraction",
			"InventoryHack",
			"Killaura",
			"KillauraKeepSprint",
			"MorePackets",
			"NoSlowBow",
			"NoSlowFood",
			"NoWeb",
			"NoGround",
			"NoFall",
			"Speed",
			"#SpeedAir",
			"InvalidSprint",
			"SpeedFriction",
			"Jesus",
			"Step",
			"Phase",
			"ImpossibleBreak",
			"ScaffoldFalseTarget",
			"ScaffoldIllegalTarget",
			"ScaffoldDownWard",
			"ScaffoldAngle"})
	List<String> getEnabledChecks();

	@ConfKey("antibot")
	@ConfComments({ "", "AntiBot", "", "Blocks Bot Attacks which sends a lot of players", "" })
	@SubSection
	AntiBotConfig getAntiBot();

	@ConfKey("autoclick")
	@SubSection
	AutoClickConfig autoClick();

}
