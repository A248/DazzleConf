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

package space.arim.dazzleconf2.internals;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Java8DefaultMethodProvider implements DefaultMethodProvider {

	private static final Constructor<MethodHandles.Lookup> lookupConstructor;

	static {
		try {
			lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
			lookupConstructor.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@Override
	public MethodHandle getMethodHandle(Method method, MethodHandles.Lookup lookup)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Class<?> declaringClass = method.getDeclaringClass();
		return lookupConstructor.newInstance(declaringClass).unreflectSpecial(method, declaringClass);
	}
}
