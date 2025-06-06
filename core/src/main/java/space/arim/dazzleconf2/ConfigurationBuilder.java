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
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.engine.liaison.BooleanLiaison;
import space.arim.dazzleconf2.engine.liaison.ByteLiaison;
import space.arim.dazzleconf2.engine.liaison.CharacterLiaison;
import space.arim.dazzleconf2.engine.liaison.CollectionLiaison;
import space.arim.dazzleconf2.engine.liaison.DoubleLiaison;
import space.arim.dazzleconf2.engine.liaison.EnumLiaison;
import space.arim.dazzleconf2.engine.liaison.FloatLiaison;
import space.arim.dazzleconf2.engine.liaison.IntegerLiaison;
import space.arim.dazzleconf2.engine.liaison.LongLiaison;
import space.arim.dazzleconf2.engine.liaison.ShortLiaison;
import space.arim.dazzleconf2.engine.liaison.SimpleTypeLiaison;
import space.arim.dazzleconf2.engine.liaison.StringLiaison;
import space.arim.dazzleconf2.engine.liaison.SubSection;
import space.arim.dazzleconf2.engine.liaison.SubSectionLiaison;
import space.arim.dazzleconf2.internals.ImmutableCollections;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.migration.Migration;
import space.arim.dazzleconf2.reflect.DefaultInstantiator;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A builder for {@link Configuration}. The builder allows changing how the configuration is defined, read,
 * serialized, and instantiated.
 * <p>
 * <b>Construction</b>
 * <p>
 * A builder can be made either through the factory methods like {@link Configuration#defaultBuilder} or by direct
 * construction. If constructed directly, the builder is empty and type liaisons will need to be added to it.
 * <p>
 * Direct construction can be useful if a clean slate is desired, without the default liaisons.
 * <p>
 * <b>Type Liaisons</b>
 * <p>
 * Several methods add {@link TypeLiaison} instances. This builder stores type liaisons in the order they are added,
 * which is significant because <i>later</i> type liaisons will be queried first in the resulting configuration:<ul>
 * <li>{@link #addTypeLiaisons(TypeLiaison...)}
 * <li>{@link #addTypeLiaisons(List)}
 * <li>{@link #addPrimitiveTypeLiaisons()}
 * <li>{@link #addDefaultTypeLiaisons()}
 * </ul>
 * Callers can add liaisons for any type or types by adding their own {@code TypeLiaison} implementations. Because
 * liaisons added <i>later</i> will take precedence, existing liaisons can be overidden by adding another liaison
 * that covers the same type.
 *
 * @param <C> the configuration type
 */
public final class ConfigurationBuilder<C> {

    private final TypeToken<C> configType;

    // Settings
    private @Nullable Locale locale;
    private final List<TypeLiaison> typeLiaisons = new ArrayList<>();
    private @Nullable KeyMapper keyMapper;
    private @Nullable Instantiator instantiator;
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
    public @This @NonNull ConfigurationBuilder<C> locale(@NonNull Locale locale) {
        this.locale = Objects.requireNonNull(locale, "locale");
        return this;
    }

    /**
     * Adds the following type liaisons to this builder.
     * <p>
     * The order is significant. Type liaisons added <i>later</i> will be queried <i>first</i> to handle configuration
     * types. This matters because it lets the caller override existing liaisons.
     *
     * @param typeLiaisons the type liaisons
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addTypeLiaisons(@NonNull TypeLiaison @NonNull ...typeLiaisons) {
        this.typeLiaisons.addAll(Arrays.asList(typeLiaisons));
        return this;
    }

    /**
     * Adds the following type liaisons to this builder.
     * <p>
     * The order is significant. Type liaisons added <i>later</i> will be queried <i>first</i> to handle configuration
     * types. This matters because it lets the caller override existing liaisons.
     *
     * @param typeLiaisons the type liaisons
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addTypeLiaisons(@NonNull List<@NonNull TypeLiaison> typeLiaisons) {
        this.typeLiaisons.addAll(typeLiaisons);
        return this;
    }

    /**
     * Adds type liaisons for primitives and <code>String</code> to this builder.
     * <p>
     * These type liaisons are part of the default set. However, unlike {@link #addDefaultTypeLiaisons()}, enum types,
     * collections, and configuration subsections are not covered by this method.
     * <p>
     * <b>Types Handled</b>
     * <p>
     * The type liaisons added by this method cover boolean/Boolean, char/Character, byte/Byte, short/Short,
     * int/Integer, long/Long, float/Float, double/Double, and String. Using a boxed type is treated identically to
     * using the primitive type (and nulls are rejected in either case).
     * <p>
     * <b>Notable annotations</b>
     * <p>
     * The liaisons added by this method support the following annotations to modify their behavior:<ul>
     *     <li><code>StringLiaison</code>: <code>@StringDefault</code></li>
     *     <li><code>BooleanLiaison</code>: <code>@BooleanDefault</code></li>
     *     <li><code>LongLiaison</code>: <code>@LongRange</code> and <code>@LongDefault</code></li>
     *     <li><code>IntegerLiaison</code>: <code>@IntegerRange</code> and <code>@IntegerDefault</code></li>
     *     <li><code>ShortLiaison</code>: <code>@ShortRange</code> and <code>@ShortDefault</code></li>
     *     <li><code>ByteLiaison</code>: <code>@ByteRange</code> and <code>@ByteDefault</code></li>
     *     <li><code>DoubleLiaison</code>: <code>@DoubleRange</code> and <code>@DoubleDefault</code></li>
     *     <li><code>FloatLiaison</code>: <code>@FloatRange</code> and <code>@FloatDefault</code></li>
     * </ul>
     * <p>The "Range" annotations for numeric types provide bounds checking for a specified range. If the user input
     * falls outside that range, it is rejected with an error message.
     * <p>The "Default" annotations provide default values. There is mostly no difference between using default methods
     * and the default value-providing annotations, but the annotations provide additional capabilities, like specifying
     * an "if missing" value or being passed to dependent liaisons.
     * <p>
     * The annotations mentioned here can also be depended upon by other liaisons, not just the default liaisons.
     *
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addPrimitiveTypeLiaisons() {
        return addTypeLiaisons(
                new CharacterLiaison(), new FloatLiaison(),  new DoubleLiaison(), new BooleanLiaison(),
                new ByteLiaison(), new ShortLiaison(), new IntegerLiaison(), new LongLiaison(), new StringLiaison()
        );
    }

    /**
     * Adds the default type liaisons to this builder.
     * <p>
     * This method is called automatically if you are using <code>Configuration.defaultBuilder</code>.
     * <p>
     * <b>Types Handled</b>
     * <p>
     * The default type liaisons are capable of serializing primitive types, <code>String</code>s, enum types,
     * collections (<code>Collection</code>/<code>List</code>/<code>Set</code>), and configuration subsections.
     * <p>
     * The full list of types handled by this method: boolean/Boolean, char/Character, byte/Byte, short/Short,
     * int/Integer, long/Long, float/Float, double/Double, String, types for which <code>Class#isEnum</code> is true,
     * Collection, List, Set, and interface types where the type usage is annotated with {@link SubSection}.
     * <p>
     * <b>Notable annotations</b>
     * <p>
     * The default liaisons support the following annotations to modify their behavior:<ul>
     *     <li><code>StringLiaison</code>: <code>@StringDefault</code></li>
     *     <li><code>BooleanLiaison</code>: <code>@BooleanDefault</code></li>
     *     <li><code>LongLiaison</code>: <code>@LongRange</code> and <code>@LongDefault</code></li>
     *     <li><code>IntegerLiaison</code>: <code>@IntegerRange</code> and <code>@IntegerDefault</code></li>
     *     <li><code>ShortLiaison</code>: <code>@ShortRange</code> and <code>@ShortDefault</code></li>
     *     <li><code>ByteLiaison</code>: <code>@ByteRange</code> and <code>@ByteDefault</code></li>
     *     <li><code>DoubleLiaison</code>: <code>@DoubleRange</code> and <code>@DoubleDefault</code></li>
     *     <li><code>FloatLiaison</code>: <code>@FloatRange</code> and <code>@FloatDefault</code></li>
     * </ul>
     * <p>The "Range" annotations for numeric types provide bounds checking for a specified range. If the user input
     * falls outside that range, it is rejected with an error message.
     * <p>The "Default" annotations provide default values. There is mostly no difference between using default methods
     * and the default value-providing annotations, but the annotations provide additional capabilities, like specifying
     * an "if missing" value or being passed to dependent liaisons.
     * <p>
     * The annotations mentioned here can also be depended upon by other liaisons, not just the default liaisons.
     *
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addDefaultTypeLiaisons() {
        return addPrimitiveTypeLiaisons().addTypeLiaisons(new CollectionLiaison(), new EnumLiaison(), new SubSectionLiaison());
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
     * @param typeToken the type to handle. Annotations will be matched <i>exactly</i>, meaning that the provided
     *                  serializer will <i>NOT</i> apply to usage of the type with different annotations
     * @param serializeDeserialize the serialization for that type
     * @return this builder
     * @param <V> the type being handled by the serializer
     */
    public <V> @This @NonNull ConfigurationBuilder<C> addSimpleSerializer(@NonNull TypeToken<V> typeToken,
                                                                          @NonNull SerializeDeserialize<V> serializeDeserialize) {
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
    public @This @NonNull ConfigurationBuilder<C> keyMapper(@Nullable KeyMapper keyMapper) {
        this.keyMapper = keyMapper;
        return this;
    }

    /**
     * Sets the instantiator
     *
     * @param instantiator the instantiator
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> instantiator(@NonNull Instantiator instantiator) {
        this.instantiator = Objects.requireNonNull(instantiator);
        return this;
    }

    /**
     * Adds the following migration to this builder.
     * <p>
     * <b>Migrations are not trivial.</b> By using this method, you affirm that you have read the package javadoc for
     * {@link space.arim.dazzleconf2.migration}, and you understand the "perpetual migration" trap.
     * <p>
     * The order is significant, because migrations are checked in the order in which they are declared. Thus,
     * migrations that come first need to ensure they aren't wrongly handling different or overlapping versions.
     *
     * @param migration the migration
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addMigration(@NonNull Migration<?, C> migration) {
        this.migrations.add(Objects.requireNonNull(migration));
        return this;
    }

    /**
     * Adds the following migrations to this builder.
     * <p>
     * <b>Migrations are not trivial.</b> By using this method, you affirm that you have read the package javadoc for
     * {@link space.arim.dazzleconf2.migration}, and you understand the "perpetual migration" trap.
     * <p>
     * The order is significant, because migrations are checked in the order in which they are declared. Thus,
     * migrations that come first need to ensure they aren't wrongly handling different or overlapping versions.
     *
     * @param migrations the migrations
     * @return this builder
     */
    public @This @NonNull ConfigurationBuilder<C> addMigrations(@NonNull List<@NonNull Migration<?, C>> migrations) {
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
        Instantiator instantiator = (this.instantiator == null) ?
                new DefaultInstantiator(configType.getRawType().getClassLoader()) : this.instantiator;
        List<Migration<?, C>> migrations = ImmutableCollections.listOf(this.migrations);

        // Load language
        LibraryLang libraryLang = LibraryLang.loadLang(locale);

        // Scan and build definition
        ConfigurationDefinition<C> definition = new DefinitionScan(
                libraryLang, new LiaisonCache(typeLiaisons), instantiator
        ).read(configType);

        // Yield final
        return new BuiltConfig<>(definition, locale, libraryLang, typeLiaisons, keyMapper, migrations);
    }
}
