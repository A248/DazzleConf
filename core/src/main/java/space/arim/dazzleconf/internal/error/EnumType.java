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

package space.arim.dazzleconf.internal.error;

import java.util.Arrays;
import java.util.Objects;

public final class EnumType implements UserType {

	private final Class<? extends Enum<?>> enumClass;

	public EnumType(Class<? extends Enum<?>> enumClass) {
		this.enumClass = Objects.requireNonNull(enumClass, "enumClass");
	}

	@Override
	public String[] examples() {
		return Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
	}

	@Override
	public String toString() {
		return "an enum value";
	}
}
