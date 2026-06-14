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

package space.arim.dazzleconf.reflect;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf.DeveloperMistakeException;
import space.arim.dazzleconf.ReloadShell;
import space.arim.dazzleconf.internals.MethodUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static space.arim.dazzleconf.reflect.ReifiedType.EMPTY_ARRAY;

final class DefaultReflectionProvider<I> implements ReflectionProvider<I> {

    private final Class<I> iface;
    private final MethodHandles.Lookup lookup;
    private final ReflectHandleCache reflectHandleCache = new ReflectHandleCache();

    static final MethodType IMPL_METHOD_SIGNATURE = MethodType.methodType(Object.class, new Class[] {Object.class, Object[].class});

    DefaultReflectionProvider(Class<I> iface, MethodHandles.Lookup lookup) {
        this.iface = iface;
        this.lookup = lookup;
    }

    static Method getMethodFromCache(MethodId methodId) {
        MethodId.OpaqueCache methodCache = methodId.getOpaqueCache();
        if (!(methodCache instanceof MethodCache)) {
            throw new DeveloperMistakeException("Method ID not from this implementation " + methodId);
        }
        return ((MethodCache) methodCache).method;
    }

    static final class MethodCache implements MethodId.OpaqueCache {

        private final Method method;

        MethodCache(Method method) {
            this.method = method;
        }
    }

    @Override
    public @NonNull MethodYield newMethodYield() {
        return new DefaultMethodYield(this);
    }

    private @NonNull I produceProxy(ProxyHandler<I> proxyHandler) {
        assert proxyHandler.iface.equals(iface) : "producing other type?";
        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), new Class[] {iface}, proxyHandler));
    }

    @Override
    public @NonNull I generate(@NonNull MethodYield methodYield) {
        DefaultMethodYield yield = (DefaultMethodYield) methodYield;
        return produceProxy(yield.intoProxyHandler(iface));
    }

    @Override
    public @NonNull ReloadShell<I> generateShell() {
        ProxyHandlerToDelegate<I> proxyHandler = new ProxyHandlerToDelegate<>(iface, lookup, reflectHandleCache);
        I shell = produceProxy(proxyHandler);
        return proxyHandler.new AsReloadShell(shell);
    }

    @Override
    public @NonNull I generateEmpty() {
        Map<Method, MethodHandle> defaultMethods = new HashMap<>();
        for (Method method : iface.getMethods()) {
            if (method.isDefault()) {
                defaultMethods.put(method, genDefaultHandle(method));
            }
        }
        return produceProxy(new ProxyHandlerToEmpty<>(iface, defaultMethods));
    }

    MethodHandle genDefaultHandle(Method method) {
        MethodHandle methodHandle;
        try {
            methodHandle = MethodUtil.createDefaultMethodHandle(method, lookup);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            String className = method.getDeclaringClass().getName();
            throw new DeveloperMistakeException(
                    "Unable to generate method accessor for " + className + '#' + method.getName(),
                    ex
            );
        }
        // Want to invoke with Object[] (like invokeWithArguments, but prepare handle up-front)
        methodHandle = methodHandle.asSpreader(Object[].class, method.getParameterCount());
        methodHandle = methodHandle.asType(IMPL_METHOD_SIGNATURE);
        return methodHandle;
    }

    @Override
    public @NonNull TypeWalker typeWalker(@NonNull ReifiedType reifiedType) {
        if (!reifiedType.rawType().equals(iface)) {
            throw new IllegalArgumentException("Mismatched type: " + reifiedType);
        }
        // We rely on returning only interface super types in Walker#getSuperTypes
        assert reifiedType.rawType().isInterface();
        return new Walker(reifiedType);
    }

    private static final class Walker implements TypeWalker {

        private final ReifiedType enclosingType;
        private final GenericCompute classGenerics;

        private Walker(ReifiedType enclosingType) {
            this.enclosingType = enclosingType;
            this.classGenerics = new GenericCompute(new GenericContext.OfType(
                    enclosingType,
                    // Method-level generic variables are not supported
                    (methodLevelTypeVar) -> ReifiedType.rawUnannotated(Object.class)
            ));
        }

        @Override
        public @NonNull ReifiedType getEnclosingType() {
            return enclosingType;
        }

        @Override
        public @NonNull Stream<@NonNull MethodId> getViableMethods() {
            return Arrays.stream(enclosingType.rawType().getDeclaredMethods())
                    .filter(method -> !Modifier.isStatic(method.getModifiers()) && !method.isBridge())
                    .map(method -> {
                        ReifiedType reifiedReturn = classGenerics.reify(method.getAnnotatedReturnType());

                        AnnotatedType[] methodParameters = method.getAnnotatedParameterTypes();
                        ReifiedType[] reifiedParameters = (methodParameters.length == 0) ?
                                EMPTY_ARRAY : new ReifiedType[methodParameters.length];
                        for (int n = 0; n < reifiedParameters.length; n++) {
                            reifiedParameters[n] = classGenerics.reify(methodParameters[n]);
                        }
                        MethodId methodId = new MethodId(
                                method.getName(), reifiedReturn, reifiedParameters, method.isDefault()
                        );
                        return methodId.withOpaqueCache(new DefaultReflectionProvider.MethodCache(method));
                    });
        }

        @Override
        public @NonNull AnnotatedElement getAnnotations(@NonNull MethodId methodId) {
            Method method = DefaultReflectionProvider.getMethodFromCache(methodId);
            if (!enclosingType.rawType().equals(method.getDeclaringClass())) {
                throw new DeveloperMistakeException("Method ID not from this type walker: " + methodId);
            }
            return method;
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
    public <R> @NonNull Invoker<R> makeInvoker(@NonNull I receiver, @NonNull TypeToken<R> enclosingTypeToken) {
        Class<R> enclosingType = enclosingTypeToken.getRawType();
        R recv = enclosingType.cast(receiver);
        if (Proxy.isProxyClass(receiver.getClass())) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(receiver);
            if (invocationHandler instanceof ProxyHandler) {
                return new InvokeOnOwn<>(recv, enclosingType, (ProxyHandler<?>) invocationHandler);
            }
        }
        return new InvokeOnForeign<>(recv, enclosingType, reflectHandleCache.forType(lookup, enclosingType));
    }
}
