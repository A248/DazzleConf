/* 
 * DazzleConf-core
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-core. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf;

import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.serialiser.Decomposer;
import space.arim.dazzleconf.serialiser.FlexibleType;
import space.arim.dazzleconf.serialiser.ValueSerialiser;

public class NumericPairSerialiser implements ValueSerialiser<NumericPair> {

	@Override
	public Class<NumericPair> getTargetClass() {
		return NumericPair.class;
	}

	@Override
	public NumericPair deserialise(FlexibleType flexibleType) throws BadValueException {
		String value = flexibleType.getString();
		String[] split = value.split(":");
		if (split.length != 2) {
			throw flexibleType.badValueExceptionBuilder()
				.message("Value '" + value + "' must be in the form integer1:integer2").build();
		}
		try {
			return new NumericPair(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		} catch (NumberFormatException ex) {
			throw flexibleType.badValueExceptionBuilder()
				.message("Value '" + value + "' must be in the form integer1:integer2").cause(ex).build();
		}
	}

	@Override
	public Object serialise(NumericPair value, Decomposer decomposer) {
		return value.getValue1() + ":" + value.getValue2();
	}

}
