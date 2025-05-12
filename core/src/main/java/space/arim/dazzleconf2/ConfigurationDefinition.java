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
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.engine.*;
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

}
