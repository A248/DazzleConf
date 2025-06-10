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

import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Provides ordering of maps, plus optimization and safety, when constructing hocon objects

public final class WriteHocon_Access {

    private final ConfigOrigin origin;

    public WriteHocon_Access(String originName) {
        origin = SimpleConfigOrigin.newSimple(originName);
    }

    public ConfigObject fromMap(LinkedHashMap<String, ConfigValue> map) {
        // These casts are correct, since callers are presumably using
        // Cast Map<String, ConfigValue> -> Map<String, ? extends ConfigValue>
        Map<String, ? extends ConfigValue> alsoMap = map;
        // Cast Map<String, ? extends ConfigValue> -> Map<String, AbstractConfigValue>
        @SuppressWarnings("unchecked")
        Map<String, AbstractConfigValue> castMap = (Map<String, AbstractConfigValue>) alsoMap;
        return new SimpleConfigObject(origin, castMap);
    }

    public ConfigList fromList(List<ConfigValue> list) {
        // We pull off the same trick as in fromMap
        List<? extends ConfigValue> alsoList = list;
        @SuppressWarnings("unchecked")
        List<AbstractConfigValue> castList = (List<AbstractConfigValue>) alsoList;
        return new SimpleConfigList(origin, castList);
    }

    public ConfigValue fromScalar(Object scalar) {
        if (scalar instanceof List || scalar instanceof Map) {
            throw new IllegalArgumentException("Must use fromMap or fromList for maps and lists");
        }
        if (scalar instanceof Byte || scalar instanceof Short) {
            // lightbend/config does not implement byte and short directly. Let's bypass conversion to double
            Number scalarNumber = (Number) scalar;
            scalar = scalarNumber.intValue();
        }
        if (scalar instanceof Character) {
            scalar = ((Character) scalar).toString();
        }
        // Note that float/Float is not one of the implemented types, see lightbend/config 776
        // So, floats may be rendered awkwardly. https://github.com/lightbend/config/pull/776
        return ConfigImpl.fromAnyRef(scalar, origin, FromMapMode.KEYS_ARE_KEYS);
    }
}
