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
import space.arim.dazzleconf2.ReloadShell;

import java.util.Set;

/**
 * Low level service capable of generating runtime implementations of interfaces.
 * <p>
 * Callers may use {@link #hasProduced(Object)} to check whether a configuration object came from this instantiator.
 * <p>
 * <b>Implementing</b>
 * <p>
 * Implementing this interface correctly requires a number of considerations. It is recommended to check the library
 * source code as a reference implementation.
 * <p>
 * An {@code Instantiator} is supposed to implement equality among instances produced by it. Instances produced by
 * other {@code Instantiator} implementations should never be equal to instances produced by this one.
 *
 */
public interface Instantiator {

    /**
     * Checks whether this instantiator produced the specified object.
     * <p>
     * All instantiators are required to mark the instances they generate using some unique method. This could be as
     * simple as implementing a local interface as a marker.
     *
     * @param instance the object
     * @return true if this instantiator produced it, false otherwise
     */
    boolean hasProduced(@NonNull Object instance);

    /**
     * Generates the target class.
     * <p>
     * Speaking logically, this function takes a map of methods to return values, and generates an implementation of
     * the requested interface(s) which, when its methods are called, yields the preconfigured values. This function
     * is therefore an important backbone of the whole library, and the generated instance should operate as
     * performantly as feasible.
     * <p>
     * The values to be returned from each method are defined by the {@code MethodYield}. The method yield can be
     * queried for each interface to be implemented, and a map of methods to values will be given. Note that
     * {@link InvokeDefaultFunction} is a dummy object which should instruct the implementor to call the default
     * function instead of returning that dummy object.
     * <p>
     * <b>Implementation Targets</b>
     * <p>
     * The {@code targets} argument defines the interfaces implemented by the generated object. For every target class
     * provided, the {@code MethodYield} will fully specify the return values of each function from the interface. That
     * is to say, using {@link MethodYield#valuesFor(Class)} with the interface will provide a complete map of method
     * IDs to return value instructions.
     * <p>
     * Additionally, the targets will cover the complete type hierarchy for sub-interfaces. If one of the targets
     * extends another interface, the parent interface will also be in the array.
     * <p>
     * The {@code targets} array will always be delivered in a stable (deterministic) order, meaning the
     * {@code Instantiator} implementation can rely on that order to define equality across multiple instances produced
     * by this function.
     * <p>
     * Also, both the caller <b>AND</b> the implementor agree not to modify the {@code targets} array upon the call
     * of this method.
     * <p>
     * <b>Equality</b>
     * <p>
     * Calling this function with equal {@code targets} and {@code methodYield} parameters should yield equal instances.
     * That is to say, an instance is equal to another if it has the same behavior (yields the same values, and calls
     * the same default methods).
     *
     * @param classLoader where to generate the class file if necessary
     * @param targets the target interfaces to implement
     * @param methodYield a map of methods to the values they are to yield
     * @return the generated implementation
     */
    @NonNull Object generate(@NonNull ClassLoader classLoader, @NonNull Class<?> @NonNull [] targets,
                             @NonNull MethodYield methodYield);

    /**
     * Makes a reloadable shell for the given interface type. The interface's methods are conveniently included as
     * parameters.
     * <p>
     * <b>Equality</b>
     * <p>
     * The instance yielded as the shell ({@link ReloadShell#getShell()}) should have the same equality behavior as
     * the delegate it currently wraps. The returned {@code ReloadShell} itself should use identity equality (only be
     * equal to itself).
     *
     * @param classLoader where to generate the class file if necessary
     * @param iface the interface type
     * @return a reload shell
     * @param <I> the interface type
     */
    <I> @NonNull ReloadShell<I> generateShell(@NonNull ClassLoader classLoader, @NonNull Class<I> iface,
                                              @NonNull Set<@NonNull MethodId> methods);

    /**
     * Generates an "empty" implementation for the given interface type, which lets the caller use its default
     * method implementations.
     * <p>
     * The purpose of this function is for the caller to use the default methods of the provided interface, and the
     * caller promises not to use non-default methods. If that promise is broken, behavior is not defined.
     * <p>
     * <b>Equality</b>
     * <p>
     * Calling this function with the same {@code iface} parameter should yield equal instances.
     * <p>
     * The returned instance should also be considered equal to an instance produced by
     * {@link #generate(ClassLoader, Class[], MethodYield)} if and only if the {@code MethodYield} provided to that
     * function is empty. That is to say, an "empty" instance being generated by this method can only be equal to an
     * instance generated by the other method, if the other instance has only default methods and yields no preset
     * values.
     *
     * @param classLoader where to generate the class file if necessary
     * @param iface the interface type
     * @return the instance
     */
    @NonNull Object generateEmpty(@NonNull ClassLoader classLoader, @NonNull Class<?> iface);

}
