/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

package space.arim.dazzleconf2.reflect;

import space.arim.dazzleconf2.internals.ArrayType;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

final class GenericCompute {

    private final GenericContext genericContext;

    GenericCompute(GenericContext genericContext) {
        this.genericContext = genericContext;
    }

    ReifiedType reify(AnnotatedType type) {
        if (type instanceof AnnotatedParameterizedType) {
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) type;

            AnnotatedType[] sourceArgs = parameterizedType.getAnnotatedActualTypeArguments();
            ReifiedType[] reifiedArgs = new ReifiedType[sourceArgs.length];
            for (int n = 0; n < sourceArgs.length; n++) {
                reifiedArgs[n] = reify(sourceArgs[n]);
            }
            Class<?> mainType = (Class<?>) ((ParameterizedType) parameterizedType.getType()).getRawType();
            return new ReifiedType(mainType, reifiedArgs, ReifiedAnnotations.computeFrom(type));
        }
        if (type instanceof AnnotatedTypeVariable) {
            String typeVarName = ((TypeVariable<?>) type.getType()).getName();
            return genericContext.resolveTypeVariable(typeVarName);
        }
        if (type instanceof AnnotatedWildcardType) {
            AnnotatedWildcardType wildcardType = (AnnotatedWildcardType) type;

            return reify(wildcardType.getAnnotatedUpperBounds()[0]);
        }
        if (type instanceof AnnotatedArrayType) {
            AnnotatedArrayType arrayType = (AnnotatedArrayType) type;

            // Use the component type as the source of annotations - discard annotations on the array
            AnnotatedType annotatedComponent = arrayType.getAnnotatedGenericComponentType();
            ReifiedType reifiedComponent = reify(annotatedComponent);
            return new ReifiedType(
                    ArrayType.arrayType(reifiedComponent.rawType()),
                    reifiedComponent.arguments(),
                    ReifiedAnnotations.computeFrom(annotatedComponent)
            );
        }
        Type rawType = type.getType();
        if (rawType instanceof Class) {
            return new ReifiedType((Class<?>) rawType, ReifiedType.EMPTY_ARRAY, ReifiedAnnotations.computeFrom(type));
        }
        throw new IllegalStateException("Unable to reify type " + type + " using context " + genericContext);
    }

    static Class<?> eraseType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return eraseType(parameterizedType.getRawType());
        }
        if (type instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            return erasedBound(typeVariable.getBounds());
        }
        if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;
            return erasedBound(wildcard.getUpperBounds());
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType array = (GenericArrayType) type;
            return ArrayType.arrayType(eraseType(array.getGenericComponentType()));
        }
        throw new IllegalStateException("Unable to erase type " + type);
    }

    private static Class<?> erasedBound(Type[] bounds) {
        if (bounds.length == 0) {
            return Object.class;
        }
        Class<?> firstBound = eraseType(bounds[0]);
        return firstBound.isInterface() ? Object.class : firstBound;
    }
}
