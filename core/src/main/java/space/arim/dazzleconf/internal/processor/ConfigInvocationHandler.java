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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import space.arim.dazzleconf2.internals.ImmutableCollections;

class ConfigInvocationHandler implements InvocationHandler {

	private final Map<String, Object> configMap;
	
	ConfigInvocationHandler(Map<String, Object> configMap) {
		this.configMap = ImmutableCollections.mapOf(configMap);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == Object.class) {
			if (method.getName().equals("equals")) {
				return implementEquals(proxy, args[0]);
			}
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

	/**
	 * Determines equality with another object
	 *
	 * @param ourProxy our proxy
	 * @param theirConfig their config, may be a proxy
	 * @return true if equal, false otherwise
	 */
	private static boolean implementEquals(Object ourProxy, Object theirConfig) {
		/*
		 * Equals implementation - identity equality.
		 * There is no better way to implement .equals in accordance with
		 * the reflexivity contract.
		 *
		 * Ruled out possbilities:
		 * - Attempt to invoke all the methods on the other config. Fails
		 *   because the other config could implement an extended config
		 *   interface.
		 * - Check if the other object is a proxy, and then analyze whether
		 *   the configMap of its invocation handler is the same. Fails
		 *   because the other config could implement a config interface
		 *   which has the same method names, but is a different interface.
		 * - Building on the previous attempts, perform additional analysis of the
		 *   implementing proxy class in order to determine whether both proxies
		 *   implement the same interfaces. Since java.lang.reflect.Proxy uses
		 *   a cache of interfaces -> generated proxy classes, proxy.getClass()
		 *   will be identical for proxies implementing the same interfaces.
		 *   Fails because it relies on an implementation detail which could change
		 *   in a later JDK version.
		 *
		 */
		return ourProxy == theirConfig;
	}
	
	@Override
	public String toString() {
		return "ConfigInvocationHandler [configMap=" + configMap + "]";
	}
	
}
