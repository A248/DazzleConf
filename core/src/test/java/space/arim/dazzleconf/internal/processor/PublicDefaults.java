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

package space.arim.dazzleconf.internal.processor;

@SuppressWarnings("unused")
public class PublicDefaults {

	public static Value validMethod() {
		return new Value("working");
	}

	public static Value throwsException() {
		throw new RuntimeException();
	}

	private static Value privateMethod() {
		return new Value("needs to be public");
	}

	public Value publicInstanceMethod() {
		return new Value("needs to be static");
	}

	private Value privateNonStaticMethod() {
		return new Value("clueless");
	}
}
