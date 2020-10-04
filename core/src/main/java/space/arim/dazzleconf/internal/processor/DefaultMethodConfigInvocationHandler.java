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
package space.arim.dazzleconf.internal.processor;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import space.arim.dazzleconf.internal.DefaultMethodUtil;
import space.arim.dazzleconf.internal.ImmutableCollections;

class DefaultMethodConfigInvocationHandler extends ConfigInvocationHandler {

	private volatile Map<Method, MethodHandle> defaultMethodsMap;
	
	DefaultMethodConfigInvocationHandler(Map<String, Object> configMap) {
		super(configMap);
	}
	
	void initDefaultMethods(Object proxy, Set<Method> defaultMethods) {
		defaultMethodsMap = buildDefaultMethodsMap(proxy, defaultMethods);
	}
	
	private static Map<Method, MethodHandle> buildDefaultMethodsMap(Object proxy, Set<Method> defaultMethods) {
		Map<Method, MethodHandle> result = new HashMap<>(defaultMethods.size());
		for (Method method : defaultMethods) {
			MethodHandle methodHandle = DefaultMethodUtil.createDefaultMethodHandle(method).bindTo(proxy);
			result.put(method, methodHandle);
		}
		return ImmutableCollections.mapOf(result);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() != Object.class && DefaultMethodUtil.isDefault(method)) {
			return defaultMethodsMap.get(method).invokeWithArguments(args);
		}
		return super.invoke(proxy, method, args);
	}

}
