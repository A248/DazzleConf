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
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.internals.AccessChecking;
import space.arim.dazzleconf2.reflect.*;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Collectors;

final class DefinitionScan {

    private final LibraryLang libraryLang;
    private final LiaisonCache liaisonCache;
    private final MethodMirror methodMirror;
    private final Instantiator instantiator;

    private final BlockInfiniteLoop<TypeToken<?>> blockTypeLoop = new BlockInfiniteLoop<>();

    DefinitionScan(LibraryLang libraryLang, LiaisonCache liaisonCache, MethodMirror methodMirror, Instantiator instantiator) {
        this.libraryLang = libraryLang;
        this.liaisonCache = liaisonCache;
        this.methodMirror = methodMirror;
        this.instantiator = instantiator;
    }

    final class Run<V> {

        private final KeyPath.Immut pathPrefix;
        private final TypeToken<V> typeToken;
        private final List<String> labels = new ArrayList<>();
        private final LinkedHashMap<Class<?>, TypeSkeleton> typeSkeletons = new LinkedHashMap<>();

        // Seen before
        private final Set<Class<?>> superTypeSeenBefore = new HashSet<>();
        private final Set<CovariantGuard> covariantSeenBefore = new HashSet<>();

        private Run(KeyPath pathPrefix, TypeToken<V> typeToken) {
            this.pathPrefix = pathPrefix.intoImmut();
            this.typeToken = typeToken;
        }

        private void scanType(MethodMirror.TypeWalker currentWalker, V defaultsProvider) {
            Class<?> currentType = currentWalker.getEnclosingType().rawType();
            // Check if seen before (diamond inheritance)
            if (!superTypeSeenBefore.add(currentType)) {
                return;
            }
            // Check if accessible
            if (!AccessChecking.isAccessible(currentType)) {
                throw new DeveloperMistakeException("Configuration interface not accessible: " + currentType);
            }
            Set<MethodId> callableDefaultMethods = new HashSet<>();
            List<TypeSkeleton.MethodNode<?>> methodNodes = new ArrayList<>();
            MethodMirror.Invoker defaultsInvoker = methodMirror.makeInvoker(defaultsProvider, currentType);

            // To avoid massively increasing the stack depth, skip using the stream itself
            // This is potentially important considering the madness of nested configuration sections
            // Thus, we collect and iterate to reduce stack depth
            for (MethodId methodId : currentWalker.getViableMethods().collect(Collectors.toList())) {

                if (!covariantSeenBefore.add(new CovariantGuard(methodId))) {
                    //
                    // Another method exists with the same name + erased parameters
                    //
                    // If that method is in a subclass, we're already handling it and all is good
                    // If that method is in the same class, someone is using this library with hacked class binaries
                    //
                    // Regardless, doing nothing should be fine. Nobody should be using hacked class binaries. If
                    // they are, they should really check compatibilities with the libraries they're using, especially
                    // libraries which are highly-reflective (seems like a no-brainer to at least read docs)
                    continue;
                }
                AnnotatedElement methodAnnotations = currentWalker.getAnnotations(methodId);
                // Check for @CallableFn
                if (methodAnnotations.getAnnotation(CallableFn.class) != null) {
                    callableDefaultMethods.add(methodId);
                    continue;
                }
                if (methodId.parameterCount() != 0) {
                    throw new DeveloperMistakeException("Configuration method " + methodId + " cannot have parameters");
                }
                // Check for Optional return
                boolean optional = methodId.returnType().rawType().equals(Optional.class);

                // Find the return type, unpacking Optional if necessary
                ReifiedType.Annotated typeRequested;
                if (optional) {
                    typeRequested = methodId.returnType().argumentAt(0);
                } else {
                    typeRequested = methodId.returnType();
                }
                String label = methodId.name();
                LiaisonCache.HandleType<?> handleType;
                try {
                    handleType = liaisonCache.requestToHandle(new TypeToken<>(typeRequested), new AsHandshake(label));
                } catch (DeveloperMistakeException rethrow) {
                    throw new DeveloperMistakeException("Failed to make type agent for " + methodId, rethrow);
                }
                labels.add(label);
                methodNodes.add(handleType.makeMethodNode(
                        methodId, optional, methodAnnotations, defaultsInvoker
                ));
            }
            typeSkeletons.put(currentType, new TypeSkeleton(callableDefaultMethods, methodNodes));

            for (MethodMirror.TypeWalker superType : currentWalker.getSuperTypes()) {
                scanType(superType, defaultsProvider);
            }
        }

        ConfigurationDefinition<V> read() {
            Class<V> rawType = typeToken.getRawType();
            if (!rawType.isInterface()) {
                throw new DeveloperMistakeException("This library works exclusively with interfaces");
            }
            blockTypeLoop.enter(typeToken);
            try {
                scanType(
                        methodMirror.typeWalker(typeToken.getReifiedType()),
                        rawType.cast(instantiator.generateEmpty(rawType.getClassLoader(), rawType))
                );
            } finally {
                blockTypeLoop.exit(typeToken);
            }
            // Top-level comments are unaffected by inheritance
            DataEntry.@NonNull Comments topLevelComments = DataEntry.Comments.buildFrom(
                    rawType.getAnnotationsByType(Comments.class)
            );
            return new Definition<>(
                    typeToken, pathPrefix, topLevelComments, labels, typeSkeletons, libraryLang, methodMirror, instantiator
            );
        }

        final class AsHandshake implements TypeLiaison.Handshake {

            private final String label;
            private final BlockInfiniteLoop<TypeToken<?>> blockRequestLoop = new BlockInfiniteLoop<>();

            AsHandshake(String label) {
                this.label = label;
            }

            @Override
            public @NonNull <U> SerializeDeserialize<U> getOtherSerializer(@NonNull TypeToken<U> other) {
                blockRequestLoop.enter(other);
                try {
                    return liaisonCache.requestToHandle(other, this).serializer;
                } finally {
                    blockRequestLoop.exit(other);
                }
            }

            @Override
            public @NonNull <U> ConfigurationDefinition<U> getConfiguration(@NonNull TypeToken<U> other) {
                KeyPath.Mut subPath = new KeyPath.Mut(pathPrefix);
                subPath.addBack(label);
                return new Run<>(subPath, other).read();
            }
        }
    }

    <V> ConfigurationDefinition<V> read(TypeToken<V> typeToken) {
        return new Run<>(new KeyPath.Immut(), typeToken).read();
    }

    private static final class BlockInfiniteLoop<V> {

        private final Set<V> seenBefore = new HashSet<>();

        void enter(V value) {
            if (!seenBefore.add(value)) {
                throw new IllegalStateException("Cycle detected: " + value);
            }
        }

        void exit(V exitToken) {
            if (!seenBefore.remove(exitToken)) {
                throw new IllegalStateException("Gateway value was never added");
            }
        }

    }

    private static final class CovariantGuard {

        private final MethodId methodId;

        private CovariantGuard(MethodId methodId) {
            this.methodId = methodId;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CovariantGuard)) return false;

            MethodId us = this.methodId;
            MethodId them = ((CovariantGuard) o).methodId;
            if (!us.name().equals(them.name())) {
                return false;
            }
            if (us.parameterCount() != them.parameterCount()) {
                return false;
            }
            for (int n = 0; n < us.parameterCount(); n++) {
                if (!us.parameterAt(n).equals(them.parameterAt(n))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = methodId.name().hashCode();
            result = 31 * result + Arrays.hashCode(methodId.parameters());
            return result;
        }
    }
}
