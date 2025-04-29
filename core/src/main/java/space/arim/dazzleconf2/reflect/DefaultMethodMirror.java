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
import space.arim.dazzleconf2.internals.MethodUtil;
import space.arim.dazzleconf2.DeveloperMistakeException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Default implementation of {@link MethodMirror} using standard reflection
 *
 */
public final class DefaultMethodMirror implements MethodMirror {

    /**
     * Creates
     */
    public DefaultMethodMirror() {}

    private static final ReifiedType[] EMPTY_PARAMS = new ReifiedType[0];

    @Override
    public @NonNull Stream<@NonNull MethodId> getViableMethods(ReifiedType.@NonNull Annotated reifiedType) {

        GenericContext classGenerics = new GenericContext(reifiedType);
        Method[] classMethods = reifiedType.rawType().getDeclaredMethods();

        return Arrays.stream(classMethods)
                .filter(method -> !method.isSynthetic() && !method.isBridge())
                .map((method -> {
                    Type[] methodParameters = method.getGenericParameterTypes();

                    if (method.getTypeParameters().length != 0) {
                        throw new DeveloperMistakeException(
                                "Configuration method " + method.getName() + " in " + reifiedType.rawType() +
                                        " cannot declared with generic type parameters."
                        );
                    }
                    ReifiedType.Annotated reifiedReturn = classGenerics.reifyAnnotated(method.getAnnotatedReturnType());
                    ReifiedType[] reifiedParameters = (methodParameters.length == 0) ?
                            EMPTY_PARAMS : new ReifiedType[methodParameters.length];
                    for (int n = 0; n < reifiedParameters.length; n++) {
                        reifiedParameters[n] = classGenerics.reify(methodParameters[n]);
                    }
                    return new MethodId(method, reifiedReturn, reifiedParameters, MethodUtil.isDefault(method));
                }));
    }

    @Override
    public <A extends Annotation> @Nullable A getAnnotation(@NonNull MethodId methodId,
                                                            @NonNull ReifiedType enclosingType,
                                                            @NonNull Class<A> annotation) {
        return methodId.getMethod(enclosingType.rawType()).getAnnotation(annotation);
    }

    @Override
    public @NonNull Invoker makeInvoker(@NonNull Object receiver, @NonNull Class<?> enclosingType) {
        ProxyHandler proxyHandler = null;
        if (Proxy.isProxyClass(receiver.getClass())) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(receiver);
            if (invocationHandler instanceof ProxyHandler) {
                proxyHandler = (ProxyHandler) invocationHandler;
            }
        }
        return new Invoke(receiver, enclosingType, proxyHandler);
    }

    private static final class Invoke implements Invoker {

        private final Object receiver;
        private final Class<?> enclosingType;
        /** Nonnull if receiver is a proxy */
        private final ProxyHandler proxyHandler;

        private Invoke(Object receiver, Class<?> enclosingType, ProxyHandler proxyHandler) {
            this.receiver = receiver;
            this.enclosingType = enclosingType;
            this.proxyHandler = proxyHandler;
        }

        @Override
        public Class<?> enclosingType() {
            return enclosingType;
        }

        @Override
        public @Nullable Object invokeMethod(@NonNull MethodId methodId, @Nullable Object @Nullable ... arguments)
                throws InvocationTargetException {

            // Get the method - we need it either way
            Method method = methodId.getMethod(enclosingType);
            // Try calling the proxy directly if possible
            if (proxyHandler != null) {
                try {
                    proxyHandler.implInvoke(method, arguments);
                } catch (Throwable e) {
                    throw new InvocationTargetException(e);
                }
            }
            // Use standard reflection
            try {
                return method.invoke(receiver, arguments);
            } catch (IllegalAccessException e) {
                throw new DeveloperMistakeException("Configuration method inaccessible", e);
            }
        }
    }
}
