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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Unique identifier for a method
 *
 */
public final class MethodId {

    private final Object methodOrName;
    private final ReifiedType returnType;
    private final ReifiedType[] parameters;

    /**
     * Builds from nonnull arguments
     *
     * @param name the method name
     * @param returnType its return type
     * @param parameters the method parameters, excluding the receiver type
     */
    public MethodId(String name, ReifiedType returnType, ReifiedType[] parameters) {
        this.methodOrName = Objects.requireNonNull(name, "name");
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.parameters = parameters.clone();
    }

    /**
     * Builds using a method instead of just a method name
     *
     * @param method the method
     * @param returnType its return type
     * @param parameters the method parameters, excluding the receiver type
     * @throws IllegalArgumentException if the method information does not match the return type or arguments
     */
    public MethodId(Method method, ReifiedType returnType, ReifiedType[] parameters) {
        if (!method.getReturnType().equals(returnType.rawType())) {
            throw new IllegalArgumentException(
                    "Return type mismatch; expected " + method.getReturnType() + ", not " + returnType.rawType()
            );
        }
        if (method.getParameterCount() != parameters.length) {
            throw new IllegalArgumentException(
                    "Parameter count mismatch; expected " + method.getParameterCount() + ", not " + parameters.length
            );
        }
        for (int n = 0; n < parameters.length; n++) {
            Class<?> expected = method.getParameterTypes()[n];
            Class<?> actual = parameters[n].rawType();
            if (!expected.equals(actual)) {
                throw new IllegalArgumentException(
                        "Parameter type mismatch; expected " + expected + ", not " + actual
                );
            }
        }
        this.methodOrName = method;
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.parameters = parameters.clone();
    }

    /**
     * The method name
     * @return the method name
     */
    public String name() {
        if (methodOrName instanceof Method) {
            return ((Method) methodOrName).getName();
        }
        return (String) methodOrName;
    }

    /**
     * Gets the method if this was constructed with a method via {@link MethodId#MethodId(Method, ReifiedType, ReifiedType[])}
     * @return the method if constructed with one, or null empty unset
     */
    public Optional<Method> method() {
        if (methodOrName instanceof Method) {
            return Optional.of((Method) methodOrName);
        }
        return Optional.empty();
    }

    /**
     * Gets the {@code java.lang.reflect.Method} for this object. The input parameter MUST be the actual class from
     * which this method was taken, otherwise behavior is <b>not defined</b>.
     *
     * @param enclosingClass the containing class
     * @return the method
     * @throws IllegalStateException possibly, if the wrong class was specified
     */
    public Method getMethod(Class<?> enclosingClass) {
        if (methodOrName instanceof Method) {
            // Fast-path: Most of the time we're here
            return (Method) methodOrName;
        }
        Class<?>[] rawParams = new Class[parameters.length];
        for (int n = 0; n < parameters.length; n++) {
            rawParams[n] = parameters[n].rawType();
        }
        try {
            return enclosingClass.getDeclaredMethod(name(), rawParams);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Specified method does not exist on target class", e);
        }
    }

    /**
     * The return type
     * @return the return type
     */
    public ReifiedType returnType() {
        return returnType;
    }

    /**
     * Gets the parameter at a certain index
     *
     * @param index the index
     * @return the parameter at it
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public ReifiedType parameterAt(int index) {
        return parameters[index];
    }

    /**
     * The parameter count
     * @return the parameter count
     */
    public int parameterCount() {
        return parameters.length;
    }

    /**
     * Gets all the parameters
     *
     * @return a copy of the parameters
     */
    public ReifiedType[] parameters() {
        return parameters.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MethodId)) return false;

        MethodId methodId = (MethodId) o;
        return name().equals(methodId.name()) && returnType.equals(methodId.returnType)
                && Arrays.equals(parameters, methodId.parameters);
    }

    @Override
    public int hashCode() {
        int result = name().hashCode();
        result = 31 * result + returnType.hashCode();
        result = 31 * result + Arrays.hashCode(parameters);
        return result;
    }

    @Override
    public String toString() {
        return "MethodId{" +
                "name=" + name() +
                ", returnType=" + returnType +
                ", arguments=" + Arrays.toString(parameters) +
                '}';
    }
}
