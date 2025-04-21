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
import space.arim.dazzleconf2.engine.*;
import space.arim.dazzleconf2.internals.AccessChecking;
import space.arim.dazzleconf2.reflect.*;
import space.arim.dazzleconf2.internals.LibraryLang;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

final class DefinitionScan {

    private final LibraryLang libraryLang;
    private final LiaisonCache liaisonCache;
    private final MethodMirror methodMirror;
    private final Instantiator instantiator;

    DefinitionScan(LibraryLang libraryLang, LiaisonCache liaisonCache, MethodMirror methodMirror, Instantiator instantiator) {
        this.libraryLang = libraryLang;
        this.liaisonCache = liaisonCache;
        this.methodMirror = methodMirror;
        this.instantiator = instantiator;
    }

    final class Run<V> {

        private final KeyPath pathPrefix;
        private final TypeToken<V> typeToken;
        private final Set<CovariantGuard> covariantSeenBefore = new HashSet<>();
        private final Map<Class<?>, TypeSkeleton> superTypes = new HashMap<>();

        private Run(KeyPath pathPrefix, TypeToken<V> typeToken) {
            this.pathPrefix = pathPrefix;
            this.typeToken = typeToken;
            pathPrefix.lockChanges();
        }

        private void scanType(ReifiedType.Annotated currentType, Object defaultsProvider) {
            Set<MethodId> callableDefaultMethods = new HashSet<>();
            List<TypeSkeleton.MethodNode> methodNodes = new ArrayList<>();
            MethodMirror.Invoker defaultsInvoker = methodMirror.makeInvoker(defaultsProvider, currentType.rawType());

            // To avoid massively increasing the stack depth, skip using the stream itself
            // This is potentially important considering the madness of nested configuration sections
            // Thus, we collect and iterate to reduce stack depth
            for (MethodId methodId : methodMirror.getViableMethods(currentType).collect(Collectors.toList())) {

                if (!covariantSeenBefore.add(new CovariantGuard(methodId))) {
                    //
                    // Another method exists with the same name + erased parameters
                    //
                    // If that method is in a subclass, we're already handling it and all is good
                    // If that method is in the same class, someone is using this library with hacked class binaries
                    //
                    // Regardless, doing nothing should be fine. Nobody should be using hacked class binaries. If
                    // they are, they should check compatibilities with the libraries they're using, especially with
                    // libraries which are highly-reflective like ours (seems like a no-brainer to at least read docs)
                    continue;
                }
                TypeLiaison.AnnotationContext annotationContext = new TypeLiaison.AnnotationContext() {
                    @Override
                    public <A extends Annotation> A getAnnotation(@NonNull Class<A> annotationClass) {
                        return methodMirror.getAnnotation(methodId, currentType, annotationClass);
                    }
                };
                // Check for @CallableFn
                if (annotationContext.getAnnotation(CallableFn.class) != null) {
                    callableDefaultMethods.add(methodId);
                    continue;
                }
                // Check for @Comments
                Comments.Container comments = annotationContext.getAnnotation(Comments.Container.class);

                // Check for Optional return
                boolean optional = methodId.returnType().rawType().equals(Optional.class);

                // Find the return type, unpacking Optional if necessary
                ReifiedType.Annotated typeRequested;
                if (optional) {
                    typeRequested = methodId.returnType().argumentAt(0);
                } else {
                    typeRequested = methodId.returnType();
                }
                // Get DefaultValues + SerializeDeserialize
                LiaisonCache.Cache<?> agentAndSerializer = liaisonCache.requestInfo(
                        new TypeToken<>(typeRequested), new AsHandshake(methodId.name())
                );
                DefaultValues<?> defaultValues = agentAndSerializer.agent.loadDefaultValues(annotationContext);
                if (defaultValues == null) {
                    // Agent gave no defaults -- let's try calling the default method
                    if (methodId.isDefault()) {
                        Object defaultVal;
                        try {
                            defaultVal = defaultsInvoker.invokeMethod(methodId);
                        } catch (InvocationTargetException e) {
                            throw new DeveloperMistakeException("Default method threw an exception", e);
                        }
                        if (defaultVal == null) {
                            throw new DeveloperMistakeException("Default method " + methodId + " returned null");
                        }
                        // Unpack Optional as needed
                        if (optional) {
                            Optional<?> optDefaultVal = (Optional<?>) defaultVal;
                            if (optDefaultVal.isPresent()) {
                                defaultValues = DefaultValues.simple(optDefaultVal.get());
                            }
                            // If empty, that's okay -- optional entries don't need defaults
                        } else {
                            defaultValues = DefaultValues.simple(defaultVal);
                        }
                    } else {
                        // No default values here for this entry.
                        // Meaning either the developer is a complete novice or an advanced library user
                    }
                }
                methodNodes.add(new TypeSkeleton.MethodNode(
                        comments, optional, methodId, defaultValues, agentAndSerializer.serializer
                ));
            }
            superTypes.put(currentType.rawType(), new TypeSkeleton(callableDefaultMethods, methodNodes));

            GenericContext currentGenerics = new GenericContext(currentType);
            for (AnnotatedType superType : currentType.rawType().getAnnotatedInterfaces()) {
                ReifiedType.Annotated reifiedSuperType = currentGenerics.reifyAnnotated(superType);
                scanType(reifiedSuperType, defaultsProvider);
            }
        }

        ConfigurationDefinition<V> read() {
            Class<V> rawType = typeToken.getRawType();
            if (!rawType.isInterface()) {
                throw new DeveloperMistakeException("This library works exclusively with interfaces");
            }
            if (!AccessChecking.isAccessible(rawType)) {
                throw new DeveloperMistakeException("Configuration interface not accessible: " + rawType);
            }
            V defaultsProvider = instantiator.generateEmpty(rawType.getClassLoader(), rawType);
            scanType(typeToken.getReifiedType(), defaultsProvider);
            return new Definition<>(typeToken, pathPrefix, superTypes, libraryLang, methodMirror, instantiator);
        }

        final class AsHandshake implements TypeLiaison.Handshake {

            private final String pathAddition;

            AsHandshake(String pathAddition) {
                this.pathAddition = pathAddition;
            }

            @Override
            public @NonNull <U> SerializeDeserialize<U> getOtherSerializer(@NonNull TypeToken<U> other) {
                return liaisonCache.requestInfo(other, this).serializer;
            }

            @Override
            public @NonNull <U> ConfigurationDefinition<U> getConfiguration(@NonNull TypeToken<U> other) {
                KeyPath subPath = new KeyPath(pathPrefix);
                subPath.addBack(pathAddition);
                return new Run<>(subPath, other).read();
            }
        }
    }

    <V> ConfigurationDefinition<V> read(TypeToken<V> typeToken) {
        return new Run<>(new KeyPath(), typeToken).read();
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
            result = 31 * result + Arrays.hashCode(methodId.parameters());;
            return result;
        }
    }
}
