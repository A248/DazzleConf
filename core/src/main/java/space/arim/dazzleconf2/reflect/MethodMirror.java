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
import space.arim.dazzleconf2.DeveloperMistakeException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

/**
 * Reflective access API for getting and invoking methods on a type.
 *
 */
public interface MethodMirror {

    /**
     * Gets all accessible, non-inherited, instance methods declared in the given class.
     * <p>
     * However the returned stream is ordered, that order will later determine the order of serializing data entries.
     * There is no need for the implementation to skip source-level covariant overides, since these will be handled
     * by the caller, but bridge and synthetic methods do need to be skipped.
     * <p>
     * Generic values must be fully reified. The passed type information provides the reified type arguments that can
     * help with this.
     *
     * @param reifiedType the reified type
     * @return a stream of methods
     * @throws IllegalStateException optionally, if the <code>ReifiedType</code> is malformed and does not represent
     * a valid type
     * @throws DeveloperMistakeException if method-level generic parameters are declared
     */
    @NonNull Stream<@NonNull MethodId> getViableMethods(ReifiedType.@NonNull Annotated reifiedType);

    /**
     * Gets an annotation present on the specified method.
     * <p>
     * The method must be taken from {@link #getViableMethods(ReifiedType.Annotated)}, meaning it must be accessible
     * and the enclosing type parameter must match the <code>ReifiedType</code> passed there. This function is
     * permitted to assume this precondition and may behave unexpectedly if it is not met.
     *
     * @param methodId the method
     * @param enclosingType the type the method is taken from
     * @param annotation the annotation
     * @return the annotation, or null if it is not set
     * @param <A> the annotation type
     */
    <A extends Annotation> @Nullable A getAnnotation(@NonNull MethodId methodId, @NonNull ReifiedType enclosingType,
                                                     @NonNull Class<A> annotation);

    /**
     * Creates an invoker that allows calling methods on the given receiver.
     * <p>
     * Usage of the invoker is restricted to methods declared in the given enclosing type. The caller upholds the
     * guarantee that no other methods (such as inherited methods) will be called.
     *
     * @param enclosingType the type method is taken from, as a raw class
     * @param receiver the receiver object, on which the methods are called
     * @return an invoker for the receiver
     */
    @NonNull Invoker makeInvoker(@NonNull Object receiver, @NonNull Class<?> enclosingType);

    /**
     * A wrapper for an instance whose methods can be called, as long as those methods conform to a certain
     * enclosing type.
     *
     */
    interface Invoker {

        /**
         * The enclosing type passed to {@link #makeInvoker(Object, Class)}. Method invocations are restricted to
         * methods declaring in this type
         *
         * @return the enclosing type
         */
        @NonNull Class<?> enclosingType();

        /**
         * Invokes a method and gets the return value.
         * <p>
         * The method must be taken from {@link MethodMirror#getViableMethods(ReifiedType.Annotated)}, meaning it
         * must be accessible and have the enclosing type ({@link #enclosingType()}) match the <code>ReifiedType</code>
         * passed there. This function is permitted to assume this precondition and may behave unexpectedly otherwise.
         *
         * @param methodId the method to call
         * @param arguments method arguments. The array may be <code>null</code> for none, and the arguments may be boxed
         *                  primitive objects to represent primitive values.
         * @return the return value, boxing primitives if necessary
         * @throws InvocationTargetException if the method threw a throwable, it is wrapped in this exception
         */
        @Nullable Object invokeMethod(@NonNull MethodId methodId, @Nullable Object @Nullable ...arguments)
                throws InvocationTargetException;

    }
}
