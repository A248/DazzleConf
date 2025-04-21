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

import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DataTreeMut;
import space.arim.dazzleconf2.engine.KeyMapper;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.engine.UpdateListener;
import space.arim.dazzleconf2.migration.Migration;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.reflect.ReifiedType;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Main interface
 *
 * @param <C> the configuration type
 */
public interface Configuration<C> extends ConfigurationReadWrite<C> {

    /**
     * Gets the locale used at the library level. This will be used to display error messages such as in
     * {@link ErrorContext#displayDetails()}
     *
     * @return the locale for error messages
     */
    Locale getLocale();

    /**
     * Gets all type liaisons. The order of the list is relevant, with earlier values being sought to hande types
     * before later values.
     *
     * @return the type liaisons, which are immutable
     */
    List<TypeLiaison> getTypeLiaisons();

    /**
     * Gets the key mapper if one was specified during construction. Note that a specified key mapper will override
     * the standard key mapper provided by the backend ({@link Backend#recommendKeyMapper()}
     *
     * @return the key mapper if specified
     */
    Optional<KeyMapper> getKeyMapper();

    /**
     * Gets the instantiator responsible for constructing instances
     *
     * @return the instantiator
     */
    Instantiator getInstantiator();

    /**
     * Gets the migrations. The order of the list is relevant, with earlier migrations being tried before later ones.
     *
     * @return the migrations, which are immutable
     */
    List<Migration<?, C>> getMigrations();

    /**
     * Convenience method for building a configuration
     *
     * @param <C> the config type
     * @param configType the config class
     * @return a config builder
     */
    static <C> ConfigurationBuilder<C> builder(Class<C> configType) {
        if (configType.getTypeParameters().length != 0) {
            throw new IllegalArgumentException("Cannot use Configuration.builder(Class) with a generic type.");
        }
        return new ConfigurationBuilder<>(new TypeToken<>(new ReifiedType.Annotated(configType, configType)));
    }

    /**
     * Convenience method for building a configuration
     *
     * @param <C> the config type
     * @param configType the reified type token
     * @return a config builder
     */
    static <C> ConfigurationBuilder<C> builder(TypeToken<C> configType) {
        return new ConfigurationBuilder<>(configType);
    }

    /**
     * A simple, stateless read from a data tree.
     * <p>
     * This function loads from the data tree without modifying it, and it does not use migrations. The configuration
     * is instantiated and returned upon success.
     * <p>
     * The key mapper used is either the one set during construction, or the one recommended by the backend.
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
     * <p>
     * The key mapper used is either the one set during construction, or the one recommended by the backend.
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
     * <p>
     *  The key mapper used is either the one set during construction, or the one recommended by the backend.
     *
     * @param config the configuration
     * @param dataTree the data tree to write to
     */
    void writeTo(C config, DataTreeMut dataTree);

    /**
     * Configures, migrates, and/or updates the backend as needed.
     * <p>
     * This "all-in-one" function leverages multiple of this library's best features. t checks for migrations and
     * updates the config as necessary, up to the latest version. If the config was on the latest version, loads it
     * and substitutes missing values as necessary. Lastly, if any of these operations produced a change, writes the
     * config back to the backend. Yields the instantiated configuration.
     *
     * @param backend the format backend
     * @return the loaded configuration
     */
    LoadResult<C> configureWith(Backend backend);

    /**
     * Configures, migrates, and/or updates the backend as needed.
     * <p>
     * This "all-in-one" function leverages multiple of this library's best features. t checks for migrations and
     * updates the config as necessary, up to the latest version. If the config was on the latest version, loads it
     * and substitutes missing values as necessary. Lastly, if any of these operations produced a change, writes the
     * config back to the backend. Yields the instantiated configuration.
     *
     * @param backend the format backend
     * @param updateListener a listener which informs the caller if certain events happened
     * @return the loaded configuration
     */
    LoadResult<C> configureWith(Backend backend, UpdateListener updateListener);

}
