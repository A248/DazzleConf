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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public enum ElementaryType implements UserType {
	BOOLEAN,
	NUMBER,
	CHARACTER,
	STRING,
	LIST,
	SECTION;

	public static ElementaryType fromElementaryObject(Object obj) {
		Objects.requireNonNull(obj, "obj");
		if (obj instanceof Boolean) {
			return ElementaryType.BOOLEAN;
		}
		if (obj instanceof Number) {
			return ElementaryType.NUMBER;
		}
		if (obj instanceof Character) {
			return ElementaryType.CHARACTER;
		}
		if (obj instanceof String) {
			return ElementaryType.STRING;
		}
		if (obj instanceof List || obj instanceof Set) {
			return ElementaryType.LIST;
		}
		if (obj instanceof Map) {
			return ElementaryType.SECTION;
		}
		throw new IllegalArgumentException(
				"Found elementary object " + obj + " which is unexpectedly a " + obj.getClass());
	}

	@Override
	public String[] examples() {
		switch (this) {
		case BOOLEAN:
			return new String[]{"true", "false"};
		case NUMBER:
			return new String[]{"1", "14.23", "-2"};
		case CHARACTER:
			return new String[]{"'a'", "'f'", "'z'"};
		case STRING:
			return new String[]{"'hello everyone'", "'good fortune shines upon us'"};
		case LIST:
			return new String[]{};
		case SECTION:
			return new String[]{};
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String toString() {
		switch (this) {
		case BOOLEAN:
			return "a true/false value";
		case NUMBER:
			return "a numerical value";
		case CHARACTER:
			return "a single character";
		case STRING:
			return "a text value";
		case LIST:
			return "a list or group of values";
		case SECTION:
			return "a configuration section";
		default:
			throw new IllegalArgumentException();
		}
	}
}
