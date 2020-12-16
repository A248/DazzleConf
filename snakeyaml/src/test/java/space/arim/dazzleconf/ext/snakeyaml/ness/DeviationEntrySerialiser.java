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

public class DeviationEntrySerialiser extends IntPairSerialiser<DeviationEntry> {

	@Override
	DeviationEntry fromInts(int value1, int value2) {
		return new DeviationEntry(value1, value2);
	}

	@Override
	int[] toInts(DeviationEntry value) {
		return new int[] {value.deviationPercentage(), value.sampleCount()};
	}

	@Override
	public Class<DeviationEntry> getTargetClass() {
		return DeviationEntry.class;
	}
	
}
