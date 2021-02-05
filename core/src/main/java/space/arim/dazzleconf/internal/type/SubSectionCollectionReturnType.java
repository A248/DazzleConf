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

import space.arim.dazzleconf.internal.ConfigurationDefinition;

import java.util.Collection;
import java.util.Objects;

public final class SubSectionCollectionReturnType<E, R extends Collection<E>>
		implements CollectionReturnType<E, R>, ReturnTypeWithConfigDefinition<E, R> {

	private final TypeInfo<R> typeInfo;
	private final ConfigurationDefinition<E> configDefinition;

	public SubSectionCollectionReturnType(TypeInfo<R> typeInfo, ConfigurationDefinition<E> configDefinition) {
		CollectionKind.fromType(typeInfo); // check argument
		this.typeInfo = Objects.requireNonNull(typeInfo, "type info");
		this.configDefinition = Objects.requireNonNull(configDefinition, "config definition");
	}

	@Override
	public TypeInfo<R> typeInfo() {
		return typeInfo;
	}

	@Override
	public ConfigurationDefinition<E> configDefinition() {
		return configDefinition;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SubSectionCollectionReturnType<?, ?> that = (SubSectionCollectionReturnType<?, ?>) o;
		return typeInfo.equals(that.typeInfo) && configDefinition.equals(that.configDefinition);
	}

	@Override
	public int hashCode() {
		int result = typeInfo.hashCode();
		result = 31 * result + configDefinition.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "SubSectionCollectionReturnType{" +
				"typeInfo=" + typeInfo +
				", configDefinition=" + configDefinition +
				'}';
	}
}
