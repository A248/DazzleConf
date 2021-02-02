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
import space.arim.dazzleconf.internal.ConfEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

class DefaultObjectHelper {

	private final ConfEntry entry;

	DefaultObjectHelper(ConfEntry entry) {
		this.entry = entry;
	}

	IllDefinedConfigException badDefault(String reason) {
		return new IllDefinedConfigException(
				"Invalid defaults annotation on " + entry.getQualifiedMethodName()
						+ ". Reason: " + reason);
	}

	private IllDefinedConfigException badDefault(String reason, Throwable cause) {
		return new IllDefinedConfigException(
				"Invalid defaults annotation on " + entry.getQualifiedMethodName()
						+ ". Reason: " + reason, cause);
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
		int index = fullyQualifiedMethodName.lastIndexOf('.');
		if (index == -1 || index == fullyQualifiedMethodName.length() - 1) {
			throw badDefault("Malformed method name specified by @DefaultObject. " +
					"Check to ensure the value of @DefaultObject is a fully qualified method name.");
		}
		String className = fullyQualifiedMethodName.substring(0, index);
		String methodName = fullyQualifiedMethodName.substring(index + 1);
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException ex) {
			throw badDefault("Class " + className + " not found", ex);
		}
		if (!Modifier.isPublic(clazz.getModifiers())) {
			throw badDefault("Method " + entry.getQualifiedMethodName() + " must be in a public class");
		}
		Method method;
		try {
			method = clazz.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException ex) {
			throw badDefault("Method " + methodName + " not found in class " + className, ex);
		}
		return method;
	}

	Object toObject(String methodName) {
		Method method = locateMethod(methodName);
		int modifiers = method.getModifiers();
		if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
			throw badDefault("Method " + entry.getQualifiedMethodName() + " must be public and static");
		}
		method.setAccessible(true);
		try {
			return method.invoke(null);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw badDefault("Exception invoking method " + entry.getQualifiedMethodName(), ex);
		}
	}

}
