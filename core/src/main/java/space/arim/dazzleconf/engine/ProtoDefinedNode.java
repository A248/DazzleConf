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

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.reflect.TypeToken;

import java.lang.reflect.AnnotatedElement;

/**
 * Represents a {@link space.arim.dazzleconf.engine.DefinedNode} <i>before</i> it is constructed. Provides details
 * of the method in question.
 */
public interface ProtoDefinedNode {

    /**
     * The interface type where the method is located, after computing inheritance.
     * <p>
     * In particular, this is the configuration interface passed to the library. This is <b>not</b> the source code
     * location, which means that if the configuration type extends one or more parent interfaces, the
     * main interface will always be returned (and never a parent interface).
     *
     * @return the configuration interface type
     */
    @NonNull TypeToken<?> enclosingType();

    /**
     * Gets method level annotations for the entry being initialized
     *
     * @return the method level annotations
     */
    @NonNull AnnotatedElement methodAnnotations();

    /**
     * Gets the translation resolver provided by the library user
     *
     * @return the translation resolve
     */
    @NonNull TranslationResolve translationResolve();

    /**
     * A {@link DefinedNode.Value} before it is constructed.
     *
     */
    interface Value extends ProtoDefinedNode {

        /**
         * The full label path at which the node will exist.
         * <p>
         * The label represents a source code location; it has meaning from the developer's perspective, but not the
         * user's. The label path consists of the sequence of method names leading up to this one.
         *
         * @return the label path, never empty
         */
        KeyPath.@NonNull Immut labelPath();

        /**
         * The immediate label at which the node will exist.
         * <p>
         * The label regards where a method exists, from the developer's perspective. It is backend-independent and
         * logically part of the configuration definition.
         * <p>
         * Using this method is equivalent to {@code this.labelPath().getLeading(KeyPath.SequenceBoundary.BACK)}
         *
         * @return the label
         */
        @NonNull String label();

    }
}
