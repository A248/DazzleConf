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

import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.serialiser.ValueSerialiser;
import space.arim.dazzleconf.validator.ValueValidator;

public class SingleConfEntry extends ConfEntry {
	
	private final ValueSerialiser<?> serialiser;
	private final ValueValidator validator;
	
	SingleConfEntry(Method method, ValueSerialiser<?> serialiser, ValueValidator validator) {
		super(method);
		this.serialiser = serialiser;
		this.validator = validator;
	}

	public ValueSerialiser<?> getSerialiser() {
		return serialiser;
	}
	
	public ValueValidator getValidator() {
		return validator;
	}
	
	/**
	 * Assuming this entry's config value is a collection, get the element type
	 * 
	 * @return the element type
	 * @throws IllDefinedConfigException if the element type cannot be determined
	 */
	public Class<?> getCollectionElementType() {
		Type genericReturnType = getMethod().getGenericReturnType();
		if (genericReturnType instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) genericReturnType;
			Type[] typeArguments = paramType.getActualTypeArguments();
			Type elementType;
			if (typeArguments.length > 0 && (elementType = typeArguments[0]) instanceof Class) {
				return (Class<?>) elementType;
			}
		}
		throw new IllDefinedConfigException(
					"Unable to determine element type of collection in " + getMethod().getName());
	}
	
}
