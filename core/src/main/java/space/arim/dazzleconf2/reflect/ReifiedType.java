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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * A possibly generic type, with its arguments fully specified.
 * <p>
 * This type is a runtime stand-in for fully reified generic information. Given that Java discards generic type data
 * at runtime, this type exists to retain that data. Lastly, while this class itself contains the raw generic data, it
 * is designed for low-level usage. See {@link TypeToken} for an ergonomic high-level version.
 * <p>
 * This class should be considered sealed and not subclassed. Equality is defined for any two reified types based
 * on whether the raw type and arguments match, or annotations if {@link ReifiedType.Annotated} is used.
 * <p>
 * This class and its subtype {@code ReifiedType.Annotated} are both immutable.
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
            return ((Annotated) that).annotations.getAnnotations().length == 0;
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

    void toString(StringBuilder builder, AnnotatedElement annotations) {
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
            for (Annotation annotation : annotations.getAnnotations()) {
                builder.append('@');
                builder.append(annotation.annotationType().getName());
            }
        }
    }

    /**
     * Annotation capable extension to a reified type.
     */
    public static final class Annotated extends ReifiedType implements AnnotatedElement {

        private final AnnotatedElement annotations;

        /**
         * Creates from nonnull input arguments
         * <p>
         * The provided {@code AnnotatedElement} must be immutable; it should return consistent results for consistent
         * calls.
         *
         * @param rawType the raw type
         * @param arguments, which are copied to ensure immutability
         * @param annotations the annotations source, which is trusted as immutable
         */
        public Annotated(@NonNull Class<?> rawType, ReifiedType.@NonNull Annotated @NonNull [] arguments,
                         @NonNull AnnotatedElement annotations) {
            super(rawType, arguments);
            this.annotations = Objects.requireNonNull(annotations, "annotations");
        }

        /**
         * Creates from nonnull input arguments
         * <p>
         * The provided {@code AnnotatedElement} must be immutable; it should return consistent results for consistent
         * calls.
         *
         * @param rawType the raw type
         * @param annotations the annotations source, which is trusted as immutable
         */
        public Annotated(@NonNull Class<?> rawType, @NonNull AnnotatedElement annotations) {
            super(rawType);
            this.annotations = Objects.requireNonNull(annotations, "annotations");
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
                // Check for equality of annotations, but ignore declaration order
                Annotation[] ours = annotations.getAnnotations();
                Annotation[] theirs = thatAnnotated.annotations.getAnnotations();
                if (ours.length != theirs.length) {
                    return false;
                }
                for (Annotation ourAnnote : ours) {
                    Annotation theirAnnote = thatAnnotated.getAnnotation(ourAnnote.annotationType());
                    if (!ourAnnote.equals(theirAnnote)) {
                        return false;
                    }
                }
                return true;
            }
            return annotations.getAnnotations().length == 0;
        }

        @Override
        void toString(StringBuilder builder) {
            toString(builder, annotations);
        }

        @Override
        public boolean isAnnotationPresent(@NonNull Class<? extends Annotation> annotationClass) {
            return annotations.isAnnotationPresent(annotationClass);
        }

        @Override
        public <T extends Annotation> T getAnnotation(@NonNull Class<T> annotationClass) {
            return annotations.getAnnotation(annotationClass);
        }

        @Override
        public Annotation @NonNull [] getAnnotations() {
            return annotations.getAnnotations();
        }

        @Override
        public <T extends Annotation> T @NonNull [] getAnnotationsByType(@NonNull Class<T> annotationClass) {
            return annotations.getAnnotationsByType(annotationClass);
        }

        @Override
        public <T extends Annotation> T getDeclaredAnnotation(@NonNull Class<T> annotationClass) {
            return annotations.getDeclaredAnnotation(annotationClass);
        }

        @Override
        public <T extends Annotation> T @NonNull [] getDeclaredAnnotationsByType(@NonNull Class<T> annotationClass) {
            return annotations.getDeclaredAnnotationsByType(annotationClass);
        }

        @Override
        public Annotation @NonNull [] getDeclaredAnnotations() {
            return annotations.getDeclaredAnnotations();
        }
    }
}
