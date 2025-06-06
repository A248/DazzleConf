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
import org.checkerframework.checker.nullness.qual.PolyNull;
import space.arim.dazzleconf2.DeveloperMistakeException;

import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.Objects;

/**
 * Token for handling configuration-related types at runtime.
 * <p>
 * A {@code TypeToken} stores the annotated, reified type data for a single written type. It represents this
 * information at the source level as the type parameter {@code V}, and at runtime via {@link #getReifiedType()}.
 * <p>
 * <b>Creation</b>
 * <p>
 * Construction of this type follows the common pattern seen in other libraries. The caller should instantiate an
 * anonymous subclass, fully specifying the generic type. For example:
 * <pre>
 *     {@code
 *     TypeToken<GenericConfig<String>> token = new TypeToken<GenericConfig<String>>() {};
 *     }
 * </pre>
 * The type data will be extracted thanks to the use of the anonymous subclass, which retains the declaration of its
 * superclass (and therefore the concrete generic parameter) at runtime. As one would expect, type variables are not
 * permitted in the type declaration, as they defeat the very purpose of using {@code TypeToken} (which is a runtime,
 * not source, variable) to begin with.
 * <p>
 * <b>Keying and Usage</b>
 * <p>
 * This class doubles as an immutable store of a single {@link ReifiedType.Annotated}. This class represents that type
 * and can be thought of as a higher-level wrapper for it. It contains that same information at runtime, while also
 * adding the source-level variable {@code V}.
 * <p>
 * Thus, in addition to extracting information upon subclass construction, this class can be used like any other POJO.
 * Callers can use the trusted constructor {@link TypeToken#TypeToken(ReifiedType.Annotated)}, and they are expected to
 * choose an appropriate type parameter based on their usage of this class.
 * <p>
 * Equality and hash code are defined based on the {@code ReifiedType.Annotated} this token represents. Subclases
 * cannot override this behavior, which makes {@code TypeToken} safely consumable by various parts of the library and
 * its callers.
 *
 * @param <V> the type
 */
public class TypeToken<V> {

    private final ReifiedType.@NonNull Annotated reifiedType;

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
     *
     * @throws DeveloperMistakeException if type variables were used in the type declaration
     */
    protected TypeToken() {
        AnnotatedType typeFromSubclass = extractTypeFromSubclass();
        this.reifiedType = new GenericContext(ReifiedType.Annotated.unannotated(Object.class)) {

            @Override
            ReifiedType.Annotated unknownVariable(String varName) {
                throw new DeveloperMistakeException(
                        "Type variables are rejected in TypeToken construction. Found variable '" + varName + '\''
                );
            }
        }.reify(typeFromSubclass);
    }

    /**
     * Casts the argument to this type.
     * <p>
     * <b>Generic types</b>: Note that only the raw type ({@link #getRawType()}) can be checked at runtime, and not
     * generic arguments.
     * <p>
     * <b>Primitive types</b>: This method accepts {@code obj} as a boxed argument if this type token represents a
     * primitive type.
     *
     * @param obj the object
     * @return the cast value
     * @throws ClassCastException if the obj is not null and not assignable to this type
     */
    public final @PolyNull V cast(@PolyNull Object obj) {
        Class<V> rawType = getRawType();
        if (rawType.isPrimitive()) {
            @SuppressWarnings("unchecked")
            Class<V> wrapperType = (Class<V>) MethodType.methodType(rawType).wrap().returnType();
            return wrapperType.cast(obj);
        }
        return rawType.cast(obj);
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

    private @NonNull AnnotatedType extractTypeFromSubclass() {
        AnnotatedType superClassType = getClass().getAnnotatedSuperclass();
        if (!(superClassType instanceof AnnotatedParameterizedType)) {
            throw new DeveloperMistakeException("Invalid TypeToken. Must subclass and specify generic arguments.");
        }
        return ((AnnotatedParameterizedType) superClassType).getAnnotatedActualTypeArguments()[0];
    }

    /**
     * A type token is equal to another when it points to the same reified type (at runtime)
     *
     * @param o the other object
     * @return true if equal
     */
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
    public final String toString() {
        return "TypeToken" + '{' + reifiedType + '}';
    }
}
