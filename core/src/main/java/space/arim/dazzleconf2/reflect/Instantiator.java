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

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.ReloadShell;

/**
 * Service capable of generating runtime implementations of interfaces.
 * <p>
 * Callers may use {@link #hasProduced(Object)} to check whether a configuration object came from this instantiator.
 * <p>
 * <b>Implementing</b>
 * <p>
 * Implementing this interface correctly requires a number of considerations. It is recommended to check the library
 * source code as a reference implementation.
 * <p>
 * Generated code must handle bridge and synthetic methods of its own accord. Users of this interface, like users of
 * {@link MethodMirror}, are expected to be externally ignorant of bridge and synthetic methods, insofar as caller code
 * will look identical regardless of the presence of bridge and synthetic methods.
 * <p>
 * <b>Equality</b>
 * <p>
 * An {@code Instantiator} is supposed to implement equality among instances produced by it. Instances produced by
 * other {@code Instantiator} implementations should never be equal to instances produced by this one. Each generating
 * method describes the equality considerations which must be upheld.
 * <p>
 * Notably, all methods must consider the leading {@code iface} parameter in calculating equality. The {@code iface}
 * identifies the interface being implemented, and if this interface differs between instances produced by this
 * instantiator, then those instances cannot be equal. This holds true not only for instances produced by the same
 * generating method, but also for equality between instances from different generating methods.
 * <p>
 * Because of the reflexive property of {@code equals}, generated instances may need to be aware of each others'
 * equality contracts even if they are produced by different generating methods. Additionally, {@code hashCode} must
 * be implemented in a manner compatible with the equality contract.
 *
 */
@API(status = API.Status.MAINTAINED)
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
     * the requested interface which, when its methods are called, yields the preconfigured values. This function
     * is therefore an important backbone of the whole library, and the generated instance should operate as
     * performantly as feasible.
     * <p>
     * <b>Methods and Values</b>
     * <p>
     * The implementor can use {@link MethodYield#entries()} to traverse the methods of {@code iface}, retrieving
     * return values for each. Every {@link MethodYield.Entry#method()} identifies a method belonging to the
     * {@code iface} argument, or one of its supertypes. These methods are guaranteed to represent the lowest in the
     * class hierarchy, and they exclude overidden parent methods. In other words, parent methods which are overidden
     * won't be provided; using {@link MethodYield.Entry#implementable()} will return the overriding interface.
     * <p>
     * <b>Equality</b>
     * <p>
     * Calling this function with equal {@code iface} and {@code methodYield} parameters should yield equal instances.
     * That is to say, an instance is equal to another if it has the same behavior (yields the same values, and calls
     * the same default methods).
     * <p>
     * Additionally, an instance should be considered equal to an instance produced by <code>generateEmpty</code>
     * if the {@code MethodYield} passed here is empty of preset values (see {@link #generateEmpty(Class)}). For
     * a shell instance produced by <code>generateShell</code>, please see {@link #generateShell(Class)}.
     *
     * @param iface       the interface to implement
     * @param methodYield a map of methods to the values they are to yield, for each type in the hierarchy of {@code iface}
     * @return the generated implementation
     */
    <I> @NonNull I generate(@NonNull Class<I> iface, @NonNull MethodYield methodYield);

    /**
     * Makes a reloadable shell for the given interface type.
     * <p>
     * <b>Equality</b>
     * <p>
     * The returned {@code ReloadShell} itself should use identity equality (only be equal to itself). However, the
     * shell instance (from {@link ReloadShell#getShell()}) should have the following equality behavior.
     * <p>
     * First, it requires other instances to have the same {@code iface} argument to be considered equal. This holds
     * true not only for shell instances produced by this method, but also instances produced by {@code generate} and
     * {@code generateEmpty}.
     * <p>
     * Secondly, the delegate should be considered. Two shell instances are equal if they have equal delegates,
     * including potentially null delegates (null equals null). Otherwise, the delegate must be nonnull, and the shell
     * instance should evaluate its own equality by checking if the current delegate is equal (by calling its
     * {@code equals} method).
     *
     * @param <I>   the interface type
     * @param iface the interface to implement
     * @return a reload shell
     */
    <I> @NonNull ReloadShell<I> generateShell(@NonNull Class<I> iface);

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
     * The instance should also be considered equal to an instance produced by {@link #generate(Class, MethodYield)},
     * if and only if the interfaces match and the {@code MethodYield} provided to that function is empty of preset
     * values (i.e., excluding the special value {@link InvokeDefaultFunction}.) That is to say, an "empty" instance
     * being generated by this method can only be equal to an instance generated by the other method, if the other
     * instance implements the same interface and only invokes default method implementations.
     * <p>
     * For a shell instance produced by {@link #generateShell(Class)}, please see
     *
     * @param <I>   the interface type
     * @param iface the interface to implement
     * @return the instance
     */
    <I> @NonNull I generateEmpty(@NonNull Class<I> iface);

}
