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

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.UpdateListener;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.reflect.MethodMirror;
import space.arim.dazzleconf2.reflect.ReifiedType;
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
     * Gets the layout of the configuration interface
     *
     * @return the scanned layout of the configuration interface
     */
    @API(status = API.Status.MAINTAINED)
    @NonNull Layout getLayout();

    /**
     * The layout of a configuration definition.
     * <p>
     * This layout includes all of a scanned interface's components, as well as reflective services used to access
     * those components.
     */
    @API(status = API.Status.MAINTAINED)
    interface Layout {

        /**
         * Gets the type of the configuration interface.
         * <p>
         * This is always the same type as {@link ConfigurationDefinition#getType()}.
         *
         * @return the type of the configuration interface
         */
        ReifiedType.@NonNull Annotated getReifiedType();

        /**
         * Gets the top level comments on the configuration interface.
         * <p>
         * These are the comments that exist at the global level and are not tied to any specific entry. If none are
         * set, an empty {@code Comments} is returned.
         *
         * @return the top level comments, which may be empty if not set
         */
        @NonNull CommentData getComments();

        /**
         * Gets the instantiator used to generate interface implementations.
         *
         * @return the instantiator
         */
        @NonNull Instantiator getInstantiator();

        /**
         * Gets the method mirror used to invoke methods
         *
         * @return the method mirror
         */
        @NonNull MethodMirror getMethodMirror();

    }

    /**
     * Loads the default configuration.
     * <p>
     * This will build a configuration object using only the default-providing mechanisms (i.e. annotations and
     * default methods). Therefore, to use this method, every configuration entry is required to have a default value
     * attached; if an entry lacks a default value, {@code DeveloperMistakeException} will be thrown.
     *
     * @return a configuration using wholly default values
     * @throws DeveloperMistakeException if one of the default-providing methods threw an exception, or gave null.
     * Alternatively, if a configuration entry is lacking a default value set either by default methods or annotations
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
     * (as determined by {@link SerializeDeserialize#deserializeUpdate(DeserializeInput, SerializeOutput)}) then the
     * objects will be updated in the data tree. Callers can check whether any updates ocurred by using a load listener
     * in the read options.
     *
     * @param dataTree the data tree to read and update
     * @param readOptions full parameters to customize the operation
     * @return the loaded configuration, or an error if failed
     */
    @NonNull LoadResult<@NonNull C> readWithUpdate(DataTree.@NonNull Mut dataTree, @NonNull ReadWithUpdateOptions readOptions);

    /**
     * Writes to the given data tree.
     * <p>
     * The output data tree does not need to be empty, but there are no guarantees that existing data will not be
     * overidden or cleared. The values of the provided configuration are written to it, and it does not matter
     * how the {@code config} parameter is implemented so long as it returns non-null values.
     * <p>
     * Values are guaranteed to be inserted in the provided tree in the same order as the layout of this definition.
     * Note that the order of the layout may differ from the source code order of methods.
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
    interface ReadOptions extends UpdateListener {

        /**
         * The key mapper to use
         *
         * @return the key mapper
         */
        @NonNull KeyMapper keyMapper();

        /**
         * Gets the absolute key path at which the configuration is being read.
         * <p>
         * This path is intended as a debug value and might be used in error messages, such as by using it to prefi
         * sub-paths within this configuration. The implementor of this method can return an empty path to represent
         * the root configuration or if the path prefix is unknown.
         *
         * @return the path at which the configuration is read, or an empty {@code KeyPath} if none. It is recommended
         * to return a {@code KeyPath.Immut}, since the caller might modify this return value
         */
        @NonNull KeyPath keyPath();

        /**
         * The maximum number of errors to collect before exiting. Must be greater than 0.
         * <p>
         * If reading the configuration failed, the size of {@link LoadResult#getErrorContexts()} will be at most this
         * number.
         *
         * @return the maximum number of errors to collect, default 12. Must be greater than 0
         */
        default int maximumErrorCollect() {
            return 12;
        }

    }

    /**
     * Parameters for reading a configuration and updating it in-place
     *
     */
    interface ReadWithUpdateOptions extends ReadOptions, WriteOptions {

        /**
         * Controls whether comments on existing data entries are refreshed.
         * <p>
         * Newly-added entries (due to missing values) will always have comments set. This setting controls whether
         * existing entries in the data tree will have their comments refreshed at the given location: i.e., by setting
         * the comments anew on the entry.
         * <p>
         * This function can be used, for example, to continually write comments on entries, if the backend supports
         * writing but not reading comments. Use {@link Backend.Meta#supportsComments(boolean, boolean, CommentLocation)}
         * to check the extent of backend support.
         *
         * @param location the location of comments with respect to the entry
         * @return whether refreshing should take place for entry comments there
         */
        @Override
        @API(status = API.Status.EXPERIMENTAL)
        default boolean writeEntryComments(@NonNull CommentLocation location) {
            return false;
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
         * @return the key mapper
         */
        @NonNull KeyMapper keyMapper();

        /**
         * Controls whether comments are written to data entries.
         * <p>
         * If enabled (the default), comment data is copied from the configuration definition to entries in the output
         * data tree. If disabled, written {@code DataEntry}s will be without comments.
         * <p>
         * <b>Backend support</b>
         * <p>
         * Note that even if this function returns true, the backend is not guaranteed to support writing comments.
         * Please consult the documentation of the {@link Backend} regarding comment support.
         *
         * @param location the location of the entry comments being written
         * @return whether data entries should be written with comments here, default true
         */
        @API(status = API.Status.EXPERIMENTAL)
        default boolean writeEntryComments(@NonNull CommentLocation location) {
            return true;
        }
    }

}
