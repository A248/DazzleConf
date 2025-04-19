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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Objects;

/**
 * A possibly generic type, with its arguments fully specified.
 * <p>
 * An equality contract exists for all ReifiedTypes, including subtypes like {@link ReifiedType.Annotated}, based on
 * the raw type and reified arguments.
 *
 */
public class ReifiedType {

    private final Class<?> rawType;
    private final ReifiedType[] arguments;

    static final ReifiedType[] EMPTY_ARR = new ReifiedType[] {};

    /**
     * Builds from nonnull input arguments.
     *
     * @param rawType the raw type
     * @param arguments the arguments, which are copied to ensure immutability
     */
    public ReifiedType(Class<?> rawType, ReifiedType[] arguments) {
        this.rawType = Objects.requireNonNull(rawType, "rawType");
        this.arguments = arguments.clone();
    }

    /**
     * Builds from simple raw type
     *
     * @param rawType the raw type
     */
    public ReifiedType(Class<?> rawType) {
        this.rawType = Objects.requireNonNull(rawType, "rawType");
        this.arguments = EMPTY_ARR;
    }

    /**
     * Gets the raw type, unparameterized
     *
     * @return the raw type
     */
    public Class<?> rawType() {
        return rawType;
    }

    /**
     * Gets the argument at a certain index
     *
     * @param index the index
     * @return the argument at it
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public ReifiedType argumentAt(int index) {
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
    public ReifiedType[] arguments() {
        return arguments.clone();
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ReifiedType)) return false;

        ReifiedType that = (ReifiedType) o;
        return rawType.equals(that.rawType) && Arrays.equals(arguments, that.arguments);
    }

    @Override
    public final int hashCode() {
        int result = rawType.hashCode();
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    @Override
    public String toString() {
        return "ReifiedType{" +
                "rawType=" + rawType +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
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
        public Annotated(Class<?> rawType, ReifiedType.Annotated[] arguments, AnnotatedElement annotations) {
            super(rawType, arguments);
            this.annotations = annotations.getAnnotations();
        }

        /**
         * Creates from nonnull input arguments
         *
         * @param rawType the raw type
         * @param annotations the annotations source
         */
        public Annotated(Class<?> rawType, AnnotatedElement annotations) {
            super(rawType);
            this.annotations = annotations.getAnnotations();
        }

        /**
         * Gets an annotation if it is set. If multiple are found, the first is returned.
         * @param annotationClass the annotation class
         * @return the annotation if present, or null if not found
         * @param <A> the annotation type
         */
        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
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
        public ReifiedType.Annotated argumentAt(int index) {
            return (ReifiedType.Annotated) super.argumentAt(index);
        }

        /**
         * Gets all the arguments
         *
         * @return a copy of the arguments
         */
        @Override
        public ReifiedType.Annotated[] arguments() {
            return (ReifiedType.Annotated[]) super.arguments();
        }

        @Override
        public String toString() {
            return "ReifiedType.Annotated{" +
                    "rawType=" + rawType() +
                    ", arguments=" + Arrays.toString(arguments()) +
                    ", annotations=" + Arrays.toString(annotations) +
                    '}';
        }
    }
}
