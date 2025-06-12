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
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.LabelSorting;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.Collection;
import java.util.stream.Stream;

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
    @NonNull Layout getLayout();

    /**
     * The layout of a configuration definition.
     * <p>
     * This layout includes all of a scanned interface's components.
     */
    interface Layout {

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
         * Gets the labels of this layout.
         * <p>
         * These are the same as the method names from the configuration interface, excluding default methods annotated
         * with <code>@CallableFn</code>. Their order will match however this definition orders its entries
         * <p>
         * The returned collection is immutable and ordered according to how this {@code ConfigurationDefinition} orders
         * its entries.
         * <p>
         * Note that this method returns <i>labels</i>, not keys. As such, a {@code KeyMapper} has not been applied to
         * the elements. If callers want key mapping, they must perform it themselves.
         *
         * @return the ordered labels for this definition, an immutable but ordered collection. If using Java 21 or
         * later, this return value can be safely cast to {@code java.util.SequencedCollection}.
         */
        @NonNull Collection<@NonNull String> getLabels();

        /**
         * Gets the labels, as a stream.
         * <p>
         * This function is a stream version of {@link #getLabels()}, and using it may be more efficient than calling
         * <code>getLabels().stream()</code>.
         *
         * @return the ordered labels for this definition
         */
        @NonNull Stream<@NonNull String> getLabelsAsStream();

    }

    /**
     * Gets the instantiator used to generate interface implementations.
     *
     * @return the instantiator
     */
    @NonNull Instantiator getInstantiator();

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
        @NonNull KeyMapper keyMapper();

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

        // TODO complete the documentation here
        /**
         *
         *
         * Note that if updating the data tree failed with one or more errors, it is undefined as to which entries in
         * the data tree, if any, will be sorted.
         *
         * @return the sorting for the updatable data tree
         */
        @Override
        @API(status = API.Status.EXPERIMENTAL)
        default @NonNull LabelSorting sorting() {
            return LabelSorting.disabled();
        }

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
         * Controls the order in which entries are written to the output data tree.
         * <p>
         * Please see {@link LabelSorting} for a full discussion of ordering, its implications, and the role of that
         * interface.
         *
         * @return the sorting for the output data tree
         */
        @API(status = API.Status.EXPERIMENTAL)
        default @NonNull LabelSorting sorting() {
            return LabelSorting.disabled();
        }

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
