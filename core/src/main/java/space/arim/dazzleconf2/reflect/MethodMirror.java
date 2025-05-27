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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

/**
 * Reflective access API for getting and invoking methods on a type.
 *
 */
public interface MethodMirror {

    /**
     * Starts making a type walker for the given type.
     * <p>
     * The type walker allows the caller to control movement and selection of super classes insofar as the control flow
     * suits them.
     *
     * @param reifiedType the reified type being walked
     * @return the type walked
     */
    @NonNull TypeWalker typeWalker(ReifiedType.@NonNull Annotated reifiedType);

    /**
     * Controller for accessing the reflected methods of a given type
     *
     */
    interface TypeWalker {

        /**
         * Gets the type which this {@code TypeWalker} is looking at
         *
         * @return the type
         */
        ReifiedType.@NonNull Annotated getEnclosingType();

        /**
         * Gets all accessible, non-inherited, instance methods declared in this type.
         * <p>
         * However the returned stream is ordered, that order will later determine the order of serializing data entries.
         * There is no need for the implementation to skip source-level covariant overides, since these will be handled
         * by the caller, but bridge and synthetic methods do need to be skipped.
         * <p>
         * Generic values must be fully reified. The passed type information provides the reified type arguments that can
         * help with this.
         *
         * @return a stream of methods
         * @throws IllegalStateException optionally, if the <code>ReifiedType</code> is malformed and does not represent
         * a valid type
         */
        @NonNull Stream<@NonNull MethodId> getViableMethods();

        /**
         * Gets annotations present on the specified method.
         * <p>
         * The method must be taken from {@link #getViableMethods()}, meaning it must be accessible and located in
         * the type this {@code TypeWalker} is made for. This function is permitted to assume this precondition and
         * may behave unexpectedly if it is not met.
         *
         * @param methodId the method
         * @return the annotations on this method
         */
        @NonNull AnnotatedElement getAnnotations(@NonNull MethodId methodId);

        /**
         * Moves to the directly declared super types of this type
         *
         * @return the super types
         */
        @NonNull TypeWalker @NonNull [] getSuperTypes();

    }

    /**
     * Creates an invoker that allows calling methods on the given receiver.
     * <p>
     * Usage of the invoker is restricted to methods declared in the given enclosing type. The caller upholds the
     * guarantee that no other methods (such as inherited methods) will be called.
     *
     * @param receiver the receiver object, on which the methods are called
     * @param enclosingType the enclosing type
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
         * Invokes a method and gets the return value.
         * <p>
         * The method must be taken from {@link TypeWalker#getViableMethods()}, meaning it must be accessible and located in
         * the type this {@code TypeWalker} is made for. This function is permitted to assume this precondition and
         * may behave unexpectedly if it is not met.
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
