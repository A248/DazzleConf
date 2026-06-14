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

package space.arim.dazzleconf.engine;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.reflect.MethodId;
import space.arim.dazzleconf.reflect.ReflectionProvider;

/**
 * A method that is part of the low-level configuration definition.
 * <p>
 * The particular kind of node is represented by subtypes of this interface. For example, {@link Value} is the typical
 * interface method based on user configuration data. More nodes (and thus more subtypes) may be added in the future.
 *
 * @param <R> the return type of the method
 * @param <B> the configuration interface type which declared the method
 */
public interface DefinedNode<R, B> {

    /**
     * The method which this node was created from.
     * <p>
     * The {@link MethodId} is guaranteed to have been obtained from the same {@link ReflectionProvider.TypeWalker} used
     * to define the layout. Because of this, it is suitable for using to call methods on a receiver object which is an
     * instance of the enclosing {@link DefinedLayout.Branch#getType()}.
     *
     * @return the method id
     */
    @NonNull MethodId methodId();

    /**
     * A value node reads and writes data, and it returns that data as a method. It is the typical configuration
     * interface method.
     *
     * @param <R> the return type of the method
     * @param <B> the configuration interface type which declared the method
     */
    interface Value<R, B> extends DefinedNode<R, B> {

        /**
         * Gets the comment on this value node, if any.
         * <p>
         * If there are no comments at all, this can return empty. It might also return empty comment data if
         * comments were specified but without any content.
         *
         * @return the comment data, which may be empty
         */
        @SideEffectFree
        @NonNull CommentData comments();

        /**
         * Gets the serializer used by this value node
         *
         * @return the serializer
         */
        @NonNull SerializeDeserialize<R> serializeDeserialize();

        /**
         * Gets the default values for this value node, if there are any
         *
         * @return the default values, or null if none
         */
        @Nullable DefaultValues<R> defaultValues();

        /**
         * The label of a value node represents its backend-independent key in the configuration data.
         * <p>
         * It is typically the method name. Converting this value through the key mapper produces the actual key used
         * to access the backend data.
         *
         * @return the label
         */
        @NonNull String label();

    }

    /**
     * A method whose interface default implementation becomes the implementation of that method.
     * <p>
     * This node corresponds to the use of {@link CallableFn} on interface methods.
     *
     * @param <R> the return type of the method
     * @param <B> the configuration interface type which declared the method
     */
    interface Callable<R, B> extends DefinedNode<R, B> {}

}
