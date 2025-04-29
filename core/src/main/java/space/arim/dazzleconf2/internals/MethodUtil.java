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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import space.arim.dazzleconf2.internals.jdk11.Java11DefaultMethodProvider;

public final class MethodUtil {

	private MethodUtil() {}
	
	/**
	 * Gets the qualified name of a method
	 * 
	 * @param method the method
	 * @return the qualified name
	 */
	public static String getQualifiedName(Method method) {
		return method.getDeclaringClass().getName() + "#" + method.getName();
	}
	
	/**
	 * Micro optimised version of {@link Method#isDefault()} since it is known the declaring class is an interface
	 * 
	 * @param method the method, known to be declared in an interface
	 * @return true if the method is a default method
	 */
	public static boolean isDefault(Method method) {
		int modifiers = method.getModifiers();
		boolean isDefault = ((modifiers & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) ==
                Modifier.PUBLIC);
		assert isDefault == method.isDefault();
		return isDefault;
	}
	
	/**
	 * Creates a method handle invoking a default method
	 * 
	 * @param method the method
	 * @return a method handle for the default method
	 */
	public static MethodHandle createDefaultMethodHandle(Method method)
			throws IllegalAccessException, InstantiationException, InvocationTargetException {
		return DEFAULT_METHOD_PROVIDER.getMethodHandle(method);
	}
	
	private static final DefaultMethodProvider DEFAULT_METHOD_PROVIDER = createDefaultMethodProvider();

	private static DefaultMethodProvider createDefaultMethodProvider() {
		try {
			MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
			return new Java11DefaultMethodProvider();
		} catch (NoSuchMethodException nsme) {
			return new Java8DefaultMethodProvider();
		}
	}

}
