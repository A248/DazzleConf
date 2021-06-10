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

package space.arim.dazzleconf.internal.type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeInfoCreationTest<T1, T2 extends CharSequence> {

	private Method getMethod(String methodInThisClass) {
		Method method;
		try {
			method = getClass().getDeclaredMethod(methodInThisClass);
		} catch (NoSuchMethodException ex) {
			throw Assertions.<RuntimeException>fail(ex);
		}
		return method;
	}

	private TypeInfo<?> createInfo(String methodInThisClass) {
		Method method = getMethod(methodInThisClass);
		return new TypeInfoCreation(method.getAnnotatedReturnType()).create(method.getReturnType());
	}

	private <T> TypeInfo<T> simpleInfo(Class<T> type) {
		return new TypeInfo<>(type, List.of(), List.of());
	}

	private <T> TypeInfo<T> genericInfo(Class<T> type, TypeInfo<?>...arguments) {
		return new TypeInfo<>(type, List.of(), List.of(arguments));
	}

	private String simple() { return ""; }
	@Test
	public void createSimple() {
		assertEquals(simpleInfo(String.class), createInfo("simple"));
	}

	private List<Integer> generic() { return List.of(); }
	@Test
	public void createGeneric() {
		assertEquals(
				genericInfo(List.class, simpleInfo(Integer.class)),
				createInfo("generic"));
	}

	private List<Set<String>> genericOfGeneric() { return List.of(); }
	@Test
	public void createGenericOfGeneric() {
		assertEquals(
				genericInfo(List.class, genericInfo(Set.class, simpleInfo(String.class))),
				createInfo("genericOfGeneric"));
	}

	private List<T1> genericVariable() { return List.of(); }
	@Test
	public void createGenericVariable() {
		assertEquals(
				genericInfo(List.class, simpleInfo(Object.class)),
				createInfo("genericVariable"));
	}

	private List<T2> genericBoundVariable() { return List.of(); }
	@Test
	public void createGenericBoundVariable() {
		assertEquals(
				genericInfo(List.class, simpleInfo(CharSequence.class)),
				createInfo("genericBoundVariable"));
	}
}
