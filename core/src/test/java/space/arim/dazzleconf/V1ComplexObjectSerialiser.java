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

import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.serialiser.Decomposer;
import space.arim.dazzleconf.serialiser.FlexibleType;
import space.arim.dazzleconf.serialiser.ValueSerialiser;

import java.util.Map;

class V1ComplexObjectSerialiser implements ValueSerialiser<V1ComplexObject> {

	@Override
	public Class<V1ComplexObject> getTargetClass() {
		return V1ComplexObject.class;
	}

	@Override
	public V1ComplexObject deserialise(FlexibleType flexibleType) throws BadValueException {
		Map<String, FlexibleType> map = flexibleType.getMap((flexKey, flexValue) -> {
			return Map.entry(flexKey.getString(), flexValue);
		});
		FlexibleType count = map.get("count");
		FlexibleType name = map.get("name");
		FlexibleType enabled = map.get("enabled");
		if (count == null || name == null || enabled == null) {
			throw flexibleType.badValueExceptionBuilder().message("Missing one of [count, name, enabled]").build();
		}
		return new V1ComplexObject(count.getInteger(), name.getString(), enabled.getBoolean());
	}

	@Override
	public Object serialise(V1ComplexObject value, Decomposer decomposer) {
		return Map.of("count", value.count(), "name", value.name(), "enabled", value.enabled());
	}
}
