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
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.engine.UpdateListener;
import space.arim.dazzleconf2.migration.Migration;
import space.arim.dazzleconf2.reflect.ReifiedType;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;

/**
 * Main interface
 *
 * @param <C> the configuration type
 */
public interface Configuration<C> extends ConfigurationDefinition<C> {

    /**
     * Gets the locale used at the library level. This will be used to display error messages such as in
     * {@link ErrorContext#displayDetails()}
     *
     * @return the locale for error messages
     */
    @NonNull Locale getLocale();

    /**
     * Gets all type liaisons. The order of the list is relevant, with earlier values being sought to handle types
     * before later values.
     *
     * @return the type liaisons, which are immutable
     */
    @NonNull List<@NonNull TypeLiaison> getTypeLiaisons();

    /**
     * Gets the key mapper if one was specified during construction. Note that a specified key mapper will override
     * the standard key mapper provided by the backend ({@link Backend#recommendKeyMapper()}
     *
     * @return the key mapper if specified
     */
    @Nullable KeyMapper getKeyMapper();

    /**
     * Gets the migrations. The order of the list is relevant, with earlier migrations being tried before later ones.
     *
     * @return the migrations, which are immutable
     */
    @NonNull List<@NonNull Migration<?, C>> getMigrations();

    /**
     * Convenience method for building a configuration.
     * <p>
     * This will automaticallly add the default type liaisons to the returned builder. The default type liaisons
     * cover primitive types, <code>String</code>, and enums, and they will take precedence over any type liaisons
     * added later. Please see {@link ConfigurationBuilder#addDefaultTypeLiaisons()} for more details. Callers who do
     * not want this behavior may construct a builder directly.
     * <p>
     * Note that the configuration type <code>configType</code> cannot use generic parameters. <code>Class</code>
     * objects are not parameterized, meaning the type {@code C} would not be available at runtime. If you need to
     * use a parameterized configuration type, please use {@link #defaultBuilder(TypeToken)} and specify the generic
     * arguments by creating a type token.
     *
     * @param <C> the config type
     * @param configType the config class, which cannot have generic parameters
     * @return a config builder, with the default type liaisons set
     */
    static <C> @NonNull ConfigurationBuilder<C> defaultBuilder(@NonNull Class<C> configType) {
        if (configType.getTypeParameters().length != 0) {
            throw new IllegalArgumentException("Cannot use Configuration.builder(Class) with a generic type.");
        }
        return defaultBuilder(new TypeToken<>(new ReifiedType.Annotated(
                configType, ReifiedType.Annotated.EMPTY_ARRAY, configType
        )));
    }

    /**
     * Convenience method for building a configuration that adds the default type liaisons.
     * <p>
     * <b>Example usage</b>
     * <pre>
     * {@code
     * Configuration<MyConfig> config = Configuration.defaultBuilder(new TypeToken<MyConfig>() {});
     * }
     * </pre>
     * <p>
     * <b>Default liaisons</b>
     * <p>
     * This method will automaticallly add the default type liaisons to the returned builder. The default type liaisons
     * cover primitive types, <code>String</code>, and enums, and they will take precedence over any type liaisons
     * added later. Please see {@link ConfigurationBuilder#addDefaultTypeLiaisons()} for more details. Callers who do
     * not want this behavior may construct a builder directly instead of using this method.
     *
     * @param <C> the config type
     * @param configType the reified type token, the runtime equivalent of {@code C}
     * @return a config builder, with the default type liaisons set
     */
    static <C> @NonNull ConfigurationBuilder<C> defaultBuilder(@NonNull TypeToken<C> configType) {
        return new ConfigurationBuilder<>(configType).addDefaultTypeLiaisons();
    }

    /**
     * A simple, stateless read from a data tree.
     * <p>
     * This function loads from the data tree without modifying it, and it does not use migrations. The configuration
     * is instantiated and returned upon success.
     * <p>
     * The key mapper used is either the one set during construction, or the default (no-op) key mapper.
     *
     * @param dataTree the data tree to read from
     * @return the loaded configuration
     */
    @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree);

    /**
     * A simple, stateless read from a data tree.
     * <p>
     * This function loads from the data tree without modifying it, and it does not use migrations. The configuration
     * is instantiated and returned upon success.
     * <p>
     * The key mapper used is either the one set during construction, or the default (no-op) key mapper.
     *
     * @param dataTree the data tree to read from
     * @param loadListener a listener which informs the caller if certain events happened
     * @return the loaded configuration
     */
    @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull LoadListener loadListener);

    /**
     * Writes to the given data tree.
     * <p>
     * The output data tree does not need to be empty, but any existing data may be overwritten or cleared. The values
     * of the provided configuration are written to it, and it does not matter how the {@code config} parameter is
     * implemented so long as it returns non-null values without throwing exceptions.
     * <p>
     * The key mapper used is either the one set during construction, or the default (no-op) key mapper.
     *
     * @param config the configuration
     * @param dataTree the data tree to write to
     */
    void writeTo(@NonNull C config, DataTree.@NonNull Mut dataTree);

    /**
     * Configures, migrates, and/or updates the backend as needed.
     * <p>
     * This "all-in-one" function leverages multiple of this library's best features. It checks for migrations and
     * updates the config as necessary, up to the latest version. If the config was on the latest version, loads it
     * and substitutes missing values as necessary. Lastly, if any of these operations produced a change, writes the
     * config back to the backend. Yields the instantiated configuration.
     *
     * @param backend the format backend
     * @return the loaded configuration
     * @throws UncheckedIOException if the backend threw this error, it is propagated
     */
    @NonNull LoadResult<@NonNull C> configureWith(@NonNull Backend backend);

    /**
     * Configures, migrates, and/or updates the backend as needed.
     * <p>
     * This "all-in-one" function leverages multiple of this library's best features. It checks for migrations and
     * updates the config as necessary, up to the latest version. If the config was on the latest version, loads it
     * and substitutes missing values as necessary. Lastly, if any of these operations produced a change, writes the
     * config back to the backend. Yields the instantiated configuration.
     *
     * @param backend the format backend
     * @param updateListener a listener which informs the caller if certain events happened
     * @return the loaded configuration
     * @throws UncheckedIOException if the backend threw this error, it is propagated
     */
    @NonNull LoadResult<@NonNull C> configureWith(@NonNull Backend backend, @NonNull UpdateListener updateListener);

    /**
     * Configures, migrates, and/or updates the backend as needed. Falls back to the default values if an error
     * occured and prints that error.
     * <p>
     * This "all-in-one" function leverages multiple of this library's best features. It checks for migrations and
     * updates the config as necessary, up to the latest version. If the config was on the latest version, loads it
     * and substitutes missing values as necessary. Lastly, if any of these operations produced a change, writes the
     * config back to the backend. Yields the instantiated configuration.
     * <p>
     * This function is similar to {@link #configureWith(Backend)} but with error handling layered on top. That error
     * handling is simple: upon failure, print the error, return default configuration.
     *
     * @param backend the format backend
     * @param errorPrint if an error occured, it will be printed through this argument
     * @return the loaded configuration, or default configuration if an error occured
     * @throws UncheckedIOException if the backend threw this error, it is propagated
     */
    @NonNull C configureOrFallback(@NonNull Backend backend, @NonNull ErrorPrint errorPrint);

    /**
     * Configures, migrates, and/or updates the backend as needed. Falls back to the default values if an error
     * occured and prints that error.
     * <p>
     * This "all-in-one" function leverages multiple of this library's best features. It checks for migrations and
     * updates the config as necessary, up to the latest version. If the config was on the latest version, loads it
     * and substitutes missing values as necessary. Lastly, if any of these operations produced a change, writes the
     * config back to the backend. Yields the instantiated configuration.
     * <p>
     * This function is similar to {@link #configureWith(Backend)} but with error handling layered on top. That error
     * handling is simple: upon failure, print the error, return default configuration.
     *
     * @param backend the format backend
     * @param updateListener a listener which informs the caller if certain events happened
     * @param errorPrint if an error occured, it will be printed through this argument
     * @return the loaded configuration, or default configuration if an error occured
     * @throws UncheckedIOException if the backend threw this error, it is propagated
     */
    @NonNull C configureOrFallback(@NonNull Backend backend, @NonNull UpdateListener updateListener,
                                   @NonNull ErrorPrint errorPrint);

    /**
     * Creates a reload shell for this configuration.
     * <p>
     * By using {@link ReloadShell#getShell()}, the caller receives a transparent proxy {@code C} whose values can be
     * updated at any time using {@link ReloadShell#setCurrentDelegate(Object)}. This can allow the caller to safely
     * store the proxy (e.g., in final fields) while retaining the ability to reload their configuration at any time.
     * Method calls to the proxy will automatically use the latest values.

     * @param initialValue the initial value of the delegate; if {@code null}, calls to the shell will generate NPE's.
     * @return a reload shell
     */
    @NonNull ReloadShell<C> makeReloadShell(@Nullable C initialValue);

}
