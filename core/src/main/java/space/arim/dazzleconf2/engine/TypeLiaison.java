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

import java.lang.reflect.AnnotatedElement;
import java.util.function.Supplier;

/**
 * Powerful handle for working with types, serializing them, and representing them throughout the library structure
 * and configuration construction process.
 * <p>
 * Type liaisons are first contacted when the configuration is constructed, and paired with configuration entries.
 * This act of pairing includes making the agent, associating the type with its serializer, and quering default values
 * whenever the type is used as a return value.
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
         * Gets default values.
         * <p>
         * This function is called for each return type of a configuration interface method, on the agent corresponding
         * to that return type.
         * <p>
         * Default values are often extracted from annotations. Method-level annotations are available via the provided
         * {@link DefaultInit#methodAnnotations()}.
         * <p>
         * Note that this function does <b>NOT</b> handle the use of default methods to provide default values. This
         * function provides default values before a default method can possibly do so; this function returning a
         * non-null value may conflict with, or override, the result of the default method if one exists.
         *
         * @param defaultInit the init context
         * @return the default values, or null if no defaults are available
         * @throws DeveloperMistakeException optionally, if a library usage failure happened
         */
        @SideEffectFree
        @Nullable DefaultValues<V> loadDefaultValues(@NonNull DefaultInit defaultInit);

        /**
         * Makes a serializer for the type. After the serializer is made, it will be permanently bound to the type
         * and can be re-used in the configuration structure.
         *
         * @return the serializer
         */
        @SideEffectFree
        @NonNull SerializeDeserialize<V> makeSerializer();

        /**
         * A convenience API for cast-free, generic-safe agent creation, given raw type {@code V}.
         * <p>
         * If the provided {@code token} matches the given {@code matchWith} in raw type, then the factory is queried
         * to return an agent. If the raw type does not match, {@code null} is returned.
         * <p>
         * Because Java does not have a good way to match on generic parameters, this function provides a helpful
         * workaround.
         *
         * @param token the token for whom an agent might be provided for; annotations on this token are ignored
         * @param matchWith the raw type to check if the token matches
         * @param factory the supplier to make the agent assuming the token matches the given raw type
         * @return the created agent if successful, or null if the token has a diffferent type
         * @param <V> the token type
         * @param <R> the raw type to look for
         */
        @SuppressWarnings("unchecked")
        @SideEffectFree
        static <V, R> @Nullable Agent<R> matchOnToken(@NonNull TypeToken<R> token, @NonNull Class<V> matchWith,
                                                      @NonNull Supplier<@NonNull Agent<V>> factory) {
            if (token.getRawType().equals(matchWith)) {
                // Success!
                return (Agent<R>) factory.get();
            }
            return null;
        }
    }

    /**
     * Provides relevant resources related to default value creation
     *
     */
    interface DefaultInit {

        /**
         * The interface type where the method is located
         *
         * @return the interface type
         */
        @NonNull TypeToken<?> enclosingType();

        /**
         * The label for the entry, typically the method name
         *
         * @return the label
         */
        @NonNull String label();

        /**
         * Gets method level annotations for the entry being initialized
         *
         * @return the method level annotations
         */
        @NonNull AnnotatedElement methodAnnotations();

    }

    /**
     * Callback interface allowing {@link Agent} to access certain resources
     */
    interface Handshake {

        /**
         * Gets another serializer. This function allows serializers to "depend" on each other by using instances of
         * other serializers.
         *
         * @param other the type being requested
         * @return a serializer for it
         * @param <U> the type requested
         * @throws DeveloperMistakeException if no liaison handles the requested type
         * @throws IllegalStateException if a cyclic loop is detected with the other liaison
         */
        @SideEffectFree
        <U> @NonNull SerializeDeserialize<U> getOtherSerializer(@NonNull TypeToken<U> other);

        /**
         * Gets another configuration. This function will use the settings from the parent configuration
         * for purposes of defining, deserializing/serializing, and instantiating the child.
         *
         * @param other the type whose definition is requested
         * @return a configuration which can be read or written
         * @param <U> the type requested
         * @throws DeveloperMistakeException if the type requested is improperly declared or has broken settings
         * @throws IllegalStateException if a cyclic loop is detected with the requested type
         */
        @SideEffectFree
        <U> @NonNull ConfigurationDefinition<U> getConfiguration(@NonNull TypeToken<U> other);

    }
}
