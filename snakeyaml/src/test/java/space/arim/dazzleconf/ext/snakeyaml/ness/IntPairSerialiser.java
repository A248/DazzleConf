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

package space.arim.dazzleconf.ext.snakeyaml.ness;

import space.arim.dazzleconf.error.BadValueException;
import space.arim.dazzleconf.serialiser.Decomposer;
import space.arim.dazzleconf.serialiser.FlexibleType;
import space.arim.dazzleconf.serialiser.ValueSerialiser;

abstract class IntPairSerialiser<T> implements ValueSerialiser<T> {

	@Override
	public T deserialise(FlexibleType flexibleType) throws BadValueException {
		String[] info = flexibleType.getString().split(":", 2);
		try {
			return fromInts(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
		} catch (NumberFormatException ex) {
			throw flexibleType.badValueExceptionBuilder().cause(ex).build();
		}
	}

	@Override
	public Object serialise(T value, Decomposer decomposer) {
		int[] toInts = toInts(value);
		return toInts[0] + ":" + toInts[1];
	}
	
	abstract T fromInts(int value1, int value2);
	
	abstract int[] toInts(T value);
	
}
