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

final class HardLimitEntry {

	private final int maxCps;
	private final int retentionSecs;

	HardLimitEntry(int maxCps, int retentionSecs) {
		this.maxCps = maxCps;
		this.retentionSecs = retentionSecs;
	}
	
	int maxCps() {
		return maxCps;
	}
	
	int retentionSecs() {
		return retentionSecs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + maxCps;
		result = prime * result + retentionSecs;
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof HardLimitEntry)) {
			return false;
		}
		HardLimitEntry other = (HardLimitEntry) object;
		return maxCps == other.maxCps && retentionSecs == other.retentionSecs;
	}
	
}
