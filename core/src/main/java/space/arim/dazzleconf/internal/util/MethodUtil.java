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
		try {
			return DEFAULT_METHOD_PROVIDER.getMethodHandle(method);
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			throw new IllDefinedConfigException(
					"Unable to generate default method accessor for " + getQualifiedName(method), ex);
		}
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

	interface DefaultMethodProvider {

		MethodHandle getMethodHandle(Method method)
				throws IllegalAccessException, InstantiationException, InvocationTargetException;

	}

	private static class Java11DefaultMethodProvider implements DefaultMethodProvider {

		@Override
		public MethodHandle getMethodHandle(Method method) throws IllegalAccessException {
			/*
			* privateLookupIn requires the calling module read the target module
			* This will indeed result in a cyclic module dependency. Luckily,
			* JPMS permits cyclic dependencies created through readability edges
			* (addReads) even though it strictly forbids cycles during initialization
			* of the module graph.
			* https://openjdk.java.net/projects/jigsaw/spec/issues/#CyclicDependences
			*/
			Class<?> declaringClass = method.getDeclaringClass();
			MethodUtil.class.getModule().addReads(declaringClass.getModule());
			return MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup())
					.unreflectSpecial(method, declaringClass);
		}

	}
	
	private static class Java8DefaultMethodProvider implements DefaultMethodProvider {
		
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
		public MethodHandle getMethodHandle(Method method)
				throws IllegalAccessException, InvocationTargetException, InstantiationException {
			Class<?> declaringClass = method.getDeclaringClass();
			return lookupConstructor.newInstance(declaringClass).unreflectSpecial(method, declaringClass);
		}
	}
	
}
