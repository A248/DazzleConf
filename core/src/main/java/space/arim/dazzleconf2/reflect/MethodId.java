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

import space.arim.dazzleconf.internal.util.ImmutableCollections;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Unique identifier for a method
 *
 */
public final class MethodId {

    private final Object methodOrName;
    private final ReifiedType returnType;
    private final List<ReifiedType> arguments;

    /**
     * Builds from nonnull arguments
     *
     * @param name the method name
     * @param returnType its return type
     * @param arguments the method arguments, excluding the receiver type
     */
    public MethodId(String name, ReifiedType returnType, List<ReifiedType> arguments) {
        this.methodOrName = Objects.requireNonNull(name, "name");
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.arguments = ImmutableCollections.listOf(arguments);
    }

    /**
     * Builds using a method instead of just a method name
     *
     * @param method the method
     * @param returnType its return type
     * @param arguments the method arguments, excluding the receiver type
     * @throws IllegalArgumentException if the method information does not match the return type or arguments
     */
    public MethodId(Method method, ReifiedType returnType, List<ReifiedType> arguments) {
        if (!method.getReturnType().equals(returnType.rawType())) {
            throw new IllegalArgumentException(
                    "Return type mismatch; expected " + method.getReturnType() + " but was " + returnType.rawType()
            );
        }
        for (int n = 0; n < method.getParameterCount(); n++) {
            Class<?> expected = method.getParameterTypes()[n];
            Class<?> actual = arguments.get(n).rawType();
            if (!expected.equals(actual)) {
                throw new IllegalArgumentException(
                        "Argument type mismatch; expected " + expected + " but was " + actual
                );
            }
        }
        this.methodOrName = method;
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.arguments = ImmutableCollections.listOf(arguments);
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
     * Gets the method if this was constructed with a method via {@link MethodId#MethodId(Method, ReifiedType, List)}
     * @return the method if constructed with one, or an empty optional
     */
    public Optional<Method> method() {
        if (methodOrName instanceof Method) {
            return Optional.of((Method) methodOrName);
        }
        return Optional.empty();
    }

    /**
     * The return type
     * @return the return type
     */
    public ReifiedType returnType() {
        return returnType;
    }

    /**
     * Gets an immmutable view of the arguments
     * @return the arguments
     */
    public List<ReifiedType> arguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MethodId)) return false;

        MethodId methodId = (MethodId) o;
        return name().equals(methodId.name()) && returnType.equals(methodId.returnType) && arguments.equals(methodId.arguments);
    }

    @Override
    public int hashCode() {
        int result = name().hashCode();
        result = 31 * result + returnType.hashCode();
        result = 31 * result + arguments.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MethodId{" +
                "name=" + name() +
                ", returnType=" + returnType +
                ", arguments=" + arguments +
                '}';
    }
}
