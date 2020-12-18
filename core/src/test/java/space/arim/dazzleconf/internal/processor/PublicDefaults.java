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

import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public class PublicDefaults {

	private static final int PRIMITIVE_VALUE = ThreadLocalRandom.current().nextInt();

	public static int validMethodPrimitive() {
		return PRIMITIVE_VALUE;
	}

	public static String validMethod() {
		return "working";
	}

	public static String throwsException() {
		throw new RuntimeException();
	}

	private static String privateMethod() {
		return "needs to be public";
	}

	public String publicInstanceMethod() {
		return "needs to be static";
	}

	private String privateNonStaticMethod() {
		return "clueless";
	}
}
