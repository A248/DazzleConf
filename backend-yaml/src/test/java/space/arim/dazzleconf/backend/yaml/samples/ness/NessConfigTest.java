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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.backend.yaml.YamlBackend;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NessConfigTest {

	private Configuration<NessConfig> configuration;

	@BeforeEach
	public void setup() {
		configuration = Configuration.defaultBuilder(NessConfig.class)
				.addTypeLiaisons(new HardLimitSerializer(), new DeviationEntrySerializer())
				.build();
	}

	@Test
	public void loadDefaults() {
		NessConfig nessConfig = configuration.loadDefaults();
		assertFalse(nessConfig.devMode());
		assertEquals(List.of(
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
				"ScaffoldAngle"),
				nessConfig.getEnabledChecks());

		AntiBotConfig antiBot = nessConfig.antibot();
		assertTrue(antiBot.enable());
		assertEquals(15, antiBot.maxPlayersPerSecond());
		assertEquals("Bot Attack Detected! By NESS Reloaded", antiBot.kickMessage());
		assertEquals(10, antiBot.timeUntilTrusted());

		assertDefaultAutoClickConfig(nessConfig.autoclick());
	}

	private static void assertDefaultAutoClickConfig(AutoClickConfig autoClick) {
		assertEquals(32, autoClick.totalRetentionSecs());
		assertEquals(Set.of(new HardLimitEntry(35, 2)), autoClick.hardLimit().cpsAndRequiredSpan());
		AutoClickConfig.Constancy autoClickConstancy = autoClick.constancy();
		assertEquals(Set.of(new DeviationEntry(30, 10)), autoClickConstancy.deviationAndSample());
		assertEquals(Set.of(new DeviationEntry(60, 10)), autoClickConstancy.superdeviationAndSupersample());
	}

	@Test
	public void auxiliarySubSectionReplacement() {
		/*
		 * Get the default config as a Map, remove the autoclick section,
		 * then reload the configuration using the default config for auxiliary values
		 */
		DataTree.Mut defaultConfig = new DataTree.Mut();
		configuration.writeTo(configuration.loadDefaults(), defaultConfig);
		defaultConfig.remove("autoclick");

		YamlBackend yamlBackend = new YamlBackend(new StringRoot(""));
		yamlBackend.write(Backend.Document.simple(defaultConfig));

		NessConfig reloadedConfig = configuration.configureWith(yamlBackend).getOrThrow();
		assertDefaultAutoClickConfig(reloadedConfig.autoclick());
	}

}
