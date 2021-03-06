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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NessConfigTest {

	private ConfigurationFactory<NessConfig> factory;

	@BeforeEach
	public void setup() {
		factory = new SnakeYamlConfigurationFactory<>(NessConfig.class, ConfigurationOptions.defaults(),
				new SnakeYamlOptions.Builder().useCommentingWriter(true).build());
	}

	@Test
	public void loadDefaults() {
		NessConfig nessConfig = factory.loadDefaults();
		assertFalse(nessConfig.isDevMode());
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

		AntiBotConfig antiBot = nessConfig.getAntiBot();
		assertTrue(antiBot.enable());
		assertEquals(15, antiBot.maxPlayersPerSecond());
		assertEquals("Bot Attack Detected! By NESS Reloaded", antiBot.kickMessage());
		assertEquals(10, antiBot.timeUntilTrusted());

		assertDefaultAutoClickConfig(nessConfig.autoClick());
	}

	private static void assertDefaultAutoClickConfig(AutoClickConfig autoClick) {
		assertEquals(32, autoClick.totalRetentionSecs());
		assertEquals(Set.of(new HardLimitEntry(35, 2)), autoClick.hardLimits());
		AutoClickConfig.Constancy autoClickConstancy = autoClick.constancy();
		assertEquals(Set.of(new DeviationEntry(30, 10)), autoClickConstancy.deviationRequirements());
		assertEquals(Set.of(new DeviationEntry(60, 10)), autoClickConstancy.superDeviationRequirements());
	}

	@Test
	public void auxiliarySubSectionReplacement() throws IOException, InvalidConfigException {
		/*
		 * Get the default config as a Map, remove the autoclick section,
		 * then reload the configuration using the default config for auxiliary values
		 */
		NessConfig defaultConfig = factory.loadDefaults();
		ByteArrayOutputStream defaultConfigOutput = new ByteArrayOutputStream();
		factory.write(defaultConfig, defaultConfigOutput);

		Map<?, ?> configMap = (Map<?, ?>) new Yaml().load(new ByteArrayInputStream(defaultConfigOutput.toByteArray()));
		configMap.remove("autoclick");
		String configWithAutoClickRemoved = new Yaml().dump(configMap);

		NessConfig reloadedConfig = factory.load(
				new ByteArrayInputStream(configWithAutoClickRemoved.getBytes(StandardCharsets.UTF_8)),
				defaultConfig);
		assertEquals(defaultConfig.autoClick(), reloadedConfig.autoClick());
		assertDefaultAutoClickConfig(reloadedConfig.autoClick());
	}

}
