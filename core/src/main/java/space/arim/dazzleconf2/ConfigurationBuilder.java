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

import space.arim.dazzleconf.internal.DefinitionReader;
import space.arim.dazzleconf2.engine.KeyMapper;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.migration.Migration;
import space.arim.dazzleconf2.reflect.DefaultInstantiator;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.reflect.TypeToken;
import space.arim.dazzleconf2.translation.LibraryLang;

import java.util.*;

/**
 * Builder for {@link Configuration}
 *
 * @param <C> the configuration type
 */
public final class ConfigurationBuilder<C> {

    private final TypeToken<C> configType;

    // Settings
    private Locale locale = Locale.ENGLISH;
    private final List<TypeLiaison> typeLiaisons = new ArrayList<>();
    private KeyMapper keyMapper;
    private Instantiator instantiator = new DefaultInstantiator();
    private final List<Migration<?, C>> migrations = new ArrayList<>();

    /**
     * Creates from the specified type
     * @param configType the config type
     */
    public ConfigurationBuilder(TypeToken<C> configType) {
        this.configType = Objects.requireNonNull(configType, "config type");
    }

    /**
     * Sets the locale for displaying error messages
     *
     * @param locale the locale, nonnull
     * @return this builder
     */
    public ConfigurationBuilder<C> locale(Locale locale) {
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
    public ConfigurationBuilder<C> addTypeLiaisons(TypeLiaison...typeLiaisons) {
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
    public ConfigurationBuilder<C> addTypeLiaisons(List<TypeLiaison> typeLiaisons) {
        this.typeLiaisons.addAll(typeLiaisons);
        return this;
    }

    /**
     * Sets the key mapper
     *
     * @param keyMapper the key mapper, or null to clear
     * @return this builder
     */
    public ConfigurationBuilder<C> keyMapper(KeyMapper keyMapper) {
        this.keyMapper = keyMapper;
        return this;
    }

    /**
     * Sets the instantiator
     *
     * @param instantiator the instantiator, nonnull
     * @return this builder
     */
    public ConfigurationBuilder<C> instantiator(Instantiator instantiator) {
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
    public ConfigurationBuilder<C> addMigration(Migration<?, C> migration) {
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
    public ConfigurationBuilder<C> addMigrations(List<Migration<?, C>> migrations) {
        this.migrations.addAll(migrations);
        return this;
    }

    /**
     * Builds into a fully fledged configuration
     *
     * @return the configuration
     * @throws DeveloperMistakeException if the combination or usage of different library features is in error
     */
    public Configuration<C> build() {
        LibraryLang libraryLang = LibraryLang.loadLang(locale);
        Definition<C> definition = new DefinitionReader<>(configType).read(typeLiaisons);
        return new BuiltConfig<>(definition, locale, typeLiaisons, keyMapper, instantiator, migrations);
    }
}
