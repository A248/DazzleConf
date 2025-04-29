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

package space.arim.dazzleconf2.reflect;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.internals.ArrayType;

import java.lang.reflect.*;

/**
 * A low level helper for reifying generic members within context of a generic class declaration.
 *
 */
public final class GenericContext {

    private final ReifiedType.Annotated parent;
    private final TypeVariable<?>[] typeVars;

    /**
     * Creates from a parent type.
     * <p>
     * This is the type that provides class-level type variables, and it will also be used in debug messages.
     *
     * @param parent the parent type in which our operations are located
     */
    public GenericContext(ReifiedType.@NonNull Annotated parent) {
        this.parent = parent;
        TypeVariable<?>[] typeVars = parent.rawType().getTypeParameters();
        if (typeVars.length != parent.argumentCount()) {
            throw new IllegalStateException("Malformed input type. Wrong number of arguments on " + parent);
        }
        this.typeVars = typeVars;
    }

    private ReifiedType.Annotated getTypeArgument(String varName) {
        for (int n = 0; n < typeVars.length; n++) {
            if (typeVars[n].getName().equals(varName)) {
                return parent.argumentAt(n);
            }
        }
        throw new IllegalStateException(
                "No known argument for type variable " + varName + " within a method of " + parent
        );
    }

    /**
     * Reifies the given annotated type.
     * <p>
     * This function assumes the given type is taken from a member within the parent type. If that is not the case,
     * behavior is not defined.
     *
     * @param type the annotated type to reify
     * @return the reified result
     */
    public ReifiedType.Annotated reifyAnnotated(AnnotatedType type) {
        if (type instanceof AnnotatedParameterizedType) {
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) type;

            AnnotatedType[] sourceArgs = parameterizedType.getAnnotatedActualTypeArguments();
            ReifiedType.Annotated[] reifiedArgs = new ReifiedType.Annotated[sourceArgs.length];
            for (int n = 0; n < sourceArgs.length; n++) {
                reifiedArgs[n] = reifyAnnotated(sourceArgs[n]);
            }
            Class<?> mainType = (Class<?>) ((ParameterizedType) parameterizedType.getType()).getRawType();
            return new ReifiedType.Annotated(mainType, reifiedArgs, type);
        }
        if (type instanceof AnnotatedTypeVariable) {
            return getTypeArgument(((TypeVariable<?>) type.getType()).getName());
        }
        if (type instanceof AnnotatedWildcardType) {
            AnnotatedWildcardType wildcardType = (AnnotatedWildcardType) type;

            return reifyAnnotated(wildcardType.getAnnotatedUpperBounds()[0]);
        }
        if (type instanceof AnnotatedArrayType) {
            AnnotatedArrayType arrayType = (AnnotatedArrayType) type;

            // Use the component type as the source of annotations - discard annotations on the array
            AnnotatedType annotatedComponent = arrayType.getAnnotatedGenericComponentType();
            ReifiedType.Annotated reifiedComponent = reifyAnnotated(annotatedComponent);
            return new ReifiedType.Annotated(
                    ArrayType.arrayType(reifiedComponent.rawType()),
                    reifiedComponent.arguments(),
                    annotatedComponent
            );
        }
        Type rawType = type.getType();
        if (rawType instanceof Class) {
            return new ReifiedType.Annotated((Class<?>) rawType, type);
        }
        throw new IllegalStateException("Unable to reify type " + type + " within a method of " + parent);
    }

    /**
     * Reifies the given type, skipping annotations.
     * <p>
     * This function assumes the given type is taken from a member within the parent type. If that is not the case,
     * behavior is not defined.
     *
     * @param type the type to reify
     * @return the reified result
     */
    public ReifiedType reify(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Type[] sourceArgs = parameterizedType.getActualTypeArguments();
            ReifiedType[] reifiedArgs = new ReifiedType[sourceArgs.length];
            for (int n = 0; n < sourceArgs.length; n++) {
                reifiedArgs[n] = reify(sourceArgs[n]);
            }
            Class<?> mainType = (Class<?>) parameterizedType.getRawType();
            return new ReifiedType(mainType, reifiedArgs);
        }
        if (type instanceof TypeVariable) {
            return getTypeArgument(((TypeVariable<?>) type).getName());
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;

            return reify(wildcardType.getUpperBounds()[0]);
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;

            ReifiedType reifiedComponent = reify(arrayType.getGenericComponentType());
            return new ReifiedType(ArrayType.arrayType(reifiedComponent.rawType()), reifiedComponent.arguments());
        }
        if (type instanceof Class) {
            return new ReifiedType((Class<?>) type);
        }
        throw new IllegalStateException("Unable to reify type " + type + " within a method of " + parent);
    }
}
