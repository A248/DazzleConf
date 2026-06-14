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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;
/**
 * A generic aware type, with its arguments fully specified.
 * <p>
 * This type is a runtime stand-in for fully reified generic information. Given that Java discards generic type data
 * at runtime, this type exists to retain that data. Lastly, while this class itself contains the raw generic data, it
 * is designed for low-level usage. See {@link TypeToken} for an ergonomic high-level version.
 * <p>
 * This class is immutable.
 */
public class ReifiedType {

    private final @NonNull Class<?> rawType;
    private final @NonNull ReifiedType @NonNull [] arguments;
    private final @NonNull ReifiedAnnotations annotations;

    static final ReifiedType[] EMPTY_ARRAY = new ReifiedType[0];

    ReifiedType(@NonNull Class<?> rawType, @NonNull ReifiedType @NonNull [] arguments,
                @NonNull ReifiedAnnotations annotations) {
        this.rawType = Objects.requireNonNull(rawType, "rawType");
        this.arguments = Objects.requireNonNull(arguments, "arguments");
        this.annotations = Objects.requireNonNull(annotations, "annotations");
    }

    /**
     * Gets the raw type, unparameterized
     *
     * @return the raw type
     */
    public @NonNull Class<?> rawType() {
        return rawType;
    }

    /**
     * Gets the argument at a certain index
     *
     * @param index the index
     * @return the argument at it
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public @NonNull ReifiedType argumentAt(int index) {
        return arguments[index];
    }

    /**
     * The argument count
     * @return the argument count
     */
    public int argumentCount() {
        return arguments.length;
    }

    /**
     * Gets all the arguments
     *
     * @return a copy of the arguments
     */
    public @NonNull ReifiedType @NonNull [] arguments() {
        return arguments.clone();
    }

    /**
     * Gets the annotations on this type
     *
     * @return the annotations
     */
    public @NonNull ReifiedAnnotations annotations() {
        return annotations;
    }

    @Override
    public final boolean equals(@Nullable Object o) {
        if (!(o instanceof ReifiedType)) return false;

        ReifiedType that = (ReifiedType) o;
        return rawType.equals(that.rawType) && Arrays.equals(arguments, that.arguments) &&
                annotations.equals(that.annotations);
    }

    @Override
    public final int hashCode() {
        int result = rawType.hashCode();
        result = 31 * result + Arrays.hashCode(arguments);
        result = 31 * result + annotations.hashCode();
        return result;
    }

    @Override
    public final @NonNull String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }

    void toString(StringBuilder builder) {
        annotations.forEachKnownAnnote(annotation -> {
            builder.append('@');
            builder.append(annotation.annotationType().getName());
            builder.append(' ');
        });
        builder.append(rawType().getName());
        if (arguments.length != 0) {
            builder.append('<');
            for (int n = 0; n < arguments.length; n++) {
                if (n != 0) {
                    builder.append(',');
                }
                arguments[n].toString(builder);
            }
            builder.append('>');
        }
    }

    /**
     * Creates with the following values.
     * <p>
     * The number of arguments of the type must be appropriate to the type itself. For example, it is impossible to
     * parameterize a {@code java.util.Map} with only one type argument. This factory function checks for type argument
     * compatibility as follows:
     * <ul>
     *     <li>The number of type arguments must match the number of type variables on {@code rawType}</li>
     *     <li>The raw types of the type arguments must extend the erased bounds of the type variables on {@code rawType}</li>
     *     <li>If the number of type arguments is {@code 0}, the previous two checks are skipped and the returned value
     *     represents a raw type.</li>
     * </ul>
     * If the above conditions are not satisfied, throws {@code IllegalArgumentException}.
     *
     * @param rawType the main type
     * @param annotations the annotations on the type
     * @param arguments its generic arguments
     * @return the reified type
     * @throws IllegalArgumentException if the reified arguments are not empty, but they are incompatible with the
     * number or bounds of the type variables on {@code rawType}
     */
    public static @NonNull ReifiedType of(@NonNull Class<?> rawType, @NonNull ReifiedAnnotations annotations,
                                          @NonNull ReifiedType @NonNull ... arguments) {
        if (arguments.length == 0) {
            arguments = EMPTY_ARRAY;
        } else {
            /*
            Preconditions:
            1. Arguments are non-null and array non-null
            2. They are compatible with the type variables in length
            3. They are compatible with the type variables in erased bounds
            4. Make sure to prevent TOCTOU attacks
             */
            TypeVariable<?>[] typeVariables = rawType.getTypeParameters();
            if (arguments.length != typeVariables.length) { // 2
                throw new IllegalArgumentException(
                        "Argument number mismatch; " + rawType + " cannot be parameterized with " +
                                arguments.length + " arguments"
                );
            }
            arguments = arguments.clone(); // 4
            for (int n = 0; n < arguments.length; n++) {
                Class<?> rawArg = arguments[n].rawType; // 1
                Class<?> bound = GenericCompute.eraseType(typeVariables[n]);
                if (!bound.isAssignableFrom(rawArg)) { // 3
                    throw new IllegalArgumentException(
                            "Argument bound mismatch; argument " + n + " (" + rawArg.getName() + ") on " + rawType +
                                    " must satisfy the type variable bound of " + bound.getName()
                    );
                }
            }
        }
        return new ReifiedType(rawType, arguments, annotations);
    }

    /**
     * Creates an unparameterized, unannotated reified type for the following class.
     *
     * @param rawType the raw type
     * @return a reified type with no annotations and no generic arguments (if relevant) for the provided raw type
     */
    public static @NonNull ReifiedType rawUnannotated(@NonNull Class<?> rawType) {
        return new ReifiedType(rawType, EMPTY_ARRAY, ReifiedAnnotations.empty());
    }

    /**
     * Extracts a reified type based on its type variable context of usage.
     *
     * @param annotatedType the annotated type from the JDK API
     * @param genericContext the generic context of type variables
     * @return the reified type
     * @throws IllegalStateException if the {@link java.lang.reflect.AnnotatedType} subclass is not recognized
     * @throws RuntimeException any exceptions thrown by the {@link GenericContext} are propagated here
     */
    public static @NonNull ReifiedType computeFrom(@NonNull AnnotatedType annotatedType,
                                                   @NonNull GenericContext genericContext) {
        Objects.requireNonNull(genericContext, "genericContext");
        Objects.requireNonNull(annotatedType, "annotatedType");
        return new GenericCompute(genericContext).reify(annotatedType);
    }
}
