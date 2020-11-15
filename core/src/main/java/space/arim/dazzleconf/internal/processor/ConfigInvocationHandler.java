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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import space.arim.dazzleconf.internal.util.ImmutableCollections;

class ConfigInvocationHandler implements InvocationHandler {

	private final Map<String, Object> configMap;
	
	ConfigInvocationHandler(Map<String, Object> configMap) {
		this.configMap = ImmutableCollections.mapOf(configMap);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == Object.class) {
			return invokeMethodOnSelf(method, args);
		}
		assert args == null : Arrays.deepToString(args);
		return configMap.get(method.getName());
	}
	
	private Object invokeMethodOnSelf(Method method, Object[] args) throws Throwable {
		try {
			return method.invoke(this, args);
		} catch (IllegalAccessException | IllegalArgumentException ex) {
			throw new AssertionError(ex);

		} catch (InvocationTargetException ex) {
			Throwable cause = ex.getCause();
			if (cause != null) {
				throw cause;
			} else {
				throw ex;
			}
		}
	}
	
	@Override
	public String toString() {
		return "ConfigInvocationHandler [configMap=" + configMap + "]";
	}
	
}
