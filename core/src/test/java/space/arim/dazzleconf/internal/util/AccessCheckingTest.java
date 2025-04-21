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

package space.arim.dazzleconf.internal.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static space.arim.dazzleconf2.internals.AccessChecking.isAccessible;

public class AccessCheckingTest {

	@Test
	public void publicAndExportedIsAccessible() {
		assertTrue(isAccessible(Object.class));
	}

	@Test
	public void nonPublicIsInaccessible() {
		assertFalse(isAccessible(List.of().getClass()));
	}

	@Test
	public void publicButNotExportedIsInaccessible() {
		// Specifically using this class because it is less likely to vanish in the future
		Class<?> algorithmId;
		try {
			algorithmId = Class.forName("sun.security.x509.AlgorithmId");
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Please update this test to the latest JDK", ex);
		}
		// Check assumptions
		assertTrue(Modifier.isPublic(algorithmId.getModifiers()));
		assertFalse(algorithmId.getModule().isExported(algorithmId.getPackageName()),
				"Expected " + algorithmId.getModule() + " to not be exported");
		// Main assertion
		assertFalse(isAccessible(algorithmId));
	}
}
