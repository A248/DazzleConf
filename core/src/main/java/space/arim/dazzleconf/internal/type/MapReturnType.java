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

public interface MapReturnType<K, V> extends ReturnType<Map<K, V>> {

	default TypeInfo<K> keyTypeInfo() {
		@SuppressWarnings("unchecked")
		TypeInfo<K> casted = (TypeInfo<K>) typeInfo().arguments().get(0);
		return casted;
	}

	default TypeInfo<V> valueTypeInfo() {
		@SuppressWarnings("unchecked")
		TypeInfo<V> casted = (TypeInfo<V>) typeInfo().arguments().get(1);
		return casted;
	}

}
