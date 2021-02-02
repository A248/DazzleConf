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

import java.util.Collection;
import java.util.List;
import java.util.Set;

public enum CollectionKind {

	COLLECTION,
	SET,
	LIST;

	public static boolean isCollectionOrSubclass(TypeInfo<?> typeInfo) {
		Class<?> rawType = typeInfo.rawType();
		return rawType.equals(Collection.class) || rawType.equals(Set.class) || rawType.equals(List.class);
	}

	static CollectionKind fromType(TypeInfo<?> typeInfo) {
		Class<?> rawType = typeInfo.rawType();
		if (rawType.equals(Collection.class)) {
			return COLLECTION;
		}
		if (rawType.equals(Set.class)) {
			return SET;
		}
		if (rawType.equals(List.class)) {
			return LIST;
		}
		throw new IllegalArgumentException("Not a Collection/Set/List: " + typeInfo);
	}
}
