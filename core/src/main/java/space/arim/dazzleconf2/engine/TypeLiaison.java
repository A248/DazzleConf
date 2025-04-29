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

package space.arim.dazzleconf2.engine;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf2.ConfigurationDefinition;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.reflect.TypeToken;

/**
 * Powerful handle for working with types, serializing them, and representing them throughout the library structure
 * and configuration construction process.
 * <p>
 * Type liaisons are first contacted when the configuration is constructed, and paired with configuration entries.
 * This act of pairing includes preparing an agent
 */
public interface TypeLiaison {

    /**
     * Attempts to support the following type.
     * <p>
     * If supported, an agent is returned that handles type serialization, deserialization, and defaults. Note that
     * implementations may need casting at the source level to satisfy the generic argument of the token requested.
     *
     * @param <V> the type being requested
     * @param typeToken the type token
     * @param handshake the handshake
     * @return the agent if supported, or null otherwise
     * @throws DeveloperMistakeException if the type has been annotated in a disallowed way for example by specifying
     * contradictory annotations, or if a method on {@code handshake} threw such an exception
     */
    @SideEffectFree
    <V> @Nullable Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake);

    /**
     * An agent that is ready to handle matters relating to a type
     * @param <V> the value type being handled
     */
    interface Agent<V> {

        /**
         * Gets default values. These are often extracted in annotations
         *
         * @param defaultInit the init context
         * @return the default values, or null if no defaults are available
         * @throws DeveloperMistakeException optionally, if a library usage failure happened
         */
        @Nullable DefaultValues<V> loadDefaultValues(@NonNull DefaultInit defaultInit);

        /**
         * Makes a serializer for the type. After the serializer is made, it will be permanently bound to the type
         * and can be re-used in the configuration structure.
         *
         * @return the serializer
         */
        @NonNull SerializeDeserialize<V> makeSerializer();

    }

    /**
     * Provides relevant resources related to default value creation
     *
     */
    interface DefaultInit {

        /**
         * Gets method level annotations for the entry being initialized
         *
         * @return the method level annotations
         */
        @NonNull AnnotationContext methodAnnotations();

    }

    /**
     * Callback interface allowing {@link Agent} to access certain resources
     */
    interface Handshake {

        /**
         * Gets another serializer. This function allows serializers to "depend" on each other by storing instances
         * of other serializers
         *
         * @param other the type being requested
         * @return a serializer for it
         * @param <U> the type requested
         * @throws DeveloperMistakeException if no serializer was configured for the requested type
         * @throws IllegalStateException if a cyclic loop is detected with the other serializer
         */
        <U> @NonNull SerializeDeserialize<U> getOtherSerializer(@NonNull TypeToken<U> other);

        /**
         * Gets another configuration. This function will use the settings from the parent configuration
         * for purposes of defining, deserializing/serializing, and instantiating the child.
         *
         * @return a configuration which can be read or written
         * @throws DeveloperMistakeException if the type requested is improperly declared or has broken settings
         * @throws IllegalStateException if a cyclic loop is detected with the requested type
         */
        <U> @NonNull ConfigurationDefinition<U> getConfiguration(@NonNull TypeToken<U> other);

    }
}
