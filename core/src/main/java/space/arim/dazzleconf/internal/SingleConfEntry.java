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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.validator.ValueValidator;

public class SingleConfEntry extends ConfEntry {
	
	private final ValueValidator validator;

	public SingleConfEntry(Method method, String key, List<String> comments, ValueValidator validator) {
		super(method, key, comments);
		this.validator = validator;
	}
	
	public ValueValidator getValidator() {
		return validator;
	}
	
	/**
	 * Assuming this entry's return type is a collection, get the element type
	 * 
	 * @return the element type
	 * @throws IllDefinedConfigException if the element type cannot be determined
	 */
	public Class<?> getCollectionElementType() {
		return getGenericReturnParameter(0);
	}
	
	/**
	 * Assuming this entry's return type is a map, get the key type
	 * 
	 * @return the key type
	 * @throws IllDefinedConfigException if the key type cannot be determined
	 */
	public Class<?> getMapKeyType() {
		return getGenericReturnParameter(0);
	}
	
	/**
	 * Assuming this entry's return type is a map, get the value type
	 * 
	 * @return the value type
	 * @throws IllDefinedConfigException if the value type cannot be determined
	 */
	public Class<?> getMapValueType() {
		return getGenericReturnParameter(1);
	}
	
	private Class<?> getGenericReturnParameter(int index) {
		Type genericReturnType = getMethod().getGenericReturnType();
		if (genericReturnType instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) genericReturnType;
			Type[] typeArguments = paramType.getActualTypeArguments();

			Type genericReturnParameter;
			if (typeArguments.length > index && (genericReturnParameter = typeArguments[index]) instanceof Class) {
				return (Class<?>) genericReturnParameter;
			}
		}
		throw new IllDefinedConfigException(
					"Unable to determine return type's generic parameters in " + getQualifiedMethodName());
	}
	
}
