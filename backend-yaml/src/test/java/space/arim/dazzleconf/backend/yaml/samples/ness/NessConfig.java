/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
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

package space.arim.dazzleconf.backend.yaml.samples.ness;

import space.arim.dazzleconf2.engine.Comments;
import space.arim.dazzleconf2.engine.liaison.SubSection;

import java.util.List;

public interface NessConfig {

	@Comments("Enable developer mode")
	default boolean devMode() { return false; }

	@Comments({
			"",
			"",
			"Enabled checks",
			"",
			"Comment out a check to disable",
			""})
	default List<String> getEnabledChecks() {
		return List.of(
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
				"ScaffoldAngle");
	}

	@Comments({ "", "AntiBot", "", "Blocks Bot Attacks which sends a lot of players", "" })
	@SubSection AntiBotConfig antibot();

	@SubSection AutoClickConfig autoclick();

}
