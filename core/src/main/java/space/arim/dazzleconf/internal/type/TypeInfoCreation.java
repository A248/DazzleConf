/*
 * DazzleConf
 * Copyright © 2021 Anand Beh
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

package space.arim.dazzleconf.internal.type;

import space.arim.dazzleconf.internal.util.ImmutableCollections;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

public class TypeInfoCreation {

	private final AnnotatedType annotatedType;

	public TypeInfoCreation(AnnotatedType annotatedType) {
		this.annotatedType = annotatedType;
	}

	public <T> TypeInfo<T> create(Class<T> rawType) {
		return new TypeInfo<>(
				rawType,
				ImmutableCollections.listOf(annotatedType.getAnnotations()),
				getArguments());
	}

	private List<TypeInfo<?>> getArguments() {
		if (!(annotatedType instanceof AnnotatedParameterizedType)) {
			return ImmutableCollections.emptyList();
		}
		AnnotatedParameterizedType paramAnnotatedType = ((AnnotatedParameterizedType) annotatedType);
		AnnotatedType[] typeArguments = paramAnnotatedType.getAnnotatedActualTypeArguments();
		List<TypeInfo<?>> arguments = new ArrayList<>(typeArguments.length);
		for (AnnotatedType typeArgument : typeArguments) {
			arguments.add(new TypeInfoCreation(typeArgument).create());
		}
		return arguments;
	}

	private TypeInfo<?> create() {
		return create(getRawType(annotatedType.getType()));
	}

	private static Class<?> getRawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		if (type instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) type).getRawType();
			// getRawType() should always be a Class.
			// See https://stackoverflow.com/a/5767681
			return (Class<?>) rawType;
		}
		if (type instanceof WildcardType) {
			return getRawType(((WildcardType) type).getUpperBounds()[0]);
		}
		if (type instanceof TypeVariable) {
			return getRawType(((TypeVariable<?>) type).getBounds()[0]);
		}
		if (type instanceof GenericArrayType) {
			Class<?> rawComponentType = getRawType(((GenericArrayType) type).getGenericComponentType());
			// Create a temporary array in order to determine the erasure type
			Object array = Array.newInstance(rawComponentType, 0);
			return array.getClass();
		}
		return Object.class;
	}

}
