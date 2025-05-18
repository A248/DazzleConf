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

package space.arim.dazzleconf2;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.reflect.MethodId;
import space.arim.dazzleconf2.reflect.TypeToken;

/**
 * Provides the minimal methods for reading and writing configurations from data trees
 *
 * @param <C> the configuration type
 */
public interface ConfigurationDefinition<C> {

    /**
     * Gets the reified type of the configuration interface
     *
     * @return a type token for the config interface
     */
    @NonNull TypeToken<C> getType();

    /**
     * Loads the default configuration.
     * <p>
     * This will build a configuration object using only the default-providing mechanisms (i.e. annotations and
     * default methods).
     *
     * @return a configuration using wholly default values
     * @throws DeveloperMistakeException if one of the default-providing methods threw an exception, or gave null
     */
    @NonNull C loadDefaults();

    /**
     * A simple, stateless read from a data tree.
     * <p>
     * This function loads from the data tree without modifying it, and it does not use migrations. The configuration
     * is instantiated and returned upon success.
     *
     * @param dataTree the data tree to read from
     * @param readOptions full parameters to customize the operation
     * @return the loaded configuration, or an error if failed
     */
    @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull ReadOptions readOptions);

    /**
     * Reads from the data tree given, and updates it as necessary.
     * <p>
     * This function loads from the data tree, and it does not use migrations. However, if any entries need updating
     * (as determined by {@link SerializeDeserialize#deserializeUpdate(DeserializeInput, SerializeOutput)} then the
     * objects will be updated in the data tree. Callers can check whether any updates ocurred by using a load listener
     * in the read options.
     *
     * @param dataTree the data tree to read and update
     * @param readOptions full parameters to customize the operation
     * @return the loaded configuration, or an error if failed
     */
    @NonNull LoadResult<@NonNull C> readWithUpdate(DataTree.@NonNull Mut dataTree, @NonNull ReadOptions readOptions);

    /**
     * Writes to the given data tree.
     * <p>
     * The output data tree does not need to be empty, but there are no guarantees that existing data will not be
     * overidden or cleared. The values of the provided configuration are written to it, and it does not matter
     * how the {@code config} parameter is implemented so long as it returns non-null values.
     *
     * @param config the configuration
     * @param dataTree the data tree to write to
     * @param writeOptions full parameters to customize the operation
     */
    void writeTo(@NonNull C config, DataTree.@NonNull Mut dataTree, @NonNull WriteOptions writeOptions);

    /**
     * Parameters for reading a configuration from a tree
     *
     */
    interface ReadOptions {

        /**
         * Listener which informs the caller if certain events happened
         *
         * @return the load listener
         */
        @NonNull LoadListener loadListener();

        /**
         * The key mapper to use
         *
         * @return the key mapper, nonnull
         */
        @NonNull
        KeyMapper keyMapper();

        /**
         * The maximum number of errors to collect before exiting. Must be greater than 0.
         * <p>
         * If reading the configuration failed, the size of {@link LoadResult#getErrorContexts()} will be at most this
         * number.
         *
         * @return the maximum number of errors to collect, default 12. Must be greater than 0
         */
        default int maximumErrorCollect() {
            return ReadOpts.DEFAULT_MAX_ERROR_TO_COLLECT;
        }

    }

    /**
     * Parameters for writing a configuration to a tree
     *
     */
    interface WriteOptions {

        /**
         * The key mapper to use
         *
         * @return the key mapper, nonnull
         */
        @NonNull KeyMapper keyMapper();
    }

    /**
     * Walks the configuration definition using the supplied visit.
     * <p>
     * This is a low-level function mainly used within the library itself. It will traverse all elements of the
     * configuration interface, calling the visitor appropriately for each item.
     *
     * @param visit the visitor preparation
     * @param walkOptions the walk options
     */
    void walkDefinition(Visit<C> visit, WalkOptions walkOptions);

    /**
     * Parameters for traversing a configuration definition
     *
     */
    interface WalkOptions {

        /**
         * Listener which informs the caller if certain events happened
         *
         * @return the load listener
         */
        @NonNull LoadListener loadListener();

        /**
         * The key mapper to use
         *
         * @return the key mapper, nonnull
         */
        @NonNull
        KeyMapper keyMapper();

    }

    /**
     * An interface, implemented by callers, to define visiting operations for parts of a configuration definition
     *
     * @param <C> the configuration type
     */
    interface Visit<C> {

        /**
         * Looks at a type in the hierarchy of this configuration.
         * <p>
         * The type passed, represented by {@code S}, is guaranteed to be a super type of {@code C}. Due to Java
         * language constraints, there is no way to express this at compile time.
         *
         * @param superType the raw super type
         * @return an element visitor for the elements of that type
         * @param <S> the super type
         */
        <S> ElementVisitor<C, S> visitTypeInHierarchy(Class<S> superType);

        /**
         * A visitor for the type-level elements of a configuration interface.
         *
         * @param <C> the configuration type from the original {@link #walkDefinition(Visit, WalkOptions)}
         * @param <S> the particular super type currently being visited in {@link #visitTypeInHierarchy(Class)}
         */
        interface ElementVisitor<C, S> {

            /**
             * Visits a transparent item.
             * <p>
             * This is used for every method annotated with {@link CallableFn}. Calling such a method on a configuration
             * object will use the implementation in the interface definition.
             *
             * @param methodTransparent the method annotated with {@code CallableFn}
             * @param <R> the return value of the method
             */
            <R> void visitTransparent(MethodTransparent<C, S, R> methodTransparent);

            /**
             * Visits a configuration option.
             * <p>
             * Each option is represented by a method node, which is a high-level wrapper that allows the caller to
             * serialize or deserialize values, load default values, etc.
             *
             * @param <R> the return value of the method node
             * @param methodNode the method node
             */
            <R> void visitNode(MethodNode<C, S, R> methodNode);

        }

        /**
         * Part of a configuration interface which represents a method
         *
         * @param <C> the configuration type
         * @param <S> the current super type
         * @param <R> the return type
         */
        interface MethodElement<C, S, R> {

            /**
             * The method in question
             *
             * @return the method ID
             */
            MethodId methodId();

            /**
             * Gets the return value of the method
             *
             * @return the return value
             */
            TypeToken<R> returnValue();

            /**
             * Calls the method.
             * <p>
             * The receiver type must be an instance of the configuration type {@code C}.
             * <p>
             * The arguments passed must be an array of the appropriate length, with each element having an appropriate
             * type. Primitive types must be boxed.
             *
             * @param configuration the receiver instance
             * @param args the arguments to pass to the method
             * @return the return value of the invocation
             * @throws Throwable any exceptions thrown by the method are rethrown here
             */
            R invokeMethod(C configuration, Object...args) throws Throwable;

            /**
             * Calls the method.
             * <p>
             * The receiver type can be any object which implements the {@code S} supertype currently being visited.
             * <p>
             * The arguments passed must be an array of the appropriate length, with each element having an appropriate
             * type. Primitive types must be boxed.
             *
             * @param implementor the receiver instance
             * @param args the arguments to pass to the method
             * @return the return value of the invocation
             * @throws Throwable any exceptions thrown by the method are rethrown here
             */
            R invokeMethodOnSuper(S implementor, Object...args) throws Throwable;

        }

        /**
         * A default method annotated with {@link CallableFn}, i.e., calling this method on a configuration object will
         * use the implementation in the interface definition.
         *
         * @param <C> the configuration type
         * @param <S> the current super type
         * @param <R> the method return type
         */
        interface MethodTransparent<C, S, R> extends MethodElement<C, S, R> { }

        /**
         * A method that has been turned into a configuration node.
         * <p>
         * This interface allows getting that method's return type and default values, as well as accessing appropriate
         * serialization.
         * <p>
         * The serializer methods in this interface are a natural frontend utilizing the {@link SerializeDeserialize}
         * implementation, which also accounts for interacting with a data tree and handling missing and optional values.
         * Note that the type parameter {@code R} on this interface could represent {@code Optional}, in which case the
         * method node is optional and will be loaded accordingly.
         *
         * @param <C> the configuration type
         * @param <S> the current super type
         * @param <R> the method return type
         */
        interface MethodNode<C, S, R> extends MethodElement<C, S, R> {

            /**
             * Gets whether this method node is optional.
             * <p>
             * This is currently equivalent to checking the return type to see if it is {@code Optional}
             *
             * @return if the method node is optional
             */
            boolean isOptional();

            /**
             * Gets the default values on this method node, if they exist
             *
             * @return the default values if set, {@code null} otherwise
             */
            @Nullable DefaultValues<R> defaultValues();

            /**
             * Deserializes the value of the method node from the given data tree.
             * <p>
             * Depending on whether default values are set, if the source option is missing in the data tree, the default
             * "if missing" value may be loaded as a substitute. In that case, {@link LoadListener#updatedMissingPath(KeyPath)}
             * will be called.
             *
             * @param dataTree the data tree to load from
             * @return the loaded result
             */
            @NonNull
            LoadResult<@NonNull R> deserialize(@NonNull DataTree dataTree);

            /**
             * Deserializes the value of the method node from the given data tree, and updates the tree in place if
             * determined by the serializer.
             * <p>
             * Depending on whether default values are set, if the source option is missing in the data tree, the default
             * "if missing" value may be loaded as a substitute. In that case, {@link LoadListener#updatedMissingPath(KeyPath)}
             * will be called and the data tree will be updated.
             *
             * @param dataTree the data tree to load from and potentially update
             * @return the loaded result
             */
            @NonNull LoadResult<@NonNull R> deserializeUpdate(DataTree.@NonNull Mut dataTree);

            /**
             * Serializes the value of the method node to the output data tree.
             * <p>
             * The value of this method can be obtained from {@link #getValue(Object)}
             *
             * @param value the value to serialize
             * @param dataTree the data tree
             * @throws DeveloperMistakeException if serialization was implemented incorrectly
             */
            void serialize(R value, DataTree.@NonNull Mut dataTree);

            /**
             * Calls the method and returns its value.
             *
             * @param configuration the receiver instance
             * @return the return value
             * @throws DeveloperMistakeException if the configuration method threw an exception, or returned null
             */
            @NonNull R getValue(C configuration);

            /**
             * Calls the method and returns its value.
             *
             * @param implementor the receiver instance
             * @return the return value
             * @throws DeveloperMistakeException if the configuration method threw an exception, or returned null
             */
            @NonNull R getValueOnSuper(S implementor);

        }
    }
}
