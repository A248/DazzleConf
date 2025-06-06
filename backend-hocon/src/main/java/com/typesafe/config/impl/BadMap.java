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

package com.typesafe.config.impl;

//
// OVERWRITES lightbend/config's BadMap.java
// See lightbend/config 817, about a bug in BadMap
// https://github.com/lightbend/config/pull/817#issuecomment-2955109160
//

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class BadMap<K,V> {

    private final Map<K, V> backing;

    BadMap() {
        this(Collections.emptyMap());
    }

    private BadMap(Map<K, V> backing) {
        this.backing = backing;
    }

    BadMap<K,V> copyingPut(K k, V v) {
        Map<K, V> newMap = new HashMap<>(backing.size() + 2, 0.9999999f);
        newMap.putAll(backing);
        newMap.put(k, v);
        return new BadMap<>(newMap);
    }

    V get(K k) {
        return backing.get(k);
    }
}