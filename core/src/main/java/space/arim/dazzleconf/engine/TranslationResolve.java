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

package space.arim.dazzleconf.engine;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.KeyPath;

/**
 * An interface supplied by the library consumer allowing the usage of locale-dependent configuration elements.
 * <h2>Selection and implementation</h2>
 * <p>
 * An instance of this interface is typically tied to a specific locale. The process of resolving translation-dependent
 * values looks like this:
 * <ol>
 *     <li>Find out the server administrator locale (the configuration locale).</li>
 *     <li>Ask the library user for a {@code TranslationResolve} for that locale.</li>
 *     <li>Use the resolver to load different elements of the configuration definition, such as comments and default
 *     values.</li>
 *     <li>Configuration definition is ready and translation resolver is no longer needed.</li>
 * </ol>
 *
 * <h3>Usage</h3>
 * <p>
 * To integrate usage of this API with the broader library, library consumers will typically apply {@link LangDefault}
 * and {@link LangComments}. The keys specified by those annotations will be fed to this interface.
 * <p>
 * For example:
 * <pre>
 *     {@code
 *         public interface Config {
 *             @LangDefault("messages.hello")
 *             String helloMessage();
 *         }
 *
 *         public record MyResolver(Locale locale) implements TranslationResolve {
 *
 *             public CommentData resolveComments(KeyPath key, boolean auto) {
 *                 return null;
 *             }
 *
 *             public @Nullable String resolveStringValue(@NonNull KeyPath valueKey) {
 *                 if (valueKey.toString().equals("messages.hello")) {
 *                     return "translatable";
 *                 }
 *                 return null;
 *             }
 *         }
 *
 *         ConfigurationBuilder<Config> builder;
 *         builder.translationResolve(MyResolver::new);
 *         Configuration<Config> configuration = builder.build();
 *     }
 * </pre>
 *
 */
@API(status = API.Status.EXPERIMENTAL)
public interface TranslationResolve {

    /**
     * A translation resolve that does nothing (returns null every time).
     */
    TranslationResolve DEFAULT = new TranslationResolve() {
        @Override
        public @Nullable CommentData resolveComments(@NonNull KeyPath key, boolean auto) {
            return null;
        }

        @Override
        public @Nullable String resolveStringValue(@NonNull KeyPath valueKey) {
            return null;
        }
    };

    /**
     * Resolves a translation key to comment data.
     * <p>
     * This method is used for translation-dependent comments on configuration methods.
     *
     * @param key  the translation key
     * @param auto if the translation key came from the label path, {@code false} if it came from an explicit key like
     *             that specified in {@link LangComments}
     * @return the comments
     */
    @Nullable CommentData resolveComments(@NonNull KeyPath key, boolean auto);

    /**
     * Resolves a translation key to a string value.
     * <p>
     * This method is used for translation-dependent default values.
     *
     * @param valueKey the translation key
     * @return the string value
     */
    @Nullable String resolveStringValue(@NonNull KeyPath valueKey);

    /**
     * Resolves a translation key to an object value.
     * <p>
     * This method is used for translation-dependent default values.
     * <p>
     * Because of the rarity of non-string translated values, this method is implemented by default to return
     * {@code null}. However, resolvers which supply object values should override this method accordingly.
     *
     * @param valueKey the translation key
     * @return the object value
     */
    default @Nullable Object resolveObjectValue(@NonNull KeyPath valueKey) {
        return null;
    }

}
