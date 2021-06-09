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

package space.arim.dazzleconf.serialiser;

import org.junit.jupiter.api.Assertions;
import space.arim.dazzleconf.error.BadValueException;

public class Serializer implements ValueSerialiser<TheType> {

	boolean wasCalled;

	@Override
	public Class<TheType> getTargetClass() {
		return TheType.class;
	}

	@Override
	public TheType deserialise(FlexibleType flexibleType) throws BadValueException {
		throw Assertions.<RuntimeException>fail("Not implemented");
	}

	@Override
	public Object serialise(TheType value, Decomposer decomposer) {
		throw Assertions.<RuntimeException>fail("Not implemented");
	}
}
