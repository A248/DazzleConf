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
import space.arim.dazzleconf.backend.KeyPath;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enables language-dependent string-based default values.
 * <p>
 * This annotation, if found, is supported by the default string liaison. Custom liaisons may also query its existence.
 * According to the convention recommended for liaisons, however, this annotation will not have an effect if the
 * translation key specified by {@link #value()} does not have a corresponding resolved value.
 * <h2>Usage</h2>
 * <p>
 * To use this annotation in conjunction with other library features, library users need to implement and provide a
 * {@link TranslationResolve}. Please see that interface for an overview of the translation mechanism.
 * <p>
 * If this annotation and other default value annotations are both present (e.g. {@link space.arim.dazzleconf.engine.liaison.StringDefault}),
 * then liaisons are encouraged to use the following strategy:
 * <ol>
 *     <li>Check for this annotation, and if the translation key resolves, use the translated value.</li>
 *     <li>Otherwise, use non-locale dependent strategies for obtaining a default value.</li>
 * </ol>
 * This approach maintains the property that, if a translated value is not available for the key, the method acts
 * equivalent as if this annotation did not exist.
 */
@API(status = API.Status.EXPERIMENTAL)
@Retention(RUNTIME)
@Target(METHOD)
public @interface LangDefault {

    /**
     * Defines the translation key used to look up the default value.
     * <p>
     * The key is sent to {@link TranslationResolve#resolveStringValue(KeyPath)} to find the default. If that method
     * does not return anything, liaisons will treat this annotation as if it were not present.
     *
     * @return the translation key
     */
    String value();

}
