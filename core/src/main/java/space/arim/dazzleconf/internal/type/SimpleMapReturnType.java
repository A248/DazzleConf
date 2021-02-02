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

package space.arim.dazzleconf.internal.type;

import java.util.Map;
import java.util.Objects;

public final class SimpleMapReturnType<K, V> implements MapReturnType<K, V> {

	private final TypeInfo<Map<K, V>> typeInfo;

	public SimpleMapReturnType(TypeInfo<Map<K, V>> typeInfo) {
		this.typeInfo = Objects.requireNonNull(typeInfo);
	}

	@Override
	public TypeInfo<Map<K, V>> typeInfo() {
		return typeInfo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SimpleMapReturnType<?, ?> that = (SimpleMapReturnType<?, ?>) o;
		return typeInfo.equals(that.typeInfo);
	}

	@Override
	public int hashCode() {
		return typeInfo.hashCode();
	}

	@Override
	public String toString() {
		return "SimpleMapReturnType{" +
				"typeInfo=" + typeInfo +
				'}';
	}
}
