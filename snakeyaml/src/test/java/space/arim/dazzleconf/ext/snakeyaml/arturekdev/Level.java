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

package space.arim.dazzleconf.ext.snakeyaml.arturekdev;

public interface Level {

	int minSpawnDelay();

	int maxSpawnDelay();

	int spawnCount();

	int spawnRange();

	int price();

	static Level of(int minSpawnDelay, int maxSpawnDelay, int spawnCount, int spawnRange, int price) {
		record LevelImpl (int minSpawnDelay, int maxSpawnDelay, int spawnCount, int spawnRange, int price)
				implements Level {

		}
		return new LevelImpl(minSpawnDelay, maxSpawnDelay, spawnCount, spawnRange, price);
	}

}
