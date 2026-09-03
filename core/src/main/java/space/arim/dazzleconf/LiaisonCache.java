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

package space.arim.dazzleconf;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.DefaultValues;
import space.arim.dazzleconf.engine.ProtoDefinedNode;
import space.arim.dazzleconf.engine.SerializeDeserialize;
import space.arim.dazzleconf.engine.TranslationResolve;
import space.arim.dazzleconf.engine.TypeLiaison;
import space.arim.dazzleconf.reflect.MethodId;
import space.arim.dazzleconf.reflect.ReflectionProvider;
import space.arim.dazzleconf.reflect.TypeToken;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class LiaisonCache {

    private final Map<TypeToken<?>, HandleType<?>> cachedAgents = new HashMap<>();
    private final TypeLiaison[] typeLiaisons;
    private final TranslationResolve translationResolve;

    LiaisonCache(List<TypeLiaison> typeLiaisons, TranslationResolve translationResolve) {
        this.typeLiaisons = typeLiaisons.toArray(new TypeLiaison[0]);
        this.translationResolve = translationResolve;
    }

    <V> HandleType<V> requestToHandle(TypeToken<V> typeToken, TypeLiaison.Handshake handshake) {
        // Don't use computeIfAbsent - reduce stack depth
        @SuppressWarnings("unchecked")
        HandleType<V> cached = (HandleType<V>) cachedAgents.get(typeToken);
        if (cached != null) {
            return cached;
        }
        // None found, so query the type liaisons. Remember to iterate backward
        for (int n = typeLiaisons.length - 1; n >= 0; n--) {
            TypeLiaison liaison = typeLiaisons[n];
            TypeLiaison.Agent<V> agent = liaison.makeAgent(typeToken, handshake);
            if (agent != null) {
                cached = new HandleType<>(typeToken, agent, agent.makeSerializer());
                cachedAgents.put(typeToken, cached);
                return cached;
            }
        }
        throw new DeveloperMistakeException(
                "Failed to resolve agent for " + typeToken + ". Please add a TypeLiaison that covers this type." +
                        "\nAvailable liaisons: " + Arrays.toString(typeLiaisons) +
                        "\nWays to add new liaisons: ConfigurationBuilder#addTypeLiaisons, ConfigurationBuilder#addSimpleSerializer."
        );
    }

    final class HandleType<V> {

        private final TypeToken<V> typeToken;
        final TypeLiaison.Agent<V> agent;
        final SerializeDeserialize<V> serializer;

        private HandleType(TypeToken<V> typeToken, TypeLiaison.Agent<V> agent, SerializeDeserialize<V> serializer) {
            this.typeToken = typeToken;
            this.agent = agent;
            this.serializer = serializer;
        }

        <C> SkeletonNode.Val<V, C> makeValueNode(
                MethodId methodId, AnnotatedElement annotations, KeyPath.Immut labelPath,
                TypeToken<?> interfaceToken, ReflectionProvider.Invoker<C> defaultsInvoker
        ) {
            class ProtoNodeBase implements ProtoDefinedNode.Value {

                @Override
                public @NonNull TypeToken<?> enclosingType() {
                    return interfaceToken;
                }

                @Override
                public @NonNull AnnotatedElement methodAnnotations() {
                    return annotations;
                }

                @Override
                public @NonNull TranslationResolve translationResolve() {
                    return translationResolve;
                }

                @Override
                public KeyPath.@NonNull Immut labelPath() {
                    return labelPath;
                }

                @Override
                public @NonNull String label() {
                    // If we ever allow custom label settings, need to update LiaisonCache/DefinitionScan
                    return methodId.name();
                }
            }
            class CommentInitImpl extends ProtoNodeBase implements TypeLiaison.CommentInit { }
            class DefaultInitImpl extends ProtoNodeBase implements TypeLiaison.DefaultInit<V> {

                @Override
                public @Nullable DefaultValues<V> methodDefault() {
                    if (!methodId.isDefault()) {
                        return null;
                    }
                    // Try calling the default method
                    Object defaultVal;
                    try {
                        defaultVal = defaultsInvoker.invokeMethod(methodId);
                    } catch (Error ex) {
                        throw ex;
                    } catch (Throwable ex) {
                        throw new DeveloperMistakeException("Default method threw an exception", ex);
                    }
                    if (defaultVal == null) {
                        throw new DeveloperMistakeException("Default method " + methodId + " returned null");
                    }
                    return DefaultValues.simple(typeToken.cast(defaultVal));
                }
            }
            CommentData comments = agent.loadComments(new CommentInitImpl());
            DefaultValues<V> defaultValues = agent.loadDefaultValues(new DefaultInitImpl());
            return new SkeletonNode.Val<>(methodId, comments, defaultValues, serializer);
        }
    }
}
