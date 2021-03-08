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

import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.type.ReturnType;
import space.arim.dazzleconf.internal.type.ReturnTypeWithConfigDefinition;
import space.arim.dazzleconf.internal.util.AccessChecking;
import space.arim.dazzleconf.internal.util.MethodUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

class DefaultObjectHelper {

	private final ConfEntry entry;
	private final ProcessorBase<?> processor;

	DefaultObjectHelper(ConfEntry entry, ProcessorBase<?> processor) {
		this.entry = entry;
		this.processor = processor;
	}

	private String reasonToExceptionMessage(String reason) {
		return "Issue with defaults annotation on " + entry.getQualifiedMethodName()
				+ ". Reason: " + reason;
	}

	IllDefinedConfigException badDefault(String reason) {
		return new IllDefinedConfigException(reasonToExceptionMessage(reason));
	}

	private IllDefinedConfigException badDefault(String reason, Throwable cause) {
		return new IllDefinedConfigException(reasonToExceptionMessage(reason), cause);
	}

	Map<String, String> toMap(String...values) {
		Map<String, String> result = new HashMap<>();

		String key = null;
		for (String value : values) {
			if (key == null) {
				key = value;
			} else {
				result.put(key, value);
				key = null;
			}
		}
		if (key != null) {
			throw badDefault("@DefaultMap must consist of key-value pairs");
		}
		return result;
	}

	private Method locateMethod(String fullyQualifiedMethodName) {
		Class<?> clazz;
		String methodName;

		int index = fullyQualifiedMethodName.lastIndexOf('.');
		if (index == -1) {
			// Method is inside the same config class
			clazz = entry.getMethod().getDeclaringClass(); // config class
			methodName = fullyQualifiedMethodName;

		} else if (index == fullyQualifiedMethodName.length() - 1) {
			throw badDefault(
					"Malformed method name " + fullyQualifiedMethodName + " specified by @DefaultObject. " +
							"Please ensure the value of @DefaultObject is a fully qualified method name.");
		} else {
			// Method is in another class
			String className = fullyQualifiedMethodName.substring(0, index);
			methodName = fullyQualifiedMethodName.substring(index + 1);
			Class<?> configClass = entry.getMethod().getDeclaringClass();
			try {
				clazz = Class.forName(className, true, configClass.getClassLoader());
			} catch (ClassNotFoundException ex) {
				throw badDefault("Class " + className + " not found", ex);
			}
			if (!AccessChecking.isAccessible(clazz)) {
				throw badDefault("Method " + fullyQualifiedMethodName + " must be in an accessible class");
			}
		}
		/*
		 * Support 2 kinds of default object-producing methods:
		 * - Methods taking no parameters
		 * - Methods used for a ReturnTypeWithConfigDefinition, where the user may want
		 *   to use the default configuration in the return value.
		 */
		NoSuchMethodException attemptOneEx;
		try {
			// No parameters
			return clazz.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException ex) {
			attemptOneEx = ex;
		}
		// Look for method with config class parameter, if possible
		ReturnType<?> returnType = entry.returnType();
		if (!(returnType instanceof ReturnTypeWithConfigDefinition)) {
			throw badDefault("Method " + methodName + " not found in class " + clazz.getName(), attemptOneEx);
		}
		Class<?> configClass = ((ReturnTypeWithConfigDefinition<?, ?>) returnType).configDefinition().getConfigClass();
		try {
			return clazz.getDeclaredMethod(methodName, configClass);
		} catch (NoSuchMethodException ex) {
			ex.addSuppressed(attemptOneEx);
			throw badDefault("Method " + methodName + " not found in class " + clazz.getName(), ex);
		}
	}

	Object toObject(String methodName) throws InvalidConfigException {
		Class<?> targetType = entry.returnType().typeInfo().rawType();
		if (targetType.isPrimitive() || targetType.equals(String.class)) {
			throw new IllDefinedConfigException(
					"@DefaultObject cannot be used for primitives or strings. " +
					"Use one of @DefaultBoolean for boolean, @DefaultInteger for int/short/byte, " +
					"@DefaultLong for long, @DefaultDouble for double/float, or @DefaultString for String/char");
		}
		Method method = locateMethod(methodName);
		int modifiers = method.getModifiers();
		if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
			throw badDefault("Method " + MethodUtil.getQualifiedName(method) + " must be public and static");
		}
		method.setAccessible(true);
		Object result = fromMethod(method);
		if (result == null) {
			throw badDefault("Object returned from @DefaultObject was null");
		}
		if (!targetType.isInstance(result)) {
			throw badDefault("Object returned from @DefaultObject must be an instance of " +
					"the return type of the config method.");
		}
		return result;
	}

	private Object fromMethod(Method method) throws InvalidConfigException {
		// Based on #locateMethod, this method may or may not take the config class as a parameter
		if (method.getParameterCount() == 0) {
			return invokeStaticMethod(method);
		} else {
			ReturnTypeWithConfigDefinition<?, ?> returnType = (ReturnTypeWithConfigDefinition<?, ?>) entry.returnType();
			Object config = processor.createNested(entry, returnType, DefaultsProcessor.CREATE_DEFAULT_SECTION);
			return invokeStaticMethod(method, config);
		}
	}

	private Object invokeStaticMethod(Method method, Object...args) {
		try {
			return method.invoke(null, args);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw badDefault("Exception invoking method " + MethodUtil.getQualifiedName(method), ex);
		}
	}

}
