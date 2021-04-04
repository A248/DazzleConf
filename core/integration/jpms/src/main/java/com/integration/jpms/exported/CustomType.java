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

package com.integration.jpms.exported;

import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.serialiser.Decomposer;
import space.arim.dazzleconf.serialiser.FlexibleType;
import space.arim.dazzleconf.serialiser.ValueSerialiser;

public final class CustomType {

	private final String value;

	public CustomType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static class Serialiser implements ValueSerialiser<CustomType> {

		@Override
		public Class<CustomType> getTargetClass() {
			return CustomType.class;
		}

		@Override
		public CustomType deserialise(FlexibleType flexibleType) throws BadValueException {
			return new CustomType(flexibleType.getString());
		}

		@Override
		public Object serialise(CustomType value, Decomposer decomposer) {
			return value.value();
		}
	}
}
