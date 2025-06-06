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
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.engine.Comments;
import space.arim.dazzleconf2.engine.DefaultValues;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.reflect.MethodId;
import space.arim.dazzleconf2.reflect.MethodMirror;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class LiaisonCache {

    private final Map<TypeToken<?>, HandleType<?>> cachedAgents = new HashMap<>();
    private final TypeLiaison[] typeLiaisons;

    LiaisonCache(List<TypeLiaison> typeLiaisons) {
        this.typeLiaisons = typeLiaisons.toArray(new TypeLiaison[0]);
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
                "Failed to resolve agent for " + typeToken + ". Please add a TypeLiaison or serializer for this type."
        );
    }

    static final class HandleType<V> {

        private final TypeToken<V> typeToken;
        private final TypeLiaison.Agent<V> agent;
        final SerializeDeserialize<V> serializer;

        private HandleType(TypeToken<V> typeToken, TypeLiaison.Agent<V> agent, SerializeDeserialize<V> serializer) {
            this.typeToken = typeToken;
            this.agent = agent;
            this.serializer = serializer;
        }

        private DefaultValues<V> makeDefaultValues(MethodId methodId, boolean optional,
                                                   TypeLiaison.DefaultInit defaultInit,
                                                   MethodMirror.Invoker defaultsInvoker) {
            DefaultValues<V> defaultValues = agent.loadDefaultValues(defaultInit);
            if (defaultValues != null) {
                return defaultValues;
            }
            if (!methodId.isDefault()) {
                // No default values here for this entry.
                // Meaning either the developer is a complete novice or an advanced library user
                return null;
            }
            // Let's try calling the default method
            Object defaultVal;
            try {
                defaultVal = defaultsInvoker.invokeMethod(methodId);
            } catch (InvocationTargetException ex) {
                throw new DeveloperMistakeException("Default method threw an exception", ex);
            }
            if (defaultVal == null) {
                throw new DeveloperMistakeException("Default method " + methodId + " returned null");
            }
            // Unpack Optional as needed
            if (optional) {
                Optional<?> optDefaultVal = (Optional<?>) defaultVal;
                if (optDefaultVal.isPresent()) {
                    defaultVal = optDefaultVal.get();
                } else {
                    // That's okay, since optional entries don't need defaults
                    return null;
                }
            }
            return DefaultValues.simple(typeToken.cast(defaultVal));
        }

        TypeSkeleton.MethodNode<V> makeMethodNode(MethodId methodId, boolean optional,
                                                  AnnotatedElement methodAnnotations,
                                                  MethodMirror.Invoker defaultsInvoker) {
            TypeLiaison.DefaultInit defaultInit = new TypeLiaison.DefaultInit() {
                @Override
                public @NonNull TypeToken<?> enclosingType() {
                    return typeToken;
                }

                @Override
                public @NonNull String label() {
                    return methodId.name();
                }

                @Override
                public @NonNull AnnotatedElement methodAnnotations() {
                    return methodAnnotations;
                }
            };
            DefaultValues<V> defaultValues = makeDefaultValues(methodId, optional, defaultInit, defaultsInvoker);
            CommentData comments  = CommentData.buildFrom(methodAnnotations.getAnnotationsByType(Comments.class));
            return new TypeSkeleton.MethodNode<>(comments, optional, methodId, defaultValues, serializer);
        }
    }
}
