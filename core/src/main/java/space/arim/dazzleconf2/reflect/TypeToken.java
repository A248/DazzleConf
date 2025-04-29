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
import space.arim.dazzleconf2.DeveloperMistakeException;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Token for handling configuration types at runtime. Usage follows the common pattern for TypeToken in other
 * libraries: instantiate an anonymous subclass, fully specifying the generic type.
 *
 * @param <V> the type
 */
public class TypeToken<V> {

    private final ReifiedType.Annotated reifiedType;

    /**
     * Creates from a reified type.
     * <p>
     * <b>Caller ensures that the passed parameter is compatible with the source level usage of this type token.</b>
     *
     * @param reifiedType a reified type
     */
    public TypeToken(ReifiedType.@NonNull Annotated reifiedType) {
        this.reifiedType = Objects.requireNonNull(reifiedType);
    }

    /**
     * Creates from a subclass.
     * <p>
     * The subclass must fully specify concrete generic arguments. The most common approach is to create a simple
     * anonymous subclass using double brackets.
     * <p>
     * <b>Example usage</b>
     * <pre>
     * {@code
     * TypeToken<GenericConfig<String>> token = new TypeToken<GenericConfig<String>>() {};
     * }
     * </pre>
     */
    protected TypeToken() {
        this.reifiedType = buildReified(extractTypeFromSubclass());
    }

    /**
     * Gets the raw type as a class object
     *
     * @return the raw type
     */
    public final @NonNull Class<V> getRawType() {
        @SuppressWarnings("unchecked")
        Class<V> rawType = (Class<V>) reifiedType.rawType();
        return rawType;
    }

    /**
     * Gets the annotated reified type represented by this type token
     * @return the annotated reified type
     */
    public final ReifiedType.@NonNull Annotated getReifiedType() {
        return reifiedType;
    }

    private AnnotatedType extractTypeFromSubclass() {
        AnnotatedType superClassType = getClass().getAnnotatedSuperclass();
        if (!(superClassType instanceof AnnotatedParameterizedType)) {
            throw new DeveloperMistakeException("Invalid TypeToken. Must subclass and specify generic arguments.");
        }
        return ((AnnotatedParameterizedType) superClassType).getAnnotatedActualTypeArguments()[0];
    }

    private static ReifiedType.@NonNull Annotated buildReified(AnnotatedType source) {

        if (source instanceof AnnotatedParameterizedType) {
            AnnotatedParameterizedType parameterizedSource = (AnnotatedParameterizedType) source;

            Class<?> rawType = (Class<?>) ((ParameterizedType) parameterizedSource.getType()).getRawType();
            AnnotatedType[] genericArgs = parameterizedSource.getAnnotatedActualTypeArguments();
            ReifiedType.Annotated[] reifiedArgs = new ReifiedType.Annotated[genericArgs.length];
            for (int n = 0; n < genericArgs.length; n++) {
                reifiedArgs[n] = buildReified(genericArgs[n]);
            }
            return new ReifiedType.Annotated(rawType, reifiedArgs, source);
        }
        Type unannotated = source.getType();
        if (unannotated instanceof Class) {
            return new ReifiedType.Annotated((Class<?>) unannotated, source);
        }
        throw new DeveloperMistakeException("Invalid TypeToken. Generics must be fully reified.");
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof TypeToken)) return false;

        TypeToken<?> typeToken = (TypeToken<?>) o;
        return reifiedType.equals(typeToken.reifiedType);
    }

    @Override
    public final int hashCode() {
        return reifiedType.hashCode();
    }

    @Override
    public String toString() {
        return "TypeToken{" + reifiedType + '}';
    }
}
