/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

package space.arim.dazzleconf.backend;

import java.util.HashMap;
import java.util.Map;

public record ModelMap(Map<Object, DataEntry> value, boolean mutable) {
    ModelMap intoImmut() {
        if (!mutable) {
            return this;
        }
        Map<Object, DataEntry> immutValue = new HashMap<>();
        value.forEach((key, value) -> immutValue.put(key, value.intoImmutDeep()));
        return new ModelMap(Map.copyOf(immutValue), false);
    }

    ModelMap intoMut() {
        if (mutable) {
            return this;
        }
        Map<Object, DataEntry> mutValue = new HashMap<>();
        value.forEach((key, value) -> mutValue.put(key, value.intoMutDeep()));
        return new ModelMap(mutValue, true);
    }

    void setAll(ModelMap from) {
        if (this == from) {
            return;
        }
        value.clear();
        value.putAll(from.value);
    }
}
