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
import org.checkerframework.dataflow.qual.Pure;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Identifiers for a method.
 * <p>
 * This class is immutable. Compared to the standard reflection API, this class provides constructability, generic type
 * reification, and a more lightweight form.
 * <p>
 * <b>Usage</b>
 * <p>
 * A {@code MethodId} is not coupled to a declaring class; in fact, it does not verify the existence of a real method
 * with its details. This means that callers can construct arbitrary instances.
 * <p>
 * This comes with the drawback that a {@code MethodId} is not itself invocable. Instead, the {@link MethodMirror} is
 * tasked with calling methods on it.
 * <p>
 * To assist the {@code MethodMirror} implementation, it can store an opaque object (invisible to callers) to attach
 * cache information. This cache information is returned by {@link #getOpaqueCache()}. Importantly, the method mirror
 * can depend on the preservation of this information, since external callers are forbidden from using method ID
 * instances except those produced by the mirror itself.
 * <p>
 * <b>Equality</b>
 * <p>
 * A method ID is equal to another based on its name, return type, parameters, and default status. If callers want to
 * ignore the return type or default status, they must implement that logic themselves.
 *
 */
public final class MethodId {

    private final transient OpaqueCache opaqueCache;
    private final String name;
    private final ReifiedType.Annotated returnType;
    private final ReifiedType[] parameters;
    private final boolean isDefault;

    /**
     * Builds from arguments
     *
     * @param name the method name
     * @param returnType its return type
     * @param parameters the method parameters, excluding the receiver type
     * @param isDefault whether the method is implemented by default
     */
    public MethodId(@NonNull String name, ReifiedType.@NonNull Annotated returnType,
                    @NonNull ReifiedType @NonNull [] parameters, boolean isDefault) {
        this.opaqueCache = null;
        this.name = Objects.requireNonNull(name, "name");
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.parameters = parameters.clone();
        for (ReifiedType parameter : this.parameters) {
            Objects.requireNonNull(parameter, "parameters");
        }
        this.isDefault = isDefault;
    }

    private MethodId(OpaqueCache opaqueCache, String name, ReifiedType.Annotated returnType,
                     ReifiedType[] parameters, boolean isDefault) {
        this.opaqueCache = opaqueCache;
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.isDefault = isDefault;
    }

    /**
     * Creates a new method ID, with the argument set as the opaque cache.
     * <p>
     * Because the opaque cache is not included in equality considerations, the returned object will always be
     * <i>equal</i> to this one (according to {@code equals}) even if not referentially equal.
     *
     * @param opaqueCache the opaque cache, or {@code null} for none
     * @return a new method ID with the cache attached
     */
    public @NonNull MethodId withOpaqueCache(@Nullable OpaqueCache opaqueCache) {
        if (this.opaqueCache == opaqueCache) {
            return this;
        }
        return new MethodId(opaqueCache, name, returnType, parameters, isDefault);
    }

    /**
     * Gets the opaque cache attached to this method ID, if one exists.
     * <p>
     * Most library users will have no need of calling this function. It is intended to help {@code MethodMirror}
     * implementations store data inside a {@code MethodId}, to speed up calling actual methods.
     *
     * @return the opaque cache, or null
     */
    @Pure
    public @Nullable OpaqueCache getOpaqueCache() {
        return opaqueCache;
    }

    /**
     * A marker interface for cache data appended to a {@code MethodId}.
     * <p>
     * Callers should check if cache data is an instance of their specific implementation of this interface.
     *
     */
    public interface OpaqueCache {}

    /**
     * The method name
     * @return the method name
     */
    public @NonNull String name() {
        return name;
    }

    /**
     * Gets the {@code java.lang.reflect.Method} for this object.
     * <p>
     * The declaring class must be either the original class which declared this method, or one of its sub-types.
     * Behavior is undefined if this property is not upheld.
     *
     * @param declaringClass the containing class
     * @return the method
     * @throws IllegalArgumentException if the wrong class was specified
     */
    @NonNull Method getMethod(@NonNull Class<?> declaringClass) {
        Class<?>[] rawParams = new Class[parameters.length];
        for (int n = 0; n < parameters.length; n++) {
            rawParams[n] = parameters[n].rawType();
        }
        try {
            return declaringClass.getMethod(name, rawParams);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Specified method does not exist on target class", ex);
        }
    }

    @NonNull MethodHandle getMethodHandle(MethodHandles.@NonNull Lookup lookup, @NonNull Class<?> declaringClass)
            throws IllegalAccessException {
        Class<?>[] rawParams = new Class[parameters.length];
        for (int n = 0; n < parameters.length; n++) {
            rawParams[n] = parameters[n].rawType();
        }
        try {
            return lookup.findVirtual(declaringClass, name, MethodType.methodType(returnType.rawType(), rawParams));
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Specified method does not exist on target class", ex);
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
        return isDefault == methodId.isDefault && name.equals(methodId.name)
                && returnType.equals(methodId.returnType) && Arrays.equals(parameters, methodId.parameters);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
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
        builder.append(name);
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
