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

public interface AntiBotConfig {

	default boolean enable() {
		return true;
	}

	@Comments("Maximum players able to join in one second")
	default int maxPlayersPerSecond() {
		return 15;
	}

	@Comments("The kick message")
	default String kickMessage() {
		return "Bot Attack Detected! By NESS Reloaded";
	}

	@Comments(
			"The play time, in seconds, after which a player will not be denied joining "
					+ "if he or she rejoins during a bot attack")
	default int timeUntilTrusted() {
		return 10;
	}

}
