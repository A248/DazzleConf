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

package space.arim.dazzleconf.backend.yaml.samples.arturekdev;


import space.arim.dazzleconf2.engine.liaison.SubSection;

import java.util.Map;

public interface LevelConfig {

	default Map<Integer, @SubSection Level> levels() {
		return Map.of(1, Level.of(5, 10, 1, 10, 0),
				2, Level.of(7, 12, 2, 11, 10),
				3, Level.of(9, 14, 3, 12, 20),
				4, Level.of(12, 16, 4, 13, 30),
				5, Level.of(15, 18, 5, 14, 40));
	}

}
