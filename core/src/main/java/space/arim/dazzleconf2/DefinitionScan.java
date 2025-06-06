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
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.engine.CallableFn;
import space.arim.dazzleconf2.engine.Comments;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.internals.AccessChecking;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.reflect.Instantiator;
import space.arim.dazzleconf2.reflect.MethodId;
import space.arim.dazzleconf2.reflect.MethodMirror;
import space.arim.dazzleconf2.reflect.ReifiedType;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class DefinitionScan {

    private final LibraryLang libraryLang;
    private final LiaisonCache liaisonCache;
    private final Instantiator instantiator;
    private final MethodMirror methodMirror;

    private final BlockInfiniteLoop blockReadDefLoop = new BlockInfiniteLoop();

    DefinitionScan(LibraryLang libraryLang, LiaisonCache liaisonCache, Instantiator instantiator, MethodMirror methodMirror) {
        this.libraryLang = libraryLang;
        this.liaisonCache = liaisonCache;
        this.instantiator = instantiator;
        this.methodMirror = methodMirror;
    }

    final class Run<V> {

        private final TypeToken<V> typeToken;
        private final LinkedHashMap<Class<?>, ClassContent> classContentMap = new LinkedHashMap<>();
        private final Map<MethodLocator, Class<?>> methodsFoundWhere = new HashMap<>();

        Run(TypeToken<V> typeToken) {
            this.typeToken = typeToken;
        }

        private MethodMirror.TypeWalker[] scanHierarchy(MethodMirror.TypeWalker currentWalker) {
            Class<?> currentType = currentWalker.getEnclosingType().rawType();
            // Check if seen before (diamond inheritance)
            if (classContentMap.containsKey(currentType)) {
                return new MethodMirror.TypeWalker[0];
            }
            // Check if accessible
            if (false && !AccessChecking.isAccessible(currentType)) {
                throw new DeveloperMistakeException("Configuration interface not accessible: " + currentType);
            }
            ClassContent classContent = new ClassContent();
            classContentMap.put(currentType, classContent);

            // Organize each method: move ownership of it to the deepest declaring subclass
            currentWalker.getViableMethods().forEachOrdered(methodId -> {

                MethodLocator methodLocator = new MethodLocator(methodId);
                // We need to make sure that subclasses own the methods they override
                // To do this, check existing types, and see if the current type is a sub-type of them
                Class<?> existingOwner = methodsFoundWhere.get(methodLocator);
                if (existingOwner == null) {
                    // All good
                } else if (existingOwner.isAssignableFrom(currentType)) {
                    // There's an existing owner, but current type is a sub-type of it
                    // So, move ownership of this method to the current type
                    classContentMap.get(existingOwner).ownedMethods.remove(methodLocator);
                } else {
                    // We're currently in a super-type of the existing owner, due to diamond inheritance
                    // In that case, keep ownership of the method with them
                    assert currentType.isAssignableFrom(existingOwner);
                    return;
                }
                classContent.ownedMethods.put(methodLocator, currentWalker.getAnnotations(methodId));
                methodsFoundWhere.put(methodLocator, currentType);
            });
            return currentWalker.getSuperTypes();
        }

        ConfigurationDefinition<V> read() {
            Class<V> rawType = typeToken.getRawType();
            if (!rawType.isInterface()) {
                throw new DeveloperMistakeException(
                        "This library works exclusively with interfaces. " + rawType + " is not an interface."
                );
            }
            // 1. Scan type hierarchy; figure out method ownership
            List<MethodMirror.TypeWalker> currentWalkers = Collections.singletonList(
                    methodMirror.typeWalker(typeToken.getReifiedType())
            );
            while (!currentWalkers.isEmpty()) {
                List<MethodMirror.TypeWalker> nextWalkers = new ArrayList<>();
                for (MethodMirror.TypeWalker currentWalker : currentWalkers) {
                    nextWalkers.addAll(Arrays.asList(scanHierarchy(currentWalker)));
                }
                currentWalkers = nextWalkers;
            }
            // 2. Make method nodes for the reflective information we just gathered
            LinkedHashMap<Class<?>, TypeSkeleton> typeSkeletons = new LinkedHashMap<>();
            blockReadDefLoop.enter(typeToken);
            try {
                V defaultsProvider = instantiator.generateEmpty(rawType);
                for (Map.Entry<Class<?>, ClassContent> classContentEntry : classContentMap.entrySet()) {
                    Class<?> enclosingClass = classContentEntry.getKey();
                    ClassContent classContent = classContentEntry.getValue();
                    // Add type skeleton
                    typeSkeletons.put(
                            enclosingClass,
                            classContent.makeTypeSkeleton(methodMirror.makeInvoker(defaultsProvider, enclosingClass))
                    );
                }
            } finally {
                blockReadDefLoop.exit(typeToken);
            }
            // 3. Extract top-level comments and build final definition
            CommentData topLevelComments = CommentData.buildFrom(rawType.getAnnotationsByType(Comments.class));
            return new Definition<>(
                    typeToken, topLevelComments, typeSkeletons, libraryLang, instantiator, methodMirror
            );
        }

        private final class ClassContent {

            private final LinkedHashMap<MethodLocator, AnnotatedElement> ownedMethods = new LinkedHashMap<>();

            TypeSkeleton makeTypeSkeleton(MethodMirror.Invoker defaultsInvoker) {

                Set<MethodId> callableDefaultMethods = new HashSet<>();
                List<TypeSkeleton.MethodNode<?>> methodNodes = new ArrayList<>(ownedMethods.size());

                for (Map.Entry<MethodLocator, AnnotatedElement> ownedMethodEntry : ownedMethods.entrySet()) {
                    // Extract starter data
                    MethodId methodId = ownedMethodEntry.getKey().methodId;
                    AnnotatedElement methodAnnotations = ownedMethodEntry.getValue();

                    // Check for @CallableFn
                    if (methodAnnotations.getAnnotation(CallableFn.class) != null) {
                        if (!methodId.isDefault()) {
                            throw new DeveloperMistakeException(
                                    "Configuration method " + methodId + " is marked with @CallableFn, but it is not a default method."
                            );
                        }
                        callableDefaultMethods.add(methodId);
                        continue;
                    }
                    if (methodId.parameterCount() != 0) {
                        throw new DeveloperMistakeException("Configuration method " + methodId + " cannot have parameters");
                    }
                    // Check for Optional return
                    boolean optional = methodId.returnType().rawType().equals(Optional.class);

                    // Find the target type, unpacking Optional if necessary
                    ReifiedType.Annotated typeRequested;
                    if (optional) {
                        typeRequested = methodId.returnType().argumentAt(0);
                    } else {
                        typeRequested = methodId.returnType();
                    }
                    LiaisonCache.HandleType<?> handleType;
                    try {
                        handleType = liaisonCache.requestToHandle(new TypeToken<>(typeRequested), new AsHandshake());
                    } catch (DeveloperMistakeException rethrow) {
                        throw new DeveloperMistakeException("Failed to make type agent for " + methodId, rethrow);
                    }
                    methodNodes.add(handleType.makeMethodNode(methodId, optional, methodAnnotations, defaultsInvoker));
                }
                return new TypeSkeleton(callableDefaultMethods, methodNodes);
            }
        }

        private final class AsHandshake implements TypeLiaison.Handshake {

            private final BlockInfiniteLoop blockRequestLoop = new BlockInfiniteLoop();

            @Override
            @SideEffectFree
            public @NonNull <U> SerializeDeserialize<U> getOtherSerializer(@NonNull TypeToken<U> other) {
                blockRequestLoop.enter(other);
                try {
                    return liaisonCache.requestToHandle(other, this).serializer;
                } finally {
                    blockRequestLoop.exit(other);
                }
            }

            @Override
            @SideEffectFree
            public @NonNull <U> ConfigurationDefinition<U> getConfiguration(@NonNull TypeToken<U> other) {
                return new Run<>(other).read();
            }
        }
    }

    private static final class BlockInfiniteLoop {

        private final Set<TypeToken<?>> seenBefore = new HashSet<>();

        void enter(TypeToken<?> value) {
            if (!seenBefore.add(value)) {
                throw new DeveloperMistakeException("Cycle detected. This type was requested before: " + value);
            }
        }

        void exit(TypeToken<?> exitToken) {
            if (!seenBefore.remove(exitToken)) {
                throw new IllegalStateException("Gateway value was never added");
            }
        }

    }

    private static final class MethodLocator {

        private final MethodId methodId;

        private MethodLocator(MethodId methodId) {
            this.methodId = methodId;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MethodLocator)) return false;

            MethodId us = this.methodId;
            MethodId them = ((MethodLocator) o).methodId;
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
