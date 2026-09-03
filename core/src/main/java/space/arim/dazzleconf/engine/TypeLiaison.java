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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf.ConfigurationDefinition;
import space.arim.dazzleconf.DeveloperMistakeException;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.reflect.TypeToken;

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
         * Gets default values for a method node.
         * <p>
         * This function is called for the return type of each configuration interface method, on the agent
         * corresponding to that return type.
         * <p>
         * <b>Sources of default values</b>
         * <p>
         * Default values are sometimes extracted from annotations. Method-level annotations are available via the
         * provided {@link DefaultInit#methodAnnotations()}.
         * <p>
         * Additionally, a function can choose when to handle the use of default methods to provide default values. The
         * default implementation of this method calls {@link DefaultInit#methodDefault()} simply.
         *
         * @param defaultInit the init context
         * @return the default values, or null if no defaults are available
         * @throws DeveloperMistakeException if a library usage failure happened
         */
        @SideEffectFree
        default @Nullable DefaultValues<V> loadDefaultValues(@NonNull DefaultInit<V> defaultInit) {
            return defaultInit.methodDefault();
        }

        /**
         * Loads comments on a method node.
         * <p>
         * The default implementation of this method gathers comments using {@link LangComments} and {@link Comments}.
         *
         * @param commentInit the init context
         * @return the comments on a method node
         * @throws DeveloperMistakeException if a library usage failure happened
         */
        default @NonNull CommentData loadComments(@NonNull CommentInit commentInit) {
            DefiningContext.LoadNodeComments loadNodeComments = commentInit.getDefiningInterprocessor()
                    .getHook(DefiningContext.LOAD_NODE_COMMENTS);
            return loadNodeComments.loadComments(commentInit);
        }

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
     * @param <V> the type of the configuration object
     */
    interface DefaultInit<V> extends ProtoDefinedNode.Value {

        /**
         * Extracts default values from the default method if one exists.
         * <p>
         * Returns {@code null} if there is no default method implementation
         *
         * @return the default values from the default method
         * @throws DeveloperMistakeException if the default method is implemented incorrectly. Liaison implementations
         * should not catch this exception but simply let it propagate
         */
        @Nullable DefaultValues<V> methodDefault();

    }

    /**
     * Provides relevant resources for comment initialization
     */
    interface CommentInit extends ProtoDefinedNode.Value {}

    /**
     * Allows an {@link Agent} under construction to access certain resources.
     * <p>
     * A {@code Handshake} is designed to be implemented only by the library. It should not be stored, but rather used
     * only during a call to {@link TypeLiaison#makeAgent(TypeToken, Handshake)}.
     */
    interface Handshake extends DefiningContext {

        /**
         * Gets another agent. This function allows agents to depend on each other.
         * <p>
         * Note: If you only need the serializer, prefer using {@link #getOtherSerializer(TypeToken)}.
         *
         * @param other the type being requested
         * @return an agent for it
         * @param <U> the type requested
         * @throws DeveloperMistakeException if no liaison handles the requested type, or if a cyclic loop is detected
         * with the requested type
         */
        @SideEffectFree
        <U> @NonNull Agent<U> getOtherAgent(@NonNull TypeToken<U> other);

        /**
         * Gets another serializer. This function allows serializers to depend on each other by using instances of
         * other serializers.
         *
         * @param other the type being requested
         * @return a serializer for it
         * @param <U> the type requested
         * @throws DeveloperMistakeException if no liaison handles the requested type, or if a cyclic loop is detected
         * with the requested type
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
         * @throws DeveloperMistakeException if the type requested is improperly declared or has broken settings, or
         * if a cyclic loop is detected with the requested type
         */
        @SideEffectFree
        <U> @NonNull ConfigurationDefinition<U> getConfiguration(@NonNull TypeToken<U> other);

        /**
         * Gets label path at which the agent is being requested.
         * <p>
         * Note that <i>labels</i> are not the same as <i>entry paths</i>. Not all arrangements of agents and liaisons
         * will use separate labels; the label represents a source code location and does <b>not</b> match user
         * configuration data. Think of it as where the agent exists, from the developer's perspective.
         * <p>
         * Usually, it is inappropriate to use the label path to influence the behavior of the agent. An example of
         * acceptable usage would be implementing keyed translations based on the administrator locale (which, in fact,
         * is how the translation feature provided by {@link TranslationResolve} operates).
         *
         * @return the label path
         */
        @Pure
        KeyPath.@NonNull Immut labelPath();

        /**
         * Simulates this handshake existing at a different {@link #labelPath()}.
         * <p>
         * The returned handshake will function identically to this one, but it will report the argument label path.
         * Just like this instance, <b>it should not be stored anywhere</b> but only used for the duration of the call
         * to {@link TypeLiaison#makeAgent(TypeToken, Handshake)}.
         *
         * @param labelPath the new label path
         * @return the new handshake
         */
        @SideEffectFree
        @NonNull Handshake atLabelPath(KeyPath.@NonNull Immut labelPath);

    }
}
