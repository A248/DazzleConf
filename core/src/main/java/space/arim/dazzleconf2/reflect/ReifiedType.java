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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Objects;

/**
 * A possibly generic type, with its arguments fully specified.
 * <p>
 * This type is a runtime stand-in for fully reified generic information. Given that Java discards generic type data
 * at runtime, this type exists to retain that data. Lastly, while this class itself contains the raw generic data, it
 * is designed for low-level usage. See {@link TypeToken} for an ergonomic high-level version.
 * <p>
 * This class should be considered sealed and not subclassed. Equality is defined for any two reified types based
 * on whether the raw type and arguments match, or annotations if {@link ReifiedType.Annotated} is used.
 *
 */
// TODO Make this class sealed in a versions/17 multi-release directory
public class ReifiedType {

    private final Class<?> rawType;
    private final ReifiedType[] arguments;

    static final ReifiedType.Annotated[] EMPTY_ARR = new ReifiedType.Annotated[] {};

    /**
     * Builds from nonnull input arguments.
     *
     * @param rawType the raw type
     * @param arguments the arguments, which are copied to ensure immutability
     */
    public ReifiedType(@NonNull Class<?> rawType, @NonNull ReifiedType @NonNull [] arguments) {
        this.rawType = Objects.requireNonNull(rawType, "rawType");
        this.arguments = arguments.clone();
    }

    /**
     * Builds from simple raw type
     *
     * @param rawType the raw type
     */
    public ReifiedType(@NonNull Class<?> rawType) {
        this.rawType = Objects.requireNonNull(rawType, "rawType");
        this.arguments = EMPTY_ARR;
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

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ReifiedType)) return false;

        ReifiedType that = (ReifiedType) o;
        return rawType.equals(that.rawType) && Arrays.equals(arguments, that.arguments) && annotationsEq(that);
    }

    boolean annotationsEq(ReifiedType that) {
        if (that instanceof ReifiedType.Annotated) {
            return ((Annotated) that).annotations.length == 0;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        int result = rawType.hashCode();
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }

    void toString(StringBuilder builder) {
        toString(builder, null);
    }

    void toString(StringBuilder builder, Annotation[] annotations) {
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
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                builder.append('@');
                builder.append(annotation.annotationType().getName());
            }
        }
    }

    /**
     * Annotation capable extension to a reified type.
     */
    public static final class Annotated extends ReifiedType {

        private final Annotation[] annotations;

        /**
         * Creates from nonnull input arguments
         *
         * @param rawType the raw type
         * @param arguments, which are copied to ensure immutability
         * @param annotations the annotations source
         */
        public Annotated(@NonNull Class<?> rawType, ReifiedType.@NonNull Annotated @NonNull [] arguments,
                         @NonNull AnnotatedElement annotations) {
            super(rawType, arguments);
            this.annotations = annotations.getAnnotations();
        }

        /**
         * Creates from nonnull input arguments
         *
         * @param rawType the raw type
         * @param annotations the annotations source
         */
        public Annotated(@NonNull Class<?> rawType, @NonNull AnnotatedElement annotations) {
            super(rawType);
            this.annotations = annotations.getAnnotations();
        }

        /**
         * Gets a type level annotation if it is set. If multiple are found, the first is returned.
         * @param annotationClass the annotation class
         * @return the annotation if present, or null if not found
         * @param <A> the annotation type
         */
        public <A extends Annotation> @Nullable A getAnnotation(@NonNull Class<A> annotationClass) {
            for (Annotation check : annotations) {
                if (annotationClass.equals(check.annotationType())) {
                    return annotationClass.cast(check);
                }
            }
            return null;
        }

        /**
         * Gets the argument at a certain index
         *
         * @param index the index
         * @return the argument at it
         * @throws IndexOutOfBoundsException if the index is out of range
         */
        @Override
        public ReifiedType.@NonNull Annotated argumentAt(int index) {
            return (ReifiedType.Annotated) super.argumentAt(index);
        }

        /**
         * Gets all the arguments
         *
         * @return a copy of the arguments
         */
        @Override
        public ReifiedType.@NonNull Annotated @NonNull [] arguments() {
            return (Annotated[]) super.arguments();
        }

        @Override
        boolean annotationsEq(ReifiedType that) {
            if (that instanceof Annotated) {
                Annotated thatAnnotated = (Annotated) that;
                if (annotations.length != thatAnnotated.annotations.length) {
                    return false;
                }
                for (int n = 0; n < annotations.length; n++) {
                    if (!annotations[n].equals(thatAnnotated.annotations[n])) {
                        return false;
                    }
                }
                return true;
            }
            return annotations.length == 0;
        }

        @Override
        void toString(StringBuilder builder) {
            toString(builder, annotations);
        }
    }
}
