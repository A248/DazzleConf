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

final class DeviationEntry {

	private final int deviationPercentage;
	private final int sampleCount;
	
	DeviationEntry(int deviationPercentage, int sampleCount) {
		this.deviationPercentage = deviationPercentage;
		this.sampleCount = sampleCount;
	}
	
	int deviationPercentage() {
		return deviationPercentage;
	}
	
	int sampleCount() {
		return sampleCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + deviationPercentage;
		result = prime * result + sampleCount;
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof DeviationEntry)) {
			return false;
		}
		DeviationEntry other = (DeviationEntry) object;
		return deviationPercentage == other.deviationPercentage && sampleCount == other.sampleCount;
	}

	@Override
	public String toString() {
		return "DeviationEntry [deviationPercentage=" + deviationPercentage + ", sampleCount=" + sampleCount + "]";
	}
	
}
