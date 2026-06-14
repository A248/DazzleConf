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

package space.arim.dazzleconf.reflect;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.DeveloperMistakeException;

/**
 * Bank of values yielded when calling an instantiated proxy.
 * <p>
 * A {@code MethodYield} can only be used with the reflection provider that produced it. Its lifetime exists from the
 * moment of production to when it is closed or consumed by {@link ReflectionProvider#generate(MethodYield)}. It
 * is mutable, not thread safe, and not designed to be long-lasting. However, if multiple copies are needed, see
 * {@link #copy()}.
 */
public interface MethodYield extends AutoCloseable {

    /**
     * Gets the subset of this method yield that can add values for the given interface.
     * <p>
     * When the returned {@code ForImplementable} is done being used, it must be closed. Only one
     * {@code ForImplementable} per implementor class can exist at any given time. However, after closing it, this
     * method can be called again with the same implementor class.
     *
     * @param implementable the interface being implemented
     * @return a handler to attach instructions for it
     * @throws IllegalStateException if the {@code implementable} already has a {@code ForImplementable} open for it,
     * or if this {@code MethodYield} is already consumed. This exception can also be thrown by methods on the returned
     * object if these conditions are realized later.
     */
    @NonNull ForImplementable forImplementable(@NonNull Class<?> implementable);

    /**
     * Builder of return value instructions within an implementable type.
     * <p>
     * Each function on this interface allows adding instructions for methods that exist within the implementable type.
     * Using methods from other places or implementations can cause {@link DeveloperMistakeException} to be thrown.
     * <p>
     * A target method can have an instruction added only once. After adding one or more instructions, this
     * {@code ForImplementable} must be closed.
     */
    interface ForImplementable extends AutoCloseable {

        /**
         * Specifies to return a fixed value. If the given method already has an instruction, it is replaced.
         *
         * @param methodId a method within the type
         * @param value the value to return, or the boxed value if primitive
         * @throws IllegalStateException if the given method already has an instruction
         */
        void returnValue(@NonNull MethodId methodId, @NonNull Object value);

        /**
         * Specifies to invoke the default implementation.
         *
         * @param methodId        a method within the type
         * @throws IllegalArgumentException if the method is not default
         * @throws IllegalStateException if the given method already has an instruction
         */
        void callDefault(@NonNull MethodId methodId);

        /**
         * Closes this {@code ForImplementable}.
         *
         */
        @Override
        void close();
    }

    /**
     * Clears all added values and starts over again
     *
     * @throws IllegalStateException if this {@code MethodYield} is already consumed
     */
    void clear();

    /**
     * Returns a deep copy of this method yield's contents.
     * <p>
     * Mutating this {@code MethodYield}, such as by adding or clearing values, will not affect the copy; likewise
     * mutating the copy will not affect this instance.
     *
     * @return a copy of this method yield's contents
     * @throws IllegalStateException if this {@code MethodYield} is already consumed
     */
    @NonNull MethodYield copy();

    /**
     * Closes this method yield
     *
     */
    @Override
    void close();

}
