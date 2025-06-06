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

package space.arim.dazzleconf2.reflect;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.DeveloperMistakeException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.stream.Stream;

import static space.arim.dazzleconf2.reflect.ReifiedType.Annotated.EMPTY_ARRAY;

/**
 * Default implementation of {@link MethodMirror} using standard reflection.
 *
 */
final class DefaultMethodMirror implements MethodMirror {

    private final MethodHandles.Lookup lookup;

    DefaultMethodMirror(MethodHandles.Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public @NonNull TypeWalker typeWalker(ReifiedType.@NonNull Annotated reifiedType) {
        return new Walker(reifiedType);
    }

    private static final class Walker implements TypeWalker {

        private final ReifiedType.Annotated enclosingType;
        private final GenericContext classGenerics;

        private Walker(ReifiedType.Annotated enclosingType) {
            this.enclosingType = enclosingType;
            this.classGenerics = new GenericContext(enclosingType) {

                @Override
                ReifiedType.Annotated unknownVariable(String varName) {
                    // Method-level type variables can be replaced by unannotated Object
                    return ReifiedType.Annotated.unannotated(Object.class);
                }
            };
        }

        @Override
        public ReifiedType.@NonNull Annotated getEnclosingType() {
            return enclosingType;
        }

        @Override
        public @NonNull Stream<@NonNull MethodId> getViableMethods() {
            Class<?> declaringClass = enclosingType.rawType();
            Method[] classMethods = declaringClass.getDeclaredMethods();
            return Arrays.stream(classMethods)
                    .filter(method -> !method.isSynthetic() && !method.isBridge())
                    .map((method -> {
                        ReifiedType.Annotated reifiedReturn = classGenerics.reify(method.getAnnotatedReturnType());

                        AnnotatedType[] methodParameters = method.getAnnotatedParameterTypes();
                        ReifiedType[] reifiedParameters = (methodParameters.length == 0) ?
                                EMPTY_ARRAY : new ReifiedType[methodParameters.length];
                        for (int n = 0; n < reifiedParameters.length; n++) {
                            reifiedParameters[n] = classGenerics.reify(methodParameters[n]);
                        }
                        MethodId methodId = new MethodId(
                                method.getName(), reifiedReturn, reifiedParameters, method.isDefault()
                        );
                        return methodId.withOpaqueCache(new MethodCache(method));
                    }));
        }

        @Override
        public @NonNull AnnotatedElement getAnnotations(@NonNull MethodId methodId) {
            MethodId.OpaqueCache methodCache = methodId.getOpaqueCache();
            assert methodCache instanceof MethodCache;
            return ((MethodCache) methodCache).method;
        }

        @Override
        public @NonNull TypeWalker @NonNull [] getSuperTypes() {
            AnnotatedType[] annotatedInterfaces = enclosingType.rawType().getAnnotatedInterfaces();
            TypeWalker[] superTypes = new TypeWalker[annotatedInterfaces.length];
            for (int n = 0; n < annotatedInterfaces.length; n++) {
                superTypes[n] = new Walker(classGenerics.reify(annotatedInterfaces[n]));
            }
            return superTypes;
        }
    }

    @Override
    public @NonNull Invoker makeInvoker(@NonNull Object receiver, @NonNull Class<?> enclosingType) {
        ProxyHandler<?> proxyHandler = null;
        if (Proxy.isProxyClass(receiver.getClass())) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(receiver);
            if (invocationHandler instanceof ProxyHandler) {
                proxyHandler = (ProxyHandler<?>) invocationHandler;
            }
        }
        return new Invoke(receiver, proxyHandler);
    }

    private final class Invoke implements Invoker {

        private final Object receiver;
        /** Nonnull if receiver is a proxy */
        private final ProxyHandler<?> proxyHandler;

        private Invoke(Object receiver, ProxyHandler<?> proxyHandler) {
            this.receiver = receiver;
            this.proxyHandler = proxyHandler;
        }

        @Override
        public @Nullable Object invokeMethod(@NonNull MethodId methodId, @Nullable Object @Nullable ... arguments)
                throws InvocationTargetException {

            // Try the fast path - works in many cases
            if (proxyHandler != null && methodId.parameterCount() == 0) {
                Object fastPath = proxyHandler.fastPathNoParams(methodId.name());
                if (fastPath != null) {
                    return fastPath;
                }
            }
            // Get the method, as we need it either way
            MethodId.OpaqueCache methodCache = methodId.getOpaqueCache();
            // We rely on the method cache - it gives us the real java.lang.reflect.Method for the MethodId
            // Reconstructing the Method is actually not possible, because MethodId reifies generic variables
            assert methodCache instanceof MethodCache;
            Method method = ((MethodCache) methodCache).method;

            // Either call the proxy directly, or fall-back to standard reflection
            if (proxyHandler != null) {
                try {
                    return proxyHandler.implInvoke(method, arguments);
                } catch (Throwable ex) {
                    throw new InvocationTargetException(ex);
                }
            }
            // Use the provided `lookup` to call the method
            MethodHandle methodHandle;
            try {
                methodHandle = lookup.unreflect(method);
            } catch (IllegalAccessException ex) {
                throw new DeveloperMistakeException("Configuration method inaccessible", ex);
            }
            try {
                return methodHandle.bindTo(receiver).invokeWithArguments(arguments);
            } catch (Throwable ex) {
                throw new InvocationTargetException(ex);
            }
        }
    }
}
