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

import space.arim.dazzleconf2.ConfigurationReadWrite;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.lang.annotation.Annotation;

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
     */
    <V> Agent<V> makeAgent(TypeToken<V> typeToken, Handshake handshake);

    /**
     * An agent that is ready to handle matters relating to a type
     * @param <V> the value type being handled
     */
    interface Agent<V> {

        /**
         * Gets default values. These are often extracted in annotations
         *
         * @return the default values, or null if no defaults are available
         * @throws DeveloperMistakeException optionally, if a library usage failure happened
         */
        DefaultValues<V> loadDefaultValues();

        /**
         * Makes a serializer for the type. After this serializer is made, it will be permanently bound to the type
         * and can be re-used in the configuration definition.
         *
         * @return the serializer
         */
        SerializeDeserialize<V> makeSerializer();

    }

    /**
     * Callback interface allowing {@link Agent} to access certain resources
     */
    interface Handshake {

        /**
         * Checks available contexts, such as the declaring method, for the following annotation.
         *
         * @param annotationClass the annotation class
         * @return the annotation if present
         * @param <A> the annotation type
         */
        <A extends Annotation> A getAnnotationInContext(Class<A> annotationClass);

        /**
         * Gets another serializer. This function allows serializers to "depend" on each other by storing instances
         * of other serializers
         *
         * @param other the type being requested
         * @return a serializer for it
         * @param <U> the type requested
         * @throws DeveloperMistakeException if no serializer was configured for the requested type
         */
        <U> SerializeDeserialize<U> getOtherSerializer(TypeToken<U> other);

        /**
         * Gets another configuration. This function will use the settings from the parent configuration
         * for purposes of defining, deserializing/serializing, and instantiating the child.
         *
         * @return a configuration which can be read or written
         * @throws DeveloperMistakeException if the type requested is improperly declared or has broken settings
         */
        <U> ConfigurationReadWrite<U> getConfiguration(TypeToken<U> other);

    }
}
