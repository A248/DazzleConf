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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import space.arim.dazzleconf.internal.DefaultMethodUtil;

class DefaultMethodConfigInvocationHandler extends ConfigInvocationHandler {

	private final ConcurrentMap<Method, MethodHandle> defaultMethods = new ConcurrentHashMap<>();
	
	DefaultMethodConfigInvocationHandler(Map<String, Object> configMap) {
		super(configMap);
	}
	
	private MethodHandle defaultMethodHandle(Object proxy, Method method) {
		MethodHandle defaultHandle = defaultMethods.computeIfAbsent(method, (defaultMethod) -> {
			return DefaultMethodUtil.createDefaultMethodHandle(method).bindTo(proxy);
		});
		return defaultHandle;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (DefaultMethodUtil.isDefault(method)) {
			return defaultMethodHandle(proxy, method).invokeWithArguments(args);
		}
		return super.invoke(proxy, method, args);
	}

}
