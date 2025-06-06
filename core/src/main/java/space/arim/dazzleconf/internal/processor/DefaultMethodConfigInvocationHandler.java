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
package space.arim.dazzleconf.internal.processor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf2.internals.ImmutableCollections;
import space.arim.dazzleconf2.internals.MethodUtil;

class DefaultMethodConfigInvocationHandler extends ConfigInvocationHandler {

	private Map<Method, MethodHandle> defaultMethodsMap;
	
	DefaultMethodConfigInvocationHandler(Map<String, Object> configMap) {
		super(configMap);
	}
	
	void initDefaultMethods(Object proxy, Set<Method> defaultMethods) {
		defaultMethodsMap = buildDefaultMethodsMap(proxy, defaultMethods);
	}
	
	private static Map<Method, MethodHandle> buildDefaultMethodsMap(Object proxy, Set<Method> defaultMethods) {
		Map<Method, MethodHandle> result = new HashMap<>(defaultMethods.size());
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		for (Method method : defaultMethods) {
            MethodHandle methodHandle;
            try {
                methodHandle = MethodUtil.createDefaultMethodHandle(method, lookup).bindTo(proxy);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
				throw new IllDefinedConfigException(
						"Unable to generate default method accessor for " + MethodUtil.getQualifiedName(method), ex
				);
            }
            result.put(method, methodHandle);
		}
		return ImmutableCollections.mapOf(result);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() != Object.class && MethodUtil.isDefault(method)) {
			return defaultMethodsMap.get(method).invokeWithArguments(args);
		}
		return super.invoke(proxy, method, args);
	}

}
