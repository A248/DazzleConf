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

package space.arim.dazzleconf;

class ComplexObject {

	private final int count;
	private final String name;
	private final boolean enabled;

	public ComplexObject(int count, String name, boolean enabled) {
		this.count = count;
		this.name = name;
		this.enabled = enabled;
	}

	public int count() {
		return count;
	}

	public String name() {
		return name;
	}

	public boolean enabled() {
		return enabled;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ComplexObject that = (ComplexObject) o;
		return count == that.count && enabled == that.enabled && name.equals(that.name);
	}

	@Override
	public int hashCode() {
		int result = count;
		result = 31 * result + name.hashCode();
		result = 31 * result + (enabled ? 1 : 0);
		return result;
	}
}
