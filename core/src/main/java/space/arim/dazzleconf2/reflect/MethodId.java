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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Identifiers for a method.
 * <p>
 * Compared to the standard reflection API, this class provides constructability, generic type reification, and a
 * more lightweight form.
 *
 */
public final class MethodId {

    private final Object methodOrName;
    private final ReifiedType.Annotated returnType;
    private final ReifiedType[] parameters;
    private final boolean isDefault;

    /**
     * Builds from nonnull arguments
     *
     * @param name the method name
     * @param returnType its return type
     * @param parameters the method parameters, excluding the receiver type
     * @param isDefault whether the method is implemented by default
     */
    public MethodId(@NonNull String name, ReifiedType.@NonNull Annotated returnType,
                    @NonNull ReifiedType @NonNull [] parameters, boolean isDefault) {
        this.methodOrName = Objects.requireNonNull(name, "name");
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.parameters = parameters.clone();
        for (ReifiedType parameter : this.parameters) {
            Objects.requireNonNull(parameter, "parameters");
        }
        this.isDefault = isDefault;
    }

    /**
     * Builds using a method instead of just a method name
     *
     * @param method the method
     * @param returnType its return type
     * @param parameters the method parameters, excluding the receiver type
     * @param isDefault whether the method is implemented by default
     * @throws IllegalArgumentException if the method information does not match the return type or arguments
     */
    public MethodId(@NonNull Method method, ReifiedType.@NonNull Annotated returnType,
                    @NonNull ReifiedType @NonNull [] parameters, boolean isDefault) {
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
        if (!(method.isDefault() == isDefault)) {
            throw new IllegalArgumentException(
                    "Default status mismatch; expected " + method.isDefault() + ", not " + isDefault
            );
        }
        this.methodOrName = method;
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.parameters = parameters.clone();
        this.isDefault = isDefault;
    }

    /**
     * The method name
     * @return the method name
     */
    public @NonNull String name() {
        if (methodOrName instanceof Method) {
            return ((Method) methodOrName).getName();
        }
        return (String) methodOrName;
    }

    /**
     * Gets the method if this was constructed with a method via
     * {@link MethodId#MethodId(Method, ReifiedType.Annotated, ReifiedType[], boolean)}
     * @return the method if constructed with one, or null empty unset
     */
    public @Nullable Method method() {
        return (methodOrName instanceof Method) ? (Method) methodOrName : null;
    }

    /**
     * Gets the {@code java.lang.reflect.Method} for this object.
     * <p>
     * The declaring class must be either the original class which declared this method, or one of its sub-types.
     * Behavior is undefined if this property is not upheld.
     *
     * @param declaringClass the containing class
     * @return the method
     * @throws IllegalStateException possibly, if the wrong class was specified
     */
    public @NonNull Method getMethod(@NonNull Class<?> declaringClass) {
        if (methodOrName instanceof Method) {
            // Fast-path: Most of the time we're here
            return (Method) methodOrName;
        }
        Class<?>[] rawParams = new Class[parameters.length];
        for (int n = 0; n < parameters.length; n++) {
            rawParams[n] = parameters[n].rawType();
        }
        try {
            return declaringClass.getMethod(name(), rawParams);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Specified method does not exist on target class", ex);
        }
    }

    /**
     * The return type
     * @return the return type
     */
    public ReifiedType.@NonNull Annotated returnType() {
        return returnType;
    }

    /**
     * Gets the parameter at a certain index
     *
     * @param index the index
     * @return the parameter at it
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public @NonNull ReifiedType parameterAt(int index) {
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
    public @NonNull ReifiedType @NonNull [] parameters() {
        return parameters.clone();
    }

    /**
     * Gets whether the method is implemented by default
     *
     * @return true if a default implementation exists
     */
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MethodId)) return false;

        MethodId methodId = (MethodId) o;
        return isDefault == methodId.isDefault && name().equals(methodId.name())
                && returnType.equals(methodId.returnType) && Arrays.equals(parameters, methodId.parameters);
    }

    @Override
    public int hashCode() {
        int result = name().hashCode();
        result = 31 * result + returnType.hashCode();
        result = 31 * result + Arrays.hashCode(parameters);
        result = 31 * result + Boolean.hashCode(isDefault);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isDefault) {
            builder.append("default ");
        }
        builder.append(name());
        builder.append('(');
        for (int n = 0; n < parameters.length; n++) {
            if (n != 0) {
                builder.append(',');
            }
            parameters[n].toString(builder);
        }
        builder.append(')');
        return builder.toString();
    }
}
