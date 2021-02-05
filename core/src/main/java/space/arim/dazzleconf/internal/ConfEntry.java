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
package space.arim.dazzleconf.internal;

import space.arim.dazzleconf.internal.type.ReturnType;
import space.arim.dazzleconf.internal.util.MethodUtil;
import space.arim.dazzleconf.sorter.SortableConfigurationEntry;
import space.arim.dazzleconf.validator.ValueValidator;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public final class ConfEntry implements SortableConfigurationEntry {

	private final Method method;
	private final String key;
	private final List<String> comments;
	private final ReturnType<?> returnType;
	private final ValueValidator validator;

	public ConfEntry(Method method, String key, List<String> comments, ReturnType<?> returnType, ValueValidator validator) {
		this.method = method;
		this.key = key;
		this.comments = comments;
		this.returnType = returnType;
		this.validator = validator;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public List<String> getComments() {
		return comments;
	}

	public ReturnType<?> returnType() {
		return returnType;
	}

	public Optional<ValueValidator> getValidator() {
		return Optional.ofNullable(validator);
	}

	/**
	 * Gets the fully qualified name of the method this entry represents
	 * 
	 * @return the qualified name of the method
	 */
	public String getQualifiedMethodName() {
		return MethodUtil.getQualifiedName(method);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + method.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof ConfEntry)) {
			return false;
		}
		ConfEntry other = (ConfEntry) object;
		return method.equals(other.method);
	}

	@Override
	public String toString() {
		return "ConfEntry [method=" + method + "]";
	}

}
