/* 
 * DazzleConf-core
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-core. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.internal.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import space.arim.dazzleconf.error.IllDefinedConfigException;

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
	 * @throws IllDefinedConfigException if unable to generate the default method handle
	 */
	public static MethodHandle createDefaultMethodHandle(Method method) {
		if (IS_JAVA_8) {
			return Java8DefaultMethodHandle.getMethodHandle(method);
		}
		Class<?> declaringClass = method.getDeclaringClass();
		try {
			return MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup())
					.unreflectSpecial(method, declaringClass);
		} catch (IllegalAccessException ex) {
			throw new IllDefinedConfigException(
					"Unable to generate default method accessor for " + getQualifiedName(method), ex);
		}
	}
	
	private static final boolean IS_JAVA_8;
	
	static {
		boolean isJava8 = false;
		try {
			MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
		} catch (NoSuchMethodException nsme) {
			isJava8 = true;
		}
		IS_JAVA_8 = isJava8;
	}
	
	private static class Java8DefaultMethodHandle {
		
		private static final Constructor<MethodHandles.Lookup> lookupConstructor;
		
		static {
			try {
				lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
				lookupConstructor.setAccessible(true);
			} catch (NoSuchMethodException | SecurityException ex) {
				throw new ExceptionInInitializerError(ex);
			}
		}
		
		static MethodHandle getMethodHandle(Method method) {
			Class<?> declaringClass = method.getDeclaringClass();
			try {
				return lookupConstructor.newInstance(declaringClass).unreflectSpecial(method, declaringClass);

			} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
				throw new IllDefinedConfigException(
						"Unable to generate default method accessor for " + getQualifiedName(method), ex);
			}
		}
	}
	
}
