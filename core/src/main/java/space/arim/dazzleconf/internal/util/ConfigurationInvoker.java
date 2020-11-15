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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import space.arim.dazzleconf.internal.ConfEntry;

/**
 * Wrapper around configuration instances supporting more efficient reflective invocation
 * 
 * @author A248
 *
 * @param <C> the configuration type
 */
public class ConfigurationInvoker<C> {

	private final C configData;
	
	/** Nonnull if configData is a proxy */
	private transient final InvocationHandler proxyHandler;
	
	public ConfigurationInvoker(C configData) {
		this.configData = configData;
		if (Proxy.isProxyClass(configData.getClass())) {
			proxyHandler = Proxy.getInvocationHandler(configData);
		} else {
			proxyHandler = null;
		}
	}
	
	public Object getEntryValue(ConfEntry entry) {
		Object value;
		Method method = entry.getMethod();
		try {
			if (proxyHandler != null) {
				value = proxyHandler.invoke(configData, method, null);
			} else {
				value = method.invoke(configData);
			}

		} catch (RuntimeException | Error ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new RuntimeException("Exception while invoking implementation of " + entry.getQualifiedMethodName()
			+ " in " + configData.getClass().getName(), ex);
		}
		if (value == null) {
			throw new NullPointerException(
					entry.getQualifiedMethodName() + " implementation in " + configData.getClass() + " returned null");
		}
		return value;
	}
	
}
