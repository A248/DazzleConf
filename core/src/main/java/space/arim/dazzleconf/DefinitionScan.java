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
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.CallableFn;
import space.arim.dazzleconf.engine.Comments;
import space.arim.dazzleconf.engine.SerializeDeserialize;
import space.arim.dazzleconf.engine.TypeLiaison;
import space.arim.dazzleconf.internals.lang.LibraryLang;
import space.arim.dazzleconf.reflect.MethodId;
import space.arim.dazzleconf.reflect.ReflectionProvider;
import space.arim.dazzleconf.reflect.ReflectionService;
import space.arim.dazzleconf.reflect.ReifiedType;
import space.arim.dazzleconf.reflect.TypeToken;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class DefinitionScan {

    private final LibraryLang libraryLang;
    private final LiaisonCache liaisonCache;
    private final Reflection reflection;

    private final BlockInfiniteLoop blockReadDefLoop = new BlockInfiniteLoop();

    DefinitionScan(LibraryLang libraryLang, LiaisonCache liaisonCache, Reflection reflection) {
        this.libraryLang = libraryLang;
        this.liaisonCache = liaisonCache;
        this.reflection = reflection;
    }

    static final class Reflection {

        private final ReflectionService reflectionService;
        private final MethodHandles.Lookup lookup;

        Reflection(ReflectionService reflectionService, MethodHandles.Lookup lookup) {
            this.reflectionService = reflectionService;
            this.lookup = lookup;
        }
    }

    final class Run<V> {

        private final KeyPath.Immut labelPath;
        private final TypeToken<V> typeToken;
        private final ReflectionProvider<V> reflectionProvider;
        private final LinkedHashMap<Class<?>, ClassContent> classContentMap = new LinkedHashMap<>();
        private final Map<MethodLocator, ClassContent> methodsFoundWhere = new HashMap<>();

        Run(KeyPath.Immut labelPath, TypeToken<V> typeToken) {
            this.labelPath = labelPath;
            this.typeToken = typeToken;
            reflectionProvider = reflection.reflectionService.newProvider(typeToken.getRawType(), reflection.lookup);
        }

        private ReflectionProvider.TypeWalker[] scanHierarchy(ReflectionProvider.TypeWalker currentWalker) {
            ReifiedType currentReifiedType = currentWalker.getEnclosingType();
            Class<?> currentType = currentReifiedType.rawType();
            // Check if seen before (diamond inheritance)
            if (classContentMap.containsKey(currentType)) {
                return new ReflectionProvider.TypeWalker[0];
            }
            ClassContent classContent = new ClassContent(currentReifiedType);
            classContentMap.put(currentType, classContent);

            // Organize each method: move ownership of it to the deepest declaring subclass
            Consumer<MethodId> forEachOrdered = (methodId) -> {
                MethodLocator methodLocator = new MethodLocator(methodId);
                // We need to make sure that subclasses own the methods they override
                // To do this, check existing types, and see if the current type is a subtype of them
                ClassContent existingOwner = methodsFoundWhere.get(methodLocator);
                Class<?> existingOwningType;
                if (existingOwner == null) {
                    // Completely new method

                } else if ((existingOwningType = existingOwner.reifiedType.rawType()).equals(currentType)) {
                    /*
                    Covariant override (bridge method), even though TypeWalker guarantees not to return them.
                    OR binary hackery with different return types, but same name and parameters.

                    Resolution: Find equal MethodLocator with different MethodId, then throw.
                     */
                    MethodId covariantMethod = existingOwner.ownedMethods.keySet().stream()
                            .filter(locator -> locator.equals(methodLocator))
                            .map(MethodLocator::methodId)
                            .findAny()
                            .orElse(null);
                    throw new IllegalStateException(
                            "Found methods with same name and parameters: " +
                                    "\n  1. " + methodLocator.methodId +
                                    "\n  2. " + covariantMethod +
                                    "\nLocation: " + currentType
                    );
                } else if (existingOwningType.isAssignableFrom(currentType)) {
                    // There's an existing owner, but current type is a subtype of it
                    // So, move ownership to the current type, making sure to use the overriding MethodId
                    existingOwner.ownedMethods.remove(methodLocator);
                    methodsFoundWhere.remove(methodLocator);

                } else if (currentType.isAssignableFrom(existingOwningType)) {
                    // We're currently in a super-type of the existing owner, due to diamond inheritance.
                    return;
                } else {
                    /*
                    Thanks to iteration order (starting from subclasses), we'll never see two methods with the same
                    signature that aren't formally overridden in some way.
                     */
                    throw new IllegalStateException("not possible to see non-overridden methods");
                }
                classContent.ownedMethods.put(methodLocator, currentWalker.getAnnotations(methodId));
                methodsFoundWhere.put(methodLocator, classContent);
            };
            try (Stream<MethodId> methodStream = currentWalker.getViableMethods()) {
                methodStream.forEachOrdered(forEachOrdered);
            }
            return currentWalker.getSuperTypes();
        }

        ConfigurationDefinition<V> read() {
            // 1. Scan type hierarchy; figure out method ownership
            List<ReflectionProvider.TypeWalker> currentWalkers = Collections.singletonList(
                    reflectionProvider.typeWalker(typeToken.getReifiedType())
            );
            while (!currentWalkers.isEmpty()) {
                List<ReflectionProvider.TypeWalker> nextWalkers = new ArrayList<>();
                for (ReflectionProvider.TypeWalker currentWalker : currentWalkers) {
                    nextWalkers.addAll(Arrays.asList(scanHierarchy(currentWalker)));
                }
                currentWalkers = nextWalkers;
            }
            // 2. Make method nodes for the reflective information we just gathered
            List<TypeSkeleton<?>> typeSkeletons = new ArrayList<>(classContentMap.size());
            V defaultsProvider = reflectionProvider.generateEmpty();
            blockReadDefLoop.enter(typeToken);
            try {
                for (ClassContent classContent : classContentMap.values()) {
                    typeSkeletons.add(classContent.makeTypeSkeleton(reflectionProvider, defaultsProvider));
                }
            } finally {
                blockReadDefLoop.exit(typeToken);
            }
            // 3. Extract top-level comments and build final definition
            // TODO: Support translated comments using @LangComment
            CommentData topLevelComments = CommentData.buildFrom(
                    typeToken.getRawType().getAnnotationsByType(Comments.class)
            );
            return new Definition<>(
                    typeToken, topLevelComments, typeSkeletons, libraryLang, reflectionProvider
            );
        }

        private final class ClassContent {

            private final ReifiedType reifiedType;
            private final LinkedHashMap<MethodLocator, AnnotatedElement> ownedMethods = new LinkedHashMap<>();

            ClassContent(ReifiedType reifiedType) {
                this.reifiedType = reifiedType;
            }

            <B> TypeSkeleton<B> makeTypeSkeleton(ReflectionProvider<V> methodMirror, V defaultsProvider) {
                ReflectionProvider.Invoker<B> defaultsInvoker = methodMirror.makeInvoker(
                        defaultsProvider, new TypeToken<>(reifiedType)
                );
                List<SkeletonNode.Val<?, B>> valNodes = new ArrayList<>(ownedMethods.size());
                List<SkeletonNode.Callable<?, B>> callableNodes = new ArrayList<>();

                for (Map.Entry<MethodLocator, AnnotatedElement> ownedMethodEntry : ownedMethods.entrySet()) {
                    MethodId methodId = ownedMethodEntry.getKey().methodId();
                    AnnotatedElement annotations = ownedMethodEntry.getValue();
                    // Check for @CallableFn
                    if (annotations.getAnnotation(CallableFn.class) != null) {
                        if (!methodId.isDefault()) {
                            throw new DeveloperMistakeException(
                                    "Configuration method " + methodId + " is marked with @CallableFn, but it is not a default method."
                            );
                        }
                        callableNodes.add(new SkeletonNode.Callable<>(methodId));
                        continue;
                    }
                    if (methodId.parameterCount() != 0) {
                        throw new DeveloperMistakeException("Configuration method " + methodId + " cannot have parameters");
                    }
                    // If we ever allow custom label settings, need to update LiaisonCache/DefinitionScan
                    KeyPath.Mut labelPathMut = Run.this.labelPath.intoMut();
                    labelPathMut.addBack(methodId.name());
                    KeyPath.Immut labelPath = labelPathMut.intoImmut();

                    TypeToken<?> typeRequested = new TypeToken<>(methodId.returnType());
                    LiaisonCache.HandleType<?> handleType;
                    try {
                        handleType = liaisonCache.requestToHandle(typeRequested, new AsHandshake(labelPath));
                    } catch (DeveloperMistakeException rethrow) {
                        throw new DeveloperMistakeException("Failed to make type agent for " + methodId, rethrow);
                    }
                    valNodes.add(handleType.makeValueNode(methodId, annotations, labelPath, typeToken, defaultsInvoker));
                }
                return new TypeSkeleton<>(new TypeToken<>(reifiedType), valNodes, callableNodes);
            }
        }

        private final class AsHandshake implements TypeLiaison.Handshake {

            private final BlockInfiniteLoop blockRequestLoop = new BlockInfiniteLoop();
            private final KeyPath.Immut labelPath;

            private AsHandshake(KeyPath.Immut labelPath) {
                this.labelPath = labelPath;
            }

            @SideEffectFree
            private <U> LiaisonCache.@NonNull HandleType<U> requestOtherType(@NonNull TypeToken<U> other) {
                blockRequestLoop.enter(other);
                try {
                    return liaisonCache.requestToHandle(other, this);
                } finally {
                    blockRequestLoop.exit(other);
                }
            }

            @Override
            @SideEffectFree
            public <U> TypeLiaison.@NonNull Agent<U> getOtherAgent(@NonNull TypeToken<U> other) {
                return requestOtherType(other).agent;
            }

            @Override
            @SideEffectFree
            public <U> @NonNull SerializeDeserialize<U> getOtherSerializer(@NonNull TypeToken<U> other) {
                return requestOtherType(other).serializer;
            }

            @Override
            @SideEffectFree
            public <U> @NonNull ConfigurationDefinition<U> getConfiguration(@NonNull TypeToken<U> other) {
                return new Run<>(labelPath, other).read();
            }

            @Override
            @Pure
            public KeyPath.@NonNull Immut labelPath() {
                return labelPath;
            }

            @Override
            @SideEffectFree
            public TypeLiaison.@NonNull Handshake atLabelPath(KeyPath.@NonNull Immut labelPath) {
                return new AsHandshake(Objects.requireNonNull(labelPath, "labelPath"));
            }
        }
    }

    private static final class BlockInfiniteLoop {

        private final Set<TypeToken<?>> seenBefore = new LinkedHashSet<>();

        void enter(TypeToken<?> value) {
            if (!seenBefore.add(value)) {
                throw new DeveloperMistakeException(
                        "Cycle detected. This type was requested before: " + value + "." +
                                "\n\nTo prevent circular loops between TypeLiaisons or configuration definitions, " +
                                "refactor your code and extract dependent types. Here is the chain of requests which " +
                                "triggered the cycle:\n" +
                                seenBefore
                );
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

        MethodId methodId() {
            return methodId;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MethodLocator)) return false;

            MethodId theirMethodId = ((MethodLocator) o).methodId;
            if (!methodId.name().equals(theirMethodId.name())) {
                return false;
            }
            if (methodId.parameterCount() != theirMethodId.parameterCount()) {
                return false;
            }
            for (int n = 0; n < methodId.parameterCount(); n++) {
                if (!methodId.parameterAt(n).rawType().equals(theirMethodId.parameterAt(n).rawType())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = methodId.name().hashCode();
            for (int n = 0; n < methodId.parameterCount(); n++) {
                result *= 31;
                result += methodId.parameterAt(n).rawType().hashCode();
            }
            return result;
        }
    }
}
