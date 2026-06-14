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
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.backend.Backend;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.engine.UpdateListener;
import space.arim.dazzleconf.engine.TypeLiaison;
import space.arim.dazzleconf.migration.Migration;
import space.arim.dazzleconf.reflect.ReflectionProvider;
import space.arim.dazzleconf.reflect.ReifiedType;
import space.arim.dazzleconf.reflect.TypeToken;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;

/**
 * Main interface.
 * <p>
 * The {@code C} parameter defines the configuration interface being used. For example:
 * <pre>
 *     {@code
 *         interface MyConfig {
 *             default String helloWorld() { return "hi"; }
 *
 *             default boolean enabled() { return true; }
 *         }
 *
 *         Configuration<MyConfig> configuration;
 *     }
 * </pre>
 * <p>
 * We will now describe how this interface fits into the broader library.
 * <p>
 * <b>Obtaining</b>
 * <p>
 * A configuration is obtained from its builder, i.e. {@link ConfigurationBuilder#build()}. The factory methods on this
 * class called {@code defaultBuilder} provide configuration builders with the default liaisons. The builder also lets
 * the library user change the locale for error messages, add migrations, and modify other settings. Its purpose is
 * to obtain this interface.
 * <p>
 * <b>Configuration</b>
 * <p>
 * The main point of usage is the {@link #configureWith(Backend)} method. This method performs many of the main features
 * of this library, and it will be suitable for many users. At the same time, this method is purely implemented using
 * library functions - meaning that any library user who wants to tweak its behavior can implement it themselves.
 * <p>
 * To add simple error handling to <code>configureWith</code>, see {@link #configureOrFallback(Backend, ErrorPrint)}.
 * <p>
 * <b>Preservation of order</b>
 * <p>
 * The library seeks to match the order of entries in a data tree to the order of methods in the interface definition.
 * However, this goal cannot always be accomplished. Sometimes external factors prevent stable ordering. Other times,
 * the end user edits the configuration file. Therefore, the library adopts a holistic approach to maintaining order.
 * <p>
 * If the {@code Backend} implementation cannot write ordered entries, nothing can be done. This behavior is checked by
 * querying {@link Backend.Meta#preservesOrder(boolean)}. For example, TOML cannot write ordered data because the TOML
 * backend's underlying library does not support it.
 * <p>
 * Otherwise, the library's strategy is to match the order of the configuration definition. Note that the default
 * {@link ReflectionProvider} does not scan interface methods in a consistent order: the
 * Java reflections API defines no order for {@link Class#getDeclaredMethods()}. This means the configuration's entries
 * will have a stable, arbitrary order defined when it is created.
 * <p>
 * Thereafter, order support is implemented realistically:
 * <ul>
 *     <li>Writing to a data tree writes in order. Existing data in the output tree may be overwritten, and if so,
 *     existing entries will keep their existing tree order ({@link DataTree} uses a linked map).</li>
 *     <li>Reading and updating data tree in place, such as through {@link #readWithUpdate}, keeps the order of
 *     existing entries. New entries that are inserted because they were missing will be added to the end of the data
 *     tree.</li>
 *     <li>Using the {@code configureWith} or {@code configureOrFallback} methods detects the ordering support from the
 *     backend. If the backend supports order, data trees are sorted based on the configuration definition before
 *     writing. Note that if the configuration is already up-to-date (no missing entries and no update notifications),
 *     it will not be resorted and rewritten.</li>
 * </ul>
 *
 * @param <C> the configuration type
 */
public interface Configuration<C> extends ConfigurationDefinition<C> {

    /**
     * Gets the locale used at the library level.
     * <p>
     * This will be used to display error messages such as in {@link ErrorContext#display()}.
     *
     * @return the locale for error messages
     */
    @NonNull Locale getLocale();

    /**
     * Gets all type liaisons.
     * <p>
     * The order of the list is relevant, with <i>later</i> values being sought to handle types before earlier values.
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
     * Produces a configuration builder for the supplied raw type, assuming it has no generic parameters. The type is
     * treated as unannotated.
     * <p>
     * <b>Default liaisons</b>
     * <p>
     * This method will automaticallly add the default type liaisons to the returned builder. The default type liaisons
     * cover primitive types, {@code String}, enums, {@code Collection}, {@code List}, and {@code Set}. Please see
     * {@link ConfigurationBuilder#addDefaultTypeLiaisons()} for more details. Callers who do not want this behavior
     * may either construct a builder directly or add later type liaisons (which will override earlier liaisons).
     * <p>
     * <b>Generic Parameters</b>
     * <p>
     * That the configuration type <code>configType</code> cannot use generic parameters. <code>Class</code> objects
     * are not parameterized, meaning the type {@code C} would not be available at runtime. If you need to use a
     * parameterized configuration type, please use {@link #defaultBuilder(TypeToken)} and specify the generic
     * arguments by creating a type token.
     *
     * @param <C> the config type
     * @param configType the config class, which cannot have generic parameters
     * @return a config builder, with the default type liaisons set
     * @throws DeveloperMistakeException if the configuration class has generic parameters
     */
    static <C> @NonNull ConfigurationBuilder<C> defaultBuilder(@NonNull Class<C> configType) {
        if (configType.getTypeParameters().length != 0) {
            throw new DeveloperMistakeException("Cannot use Configuration.builder(Class) with a generic type.");
        }
        return defaultBuilder(new TypeToken<>(ReifiedType.rawUnannotated(configType)));
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
     * cover primitive types, {@code String}, enums, {@code Collection}, {@code List}, and {@code Set}. Please see
     * {@link ConfigurationBuilder#addDefaultTypeLiaisons()} for more details. Callers who do not want this behavior
     * may either construct a builder directly or add later type liaisons (which will override earlier liaisons).
     *
     * @param <C> the config type
     * @param configType the reified type token, the runtime equivalent of {@code C}
     * @return a config builder, with the default type liaisons set
     */
    static <C> @NonNull ConfigurationBuilder<C> defaultBuilder(@NonNull TypeToken<C> configType) {
        return new ConfigurationBuilder<>(configType).addDefaultTypeLiaisons();
    }

    /**
     * A simple read from a data tree.
     * <p>
     * This function loads from the data tree without modifying it. Missing values are substituted as appropriate, and
     * the configuration is instantiated and returned upon success. Fails if the deserialization for a particular type
     * rejected that value, or if the value is missing and missing/default values do not exist for the method node.
     * <p>
     * The key mapper used is either the one set during construction, or the default (no-op) key mapper.
     * <p>
     * Note that the following features are not supported by this function:
     * <ul>
     *     <li>Key mapper provision from the backend, if the key mapper is not set at construction.</li>
     *     <li>Migrations.</li>
     * </ul>
     * These features are implemented by the {@code configureWith} and {@code configureOrFallback} methods.
     *
     * @param dataTree the data tree to read from
     * @return the loaded configuration
     */
    @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree);

    /**
     * A simple, stateless read from a data tree.
     * <p>
     * This function loads from the data tree without modifying it. Missing values are substituted as appropriate, and
     * the configuration is instantiated and returned upon success. Fails if the deserialization for a particular type
     * rejected that value, or if the value is missing and missing/default values do not exist for the method node.
     * <p>
     * The key mapper used is either the one set during construction, or the default (no-op) key mapper.
     * See {@link #readFrom(DataTree, ReadOptions)} for a list of features that this method <i>does not</i> support.
     *
     * @param dataTree the data tree to read from
     * @param updateListener a listener which informs the caller if certain events happened
     * @return the loaded configuration
     */
    @NonNull LoadResult<@NonNull C> readFrom(@NonNull DataTree dataTree, @NonNull UpdateListener updateListener);

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
     * data back to the backend. Yields the instantiated configuration.
     * <p>
     * The full operational steps are described in {@link #configureWith(Backend, ConfigureListener)}. See that method
     * for full documentation and features implemented.
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
     * <p>
     * <b>Features supported by this method and related overloads</b>
     * <p>
     * The following features are provided by this function and its associated family ({@code configureWith} and
     * {@code configureOrFallback} overloads).
     * <ul>
     *     <li>Key mapper provision based on the backend, if no key mapper was set on this configuration during
     *     construction of this {@code Configuration}.</li>
     *     <li>Migrations.</li>
     *     <li>Re-sorting of the configuration, if the backend supports order on writing but not reading.</li>
     *     <li>Detection and application of comment support, based on the backend.</li>
     * </ul>
     * <p>
     * As you can see, this method provides a backend-aware read-and-update operation. Other methods on this interface
     * like {@link #readFrom(DataTree)} do not have such an awareness. Because of this, this method is recommended for
     * most developers.
     * <p>
     * <b>Algorithm</b>
     * <p>
     * The full procedure of this function is as follows:
     * <ol>
     *     <li>If the file does not exist, write the defaults and return them.</li>
     *     <li>Try to apply any migrations. If a migration matched, write the new configuration and return it.</li>
     *     <li>Read and update the configuration
     *     <ul>
     *         <li>Use {@link #readWithUpdate(DataTree.Mut, ReadWithUpdateOptions)} to perform the operation. Use the
     *         key mapper from the backend or this object. Notify the provided {@code configureListener} of any updates
     *         </li>
     *         <li>If the backend supports writing comments, include comments on new entries that are inserted</li>
     *         <li>If the backend supports writing but not reading comments, regenerate comments on existing entries.</li>
     *         <li>If needed, deep sort the data tree based on the configuration layout. A need exists when the backend
     *         supports writing sorted data but not reading sorted data, or if the backend supports reading and writing
     *         ordered data and new entries were inserted at the back of the data tree.</li>
     *     </ul>
     *     </li>
     *     <li>If any updates occurred, write the configuration back to the file.</li>
     * </ol>
     *
     * @param backend the format backend
     * @param configureListener a listener which informs the caller if certain events happened
     * @return the loaded configuration
     * @throws UncheckedIOException if the backend threw this error, it is propagated
     */
    @NonNull LoadResult<@NonNull C> configureWith(@NonNull Backend backend, @NonNull ConfigureListener configureListener);

    /**
     * Configures, migrates, and/or updates the backend as needed. Falls back to the default values if an error
     * occured and prints that error.
     * <p>
     * This "all-in-one" function leverages multiple of this library's best features. It checks for migrations and
     * updates the config as necessary, up to the latest version. If the config was on the latest version, loads it
     * and substitutes missing values as necessary. Lastly, if any of these operations produced a change, writes the
     * data back to the backend. Yields the instantiated configuration.
     * <p>
     * This function is similar to {@link #configureWith(Backend)} but with error handling layered on top. That error
     * handling is simple: upon failure, print the error and return default configuration. The backend itself is left
     * unchanged if an error occured (e.g., an erring file would be left on disk as-is).
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
     * data back to the backend. Yields the instantiated configuration.
     * <p>
     * This function is similar to {@link #configureWith(Backend)} but with error handling layered on top. That error
     * handling is simple: upon failure, print the error and return default configuration. The backend itself is left
     * unchanged if an error occured (e.g., an erring file would be left on disk as-is).
     *
     * @param backend the format backend
     * @param configureListener a listener which informs the caller if certain events happened
     * @param errorPrint if an error occured, it will be printed through this argument
     * @return the loaded configuration, or default configuration if an error occured
     * @throws UncheckedIOException if the backend threw this error, it is propagated
     */
    @NonNull C configureOrFallback(@NonNull Backend backend, @NonNull ConfigureListener configureListener,
                                   @NonNull ErrorPrint errorPrint);

    /**
     * Creates a reload shell for this configuration.
     * <p>
     * <b>Reloading and Passthrough</b>
     * <p>
     * By using {@link ReloadShell#getShell()}, the caller receives a transparent proxy {@code C}, called the "shell."
     * All calls on this shell will be automatically passed through to the current delegate, which can be updated at
     * any time using {@link ReloadShell#setCurrentDelegate(Object)}.
     * <p>
     * By using the shell to refer to configuration values, reloading is made easier. The caller can safely store the
     * shell (e.g., in final fields) while retaining the ability to reload the backing values at any time. Because of
     * the passthrough behavior of the shell, method calls to the proxy will automatically use the latest values.
     * <p>
     * <b>Cloaking</b>
     * <p>
     * While reloading is the primary purpose of this method, it can also be used to limit the type of configuration
     * object. For example, let's say we have some {@code X} where {@code X extends C}. While the shell generated by
     * this method will always be exactly of type {@code C}, the backing delegate could be an {@code X}. This enables
     * users of this method to "cloak" an instance of {@code X} as if it were an instance of {@code C}, such that
     * other code can never cast to an {@code X}.

     * @param initialValue the initial value of the delegate; if {@code null}, calls to the shell will generate NPE's.
     * @return a reload shell
     */
    @NonNull ReloadShell<C> makeReloadShell(@Nullable C initialValue);

    /**
     * Creates a convenient error source.
     * <p>
     * This error source won't provide any additional contexts to created {@code ErrorContext}s. It is provided so
     * that callers need not undergo the pain of implementing their own error context infrastructure.
     *
     * @return an error source
     */
    ErrorContext.@NonNull Source makeErrorSource();

}
