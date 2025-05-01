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
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf2.engine.liaison.*;
import space.arim.dazzleconf2.internals.ImmutableCollections;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.migration.Migration;
import space.arim.dazzleconf2.reflect.*;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.*;

/**
 * A builder for {@link Configuration}. The builder allows changing how the configuration is defined, read,
 * serialized, and instantiated.
 * <p>
 * <b>Construction</b>
 * <p>
 * A builder can be made either through the factory methods like {@link Configuration#defaultBuilder} or by direct
 * construction. If constructed directly, the builder is empty and type liaisons will need to be added to it.
 * <p>
 * <b>Type Liaisons</b>
 * <p>
 * Several methods add {@link TypeLiaison} instances. This builder stores type liaisons in the order they are added.
 * This is significant because earlier type liaisons will be queried first in the resulting configuration:<ul>
 * <li>{@link #addTypeLiaisons(TypeLiaison...)}
 * <li>{@link #addTypeLiaisons(List)}
 * <li>{@link #addPrimitiveTypeLiaisons()}
 * <li>{@link #addDefaultTypeLiaisons()}
 * </ul>
 * To manage which liaisons apply to which types, callers are free to re-arrange calls to the above methods and use
 * direct construction if they wish to override default type handling.
 *
 * @param <C> the configuration type
 */
public final class ConfigurationBuilder<C> {

    private final TypeToken<C> configType;

    // Settings
    private @Nullable Locale locale;
    private final List<TypeLiaison> typeLiaisons = new ArrayList<>();
    private KeyMapper keyMapper;
    private MethodMirror methodMirror = new DefaultMethodMirror();
    private Instantiator instantiator = new DefaultInstantiator();
    private final List<Migration<?, C>> migrations = new ArrayList<>();

    /**
     * Creates from the specified type.
     * <p>
     * This creates an empty configuration builder. No type liaisons are added to it, including primitive types and
     * <code>String</code>. To add liaisons, please use one of the appropriate methods.
     *
     * @param configType the config type
     */
    public ConfigurationBuilder(@NonNull TypeToken<C> configType) {
        this.configType = Objects.requireNonNull(configType, "config type");
    }

    /**
     * Sets the locale for displaying error messages.
     * <p>
     * If not set, defaults to the system locale.
     *
     * @param locale the locale, nonnull
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> locale(Locale locale) {
        this.locale = Objects.requireNonNull(locale, "locale");
        return this;
    }

    /**
     * Adds the following type liaisons to this builder
     * <p>
     * The order is significant, because type liaisons are queried to handle the configuration in the order in which
     * they are declared. Some type liaisons (like for enums) can have a wildcard nature.
     *
     * @param typeLiaisons the type liaisons
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addTypeLiaisons(TypeLiaison...typeLiaisons) {
        this.typeLiaisons.addAll(Arrays.asList(typeLiaisons));
        return this;
    }

    /**
     * Adds the following type liaisons to this builder
     * <p>
     * The order is significant, because type liaisons are queried to handle the configuration in the order in which
     * they are declared. Some type liaisons (like for enums) can have a wildcard nature.
     *
     * @param typeLiaisons the type liaisons
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addTypeLiaisons(List<TypeLiaison> typeLiaisons) {
        this.typeLiaisons.addAll(typeLiaisons);
        return this;
    }

    /**
     * Adds type liaisons for primitives and <code>String</code> to this builder.
     * <p>
     * These type liaisons are part of the default set. However, unlike {@link #addDefaultTypeLiaisons()}, enum types
     * are not covered by this method.
     *
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addPrimitiveTypeLiaisons() {
        // TODO - Impl
        return addTypeLiaisons(new StringLiaison(), new IntegerLiaison());
    }

    /**
     * Adds the default type liaisons to this builder.
     * <p>
     * The default type liaisons are capable of serializing primitive types, <code>String</code>s, and enum types,
     * plus lists of other serializable types, and configuration subsections.
     * <p>
     * The default liaisons support the following annotations to modify their behavior:<ul>
     *     <li><code>IntegerLiaison</code>: <code>@IntegerRange</code></li>
     * </ul>
     *
     *
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addDefaultTypeLiaisons() {
        addPrimitiveTypeLiaisons();
        // TODO - Impl & Javadoc
        return addTypeLiaisons(new ListLiaison(), new SubSectionLiaison());
    }

    /**
     * Adds a simple type liaison based on a single serializer.
     * <p>
     * This method creates a simple type liaison pointing to the given {@link SerializeDeserialize} and paired with
     * the type specified. As such, it cannot load subsections or handle a range of types. Also, you will need to use
     * default methods in your configuration interface to supply default values.
     * <p>
     * <b>API Note</b>
     * <p>
     * This method is closest to version 1's method of handling custom types. It is slightly more limited, as it cannot
     * allow you to depend on other serializers in the same way.
     *
     * @param typeToken the type to handle
     * @param serializeDeserialize the serialization for that type
     * @return this builder
     * @param <V> the type being handled by the serializer
     */
    public <V> @This @NonNull ConfigurationBuilder<C> addSimpleSerializer(TypeToken<V> typeToken,
                                                                          SerializeDeserialize<V> serializeDeserialize) {
        return addTypeLiaisons(new SimpleTypeLiaison<>(typeToken, serializeDeserialize));
    }

    /**
     * Clears the type liaisons currently set on this builder
     *
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> clearTypeLiaisons() {
        this.typeLiaisons.clear();
        return this;
    }

    /**
     * Sets the key mapper
     *
     * @param keyMapper the key mapper, or null to clear
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> keyMapper(KeyMapper keyMapper) {
        this.keyMapper = keyMapper;
        return this;
    }

    /**
     * Sets the method mirror
     *
     * @param methodMirror the method mirror, nonnull
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> methodMirror(MethodMirror methodMirror) {
        this.methodMirror = Objects.requireNonNull(methodMirror);
        return this;
    }

    /**
     * Sets the instantiator
     *
     * @param instantiator the instantiator, nonnull
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> instantiator(Instantiator instantiator) {
        this.instantiator = Objects.requireNonNull(instantiator);
        return this;
    }

    /**
     * Adds the following migration to this builder
     * <p>
     * The order is significant, because migrations are checked in the order in which they are declared. Thus,
     * migrations that come first need to ensure they aren't wrongly handling different or overlapping versions.
     *
     * @param migration the migration
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addMigration(Migration<?, C> migration) {
        this.migrations.add(Objects.requireNonNull(migration));
        return this;
    }

    /**
     * Adds the following migrations to this builder
     * <p>
     * The order is significant, because migrations are checked in the order in which they are declared. Thus,
     * migrations that come first need to ensure they aren't wrongly handling different or overlapping versions.
     *
     * @param migrations the migrations
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addMigrations(List<Migration<?, C>> migrations) {
        this.migrations.addAll(migrations);
        return this;
    }

    /**
     * Builds into a fully fledged configuration.
     * <p>
     * This function is side effect free: it does not modify this builder, which can be re-used after the call.
     *
     * @return the configuration
     * @throws DeveloperMistakeException if the combination or usage of different library features is in error
     */
    @SideEffectFree
    public @NonNull Configuration<C> build() {

        // Harden values
        Locale locale = (this.locale == null) ? Locale.getDefault() : this.locale;
        List<TypeLiaison> typeLiaisons = ImmutableCollections.listOf(this.typeLiaisons);
        List<Migration<?, C>> migrations = ImmutableCollections.listOf(this.migrations);

        // Scan and build definition
        ConfigurationDefinition<C> definition = new DefinitionScan(
                LibraryLang.loadLang(locale), new LiaisonCache(typeLiaisons), methodMirror, instantiator
        ).read(configType);

        // Yield final
        return new BuiltConfig<>(definition, locale, typeLiaisons, keyMapper, instantiator, migrations);
    }
}
