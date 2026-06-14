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

package space.arim.dazzleconf;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.DefinedLayout;
import space.arim.dazzleconf.engine.DeserializeInput;
import space.arim.dazzleconf.engine.Interprocessor;
import space.arim.dazzleconf.engine.UpdateListener;
import space.arim.dazzleconf.engine.SerializeDeserialize;
import space.arim.dazzleconf.engine.SerializeOutput;
import space.arim.dazzleconf.reflect.TypeToken;

/**
 * Provides the methods for reading and writing configurations from data trees.
 * <p>
 * <b>Preservation of order</b>
 * <p>
 * Writing and updating data trees in place takes reasonable steps to preserve order of the defined interface's methods,
 * without compromising efficiency.
 * <p>
 * In particular:
 * <ul>
 *     <li>Writing to a data tree writes in order. Existing data in the output tree may be overwritten, and if so,
 *     existing entries will keep their existing tree order ({@link DataTree} uses a linked map).</li>
 *     <li>Reading and updating data tree in place, such as through {@link #readWithUpdate}, keeps the order of
 *     existing entries. New entries that are inserted because they were missing will be added to the end of the data
 *     tree.</li>
 * </ul>
 * Ordering considerations related to different backends and the reflection mechanism are documented in
 * {@link Configuration}. Thus, even if operations that manipulate data trees preserve order, it is possible that other
 * library areas may destroy it.
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
     * Gets the low-level layout of the configuration interface (or type)
     *
     * @return the scanned layout of the configuration interface (or type)
     */
    @NonNull DefinedLayout<C> getDefinedLayout();

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
     * Base interface for read, write, and read-with-update options.
     *
     */
    interface OperationOptions {

        /**
         * The key mapper to use
         *
         * @return the key mapper
         */
        @NonNull KeyMapper keyMapper();

        /**
         * Gets the absolute key path at which the operation is taking place.
         * <p>
         * The implementor of this method can return an empty path to represent the root configuration or if the path
         * prefix is unknown.
         *
         * @return the path at which the configuration is read or written, or an empty {@code KeyPath} if none. It is
         * recommended to return a {@code KeyPath.Immut}, since the caller might modify this return value
         */
        @NonNull KeyPath keyPath();

        /**
         * Gets the interprocessor. This can be used for adding hooks in various places, including but not limited to:
         * data tree sorting, custom logic for comment copying, and low-level aggregate listening.
         *
         * @return the interprocessor
         */
        @NonNull Interprocessor getInterprocessor();

    }

    /**
     * Parameters for reading a configuration from a tree
     *
     */
    interface ReadOptions extends UpdateListener, OperationOptions {

        /**
         * Gets the interprocessor.
         * <p>
         * Currently, reading a configuration <i>without updates</i> does not use interprocessor hooks in any places,
         * but this may change in the future. Note that {@link ReadWithUpdateOptions} <i>does</i> use this object.
         *
         * @return the interprocessor
         */
        @Override
        @NonNull Interprocessor getInterprocessor();

        /**
         * The maximum number of errors to collect before exiting. Must be greater than 0.
         * <p>
         * If reading the configuration failed, the size of {@link LoadResult#getErrorContexts()} will be at most this
         * number.
         *
         * @return the maximum number of errors to collect, default 8. Must be greater than 0
         */
        default int maximumErrorCollect() {
            return 8;
        }

    }

    /**
     * Parameters for reading a configuration and updating it in-place
     *
     */
    interface ReadWithUpdateOptions extends ReadOptions, WriteOptions {

        /**
         * Gets the interprocessor. Reading a configuration with updates uses interprocessor hooks in a few places:
         * <ul>
         *     <li>When new data entries are written to the tree, because they were missing: {@link DefinedLayout#WRITE_STRUCTURED_ENTRY}</li>
         *     <li>When existing data entries are updated: {@link DefinedLayout#UPDATE_STRUCTURED_ENTRY}</li>
         *     <li>When the tree itself is ready: {@link DefinedLayout#WRITE_STRUCTURED_TREE}</li>
         * </ul>
         *
         * @return the interprocessor
         */
        @Override
        @NonNull Interprocessor getInterprocessor();

    }

    /**
     * Parameters for writing a configuration to a tree
     *
     */
    interface WriteOptions extends OperationOptions {

        /**
         * Gets the interprocessor. Writing a configuration, in particular, uses interprocessor hooks in two places:
         * <ul>
         *     <li>When data entries are written to the tree: {@link DefinedLayout#WRITE_STRUCTURED_ENTRY}</li>
         *     <li>When the tree itself is ready: {@link DefinedLayout#WRITE_STRUCTURED_TREE}</li>
         * </ul>
         *
         * @return the interprocessor
         */
        @Override
        @NonNull Interprocessor getInterprocessor();

    }

}
