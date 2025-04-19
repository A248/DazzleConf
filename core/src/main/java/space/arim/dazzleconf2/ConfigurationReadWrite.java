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

import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DataTreeMut;
import space.arim.dazzleconf2.engine.LoadListener;

/**
 * Provides the minimal methods for reading and writing configurations from data trees
 *
 * @param <C> the configuration type
 */
public interface ConfigurationReadWrite<C> {

    /**
     * Loads the default configuration.
     * <p>
     * This will build a configuration object using only the default-providing mechanisms (i.e. annotations and
     * default methods).
     *
     * @return a configuration using wholly default values
     * @throws DeveloperMistakeException if one of the default-providing methods threw an exception
     */
    C loadDefaults();

    /**
     * A simple, stateless read from a data tree.
     * <p>
     * This function loads from the data tree without modifying it, and it does not use migrations. The configuration
     * is instantiated and returned upon success.
     *
     * @param dataTree the data tree to read from
     * @return the loaded configuration
     */
    LoadResult<C> readFrom(DataTree dataTree);

    /**
     * A simple, stateless read from a data tree.
     * <p>
     * This function loads from the data tree without modifying it, and it does not use migrations. The configuration
     * is instantiated and returned upon success.
     *
     * @param dataTree the data tree to read from
     * @param loadListener a listener which informs the caller if certain events happened
     * @return the loaded configuration
     */
    LoadResult<C> readFrom(DataTree dataTree, LoadListener loadListener);

    /**
     * Writes to the given data tree.
     * <p>
     * The output data tree does not need to be empty, but there are no guarantees that existing data will not be
     * overidden or cleared. The values of the provided configuration are written to it, and it does not matter
     * how the {@code config} parameter is implemented so long as it returns non-null values.
     *
     * @param config the configuration
     * @param dataTree the data tree to write to
     */
    void writeTo(C config, DataTreeMut dataTree);

}
