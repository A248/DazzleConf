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

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.DeveloperMistakeException;
import space.arim.dazzleconf.ReloadShell;

import java.lang.reflect.AnnotatedElement;
import java.util.stream.Stream;

/**
 * A provider responsible for generating, instantiating, scanning hierarchies of, and invoking methods on a type.
 * <p>
 * Implementing this interface correctly requires a number of considerations. It is recommended to check the library
 * source code as a reference implementation.
 * <h2>Usage</h2>
 * <p>
 * The purposes of this interface are mutually related, and some parts are logically prerequisite to using other parts.
 * Here is an overview of this interface's contracts, both from the caller and implementor perspectives.
 * <h3>Inspection</h3>
 * <p>
 * Reflection to a type yields that type's reified methods. That is, all generic signatures are replaced by the
 * arguments to their respective type variables. {@link #typeWalker(ReifiedType)} provides this capability and the
 * caller determines how to traverse super types.
 * <p>
 * The implementation is tasked with filtering out bridge methods. Thus, from the caller perspective, the type is fully
 * reified, and concerns relating to generics are invisible.
 * <h3>Calling</h3>
 * <p>
 * After the methods of a type have been obtained, they can be invoked on an instance of a type. This is only possible
 * after type walking, since the caller must use and respect the methods and super types which type walking returned.
 * <p>
 * First, a handler to support invocation is created with {@link #makeInvoker(Object, TypeToken)}. This handler must be
 * created for each type in the hierarchy, according to the hierarchy returned by {@link TypeWalker#getSuperTypes()}.
 * Afterward, the {@link Invoker} supports calling methods directly declared in the type for which it was created. Each
 * method must come the type walker, also.
 * <p>
 * Disobeying these contracts might not always result in immediate exceptions. For efficiency purposes, a reflection
 * implementor might check some of these preconditions lightly, or not at all. In such cases, caller violation of
 * contracts can lead to wrong return values or unexpected exceptions thrown at arbitrary times. Exact behavior will
 * depend on the implementation.
 * <h3>Production</h3>
 * <p>
 * The <i>generate</i> methods produce instances of the reflected type.
 * <p>
 * All such instances should cause {@link ReflectionService#hasProduced(Object)} to return {@code true} when used with
 * the reflection service that created this provider.
 * <p>
 * <b>Equality</b>
 * <p>
 * Equality is defined for all generated instances. Instances produced by other service implementors, or by providers
 * for other types, must never be equal to instances produced by this one. With respect to this provider, each
 * generating method describes the equality considerations which must be upheld.
 * <p>
 * Notably, all generation methods must consider the leading {@code clazz} parameter in calculating equality. The
 * {@code clazz} identifies the type being implemented, and if this type differs, then two instances cannot be equal.
 * <p>
 * Because of the reflexive property of {@code equals}, generated instances may need to be aware of each other's
 * equality contracts even if they are produced by different generating methods. Additionally, {@code hashCode} must
 * be implemented in a manner compatible with the equality contract.
 *
 * @param <I> the reflected type
 */
@API(status = API.Status.MAINTAINED)
public interface ReflectionProvider<I> {

    /**
     * Begins creating a new method yield for use with this provider instance
     *
     * @return a method yield
     */
    @NonNull MethodYield newMethodYield();

    /**
     * Generates the target class.
     * <p>
     * Speaking logically, this function takes a map of methods to return values, and generates an implementation of
     * the requested interface which, when its methods are called, yields the preconfigured values. This function
     * is therefore an important backbone of the whole library, and the generated instance should operate as
     * performantly as feasible.
     * <p>
     * <b>Completeness</b>
     * <p>
     * The method instructions based on the argument method yield should fully cover the type's methods, based on those
     * returned by the type walker. If some methods do not have attached instructions, then this function and/or the
     * instantiated object may behave unexpectedly; generated methods may throw exceptions or return dummy values, or
     * this function itself may throw an exception.
     * <p>
     * <b>Equality</b>
     * <p>
     * Calling this function with equal instructions in the {@code methodYield} should yield equal instances. That is,
     * an instance is equal to another if it has the same behavior (yields the same values, and calls the same default
     * methods).
     * <p>
     * Additionally, an instance should be considered equal to an instance produced by <code>generateEmpty</code>
     * if the {@code MethodYield} passed here is empty of fixed values (see {@link #generateEmpty()}). For a shell
     * instance produced by {@link #generateShell()}, please see that method's documentation. The
     * implementor must respect shell instances in all equality contracts.
     *
     * @param methodYield a map of methods to the values they are to yield, for each type in the hierarchy of {@code clazz}.
     *                    Calling this generation function closes the {@code MethodYield}, which cannot be used again.
     * @return the generated implementation
     * @throws DeveloperMistakeException if the method yield is incomplete for the reflected type, or if it was not
     * created with {@link #newMethodYield()}
     */
    @NonNull I generate(@NonNull MethodYield methodYield);

    /**
     * Makes a reloadable shell for the given interface type.
     * <p>
     * <b>Equality</b>
     * <p>
     * The returned {@code ReloadShell} itself should use identity equality (only be equal to itself). However, the
     * shell instance (from {@link ReloadShell#getShell()}) should have the following equality behavior.
     * <p>
     * First, it requires other instances to have the same {@code clazz} argument to be considered equal. This holds
     * true not only for shell instances produced by this method, but also instances produced by {@code generate} and
     * {@code generateEmpty}.
     * <p>
     * Secondly, the delegate should be considered. Two shell instances are equal if they have equal delegates,
     * including potentially null delegates (null equals null). Otherwise, the delegate must be nonnull, and the shell
     * instance should evaluate its own equality by checking if the current delegate is equal (by calling its
     * {@code equals} method).
     *
     * @return a reload shell
     * @throws UnsupportedOperationException if the reflection implementor does not support reload shells for the type
     */
    @NonNull ReloadShell<I> generateShell();

    /**
     * Generates an "empty" implementation for the given interface type, which lets the caller use its default
     * method implementations.
     * <p>
     * The purpose of this function is for the caller to use the default methods of the provided interface, and the
     * caller promises not to use non-default methods. If that promise is broken, behavior is not defined. Non-default
     * methods could throw exceptions, return null values, or even crash the JVM.
     * <p>
     * <b>Equality</b>
     * <p>
     * Calling this function with the same {@code clazz} parameter should yield equal instances.
     * <p>
     * The instance should also be considered equal to an instance produced by {@link #generate(MethodYield)},
     * if and only if the interfaces match and the {@code MethodYield} provided to that function is empty of fixed
     * values (i.e., all instructions are {@link MethodYield.ForImplementable#callDefault(MethodId)}). That is to
     * say, an "empty" instance being generated by this method can only be equal to an instance generated by the other
     * method, if the other instance implements the same interface and only invokes default method implementations.
     * <p>
     * For a shell instance produced by {@link #generateShell()}, please see that method's documentation. The
     * implementor must respect shell instances in all equality contracts.
     *
     * @return the instance
     */
    @NonNull I generateEmpty();

    /**
     * Makes a type walker for the given type.
     * <p>
     * A type walker allows traversing a type hierarchy and inspecting its methods. The caller can control movement and
     * selection of super classes insofar as the control flow suits them. However, it must be started from this method.
     * <p>
     * The passed {@code reifiedType} provides the appropriate generic information so that the implementor can return
     * reified type information for methods in this super class.
     *
     * @param reifiedType the reified type being walked
     * @return the type walker
     * @throws IllegalArgumentException if the raw type is not equal to the type {@code I} supported by this provider
     * @throws DeveloperMistakeException if the caller violated an implicit expectation of the reflection implementor
     * (for example, the reflection implementor might not support raw types, but a reified type with no generic
     * information was passed)
     */
    @NonNull TypeWalker typeWalker(@NonNull ReifiedType reifiedType);

    /**
     * Controller for accessing the reflected methods of a given type.
     * <p>
     * A type walker existing for a certain type implies the ability to call reflected methods returned from that type
     * walker, using the {@link Invoker}.
     */
    interface TypeWalker {

        /**
         * Gets the type which this {@code TypeWalker} is looking at.
         * <p>
         * Calling {@link ReflectionProvider#makeInvoker(Object, TypeToken)} cannot fail for this type.
         *
         * @return the type
         */
        @NonNull ReifiedType getEnclosingType();

        /**
         * Gets all accessible, non-inherited, instance methods declared in this type, which are viable for use as
         * defined method nodes.
         * <p>
         * The implementor must handle and filter out bridge and static methods. The order of the stream will determine
         * the order of configuration definition, so it may be preferable to return a stable order.
         *
         * @return a stream of reified methods, must not be parallel and must not include bridge methods
         */
        @NonNull Stream<@NonNull MethodId> getViableMethods();

        /**
         * Gets annotations present on the specified method.
         * <p>
         * The method must be taken from {@link #getViableMethods()}, meaning it must be accessible and located in
         * the type this {@code TypeWalker} is made for. This function is permitted to assume this precondition without
         * checks.
         *
         * @param methodId the method
         * @return the annotations on this method
         * @throws DeveloperMistakeException if the method is detected as not coming from {@code getViableMethods()}.
         * Other exceptions may also be thrown
         */
        @NonNull AnnotatedElement getAnnotations(@NonNull MethodId methodId);

        /**
         * Moves to the directly declared super types of this type.
         * <p>
         * If the type walker encounters a super type for which it does not support viable methods, it can either not
         * return that type or return an empty type walker (with no methods). In any case, the existence of a type
         * walker for a specific type implies the ability to invoke methods for it. For example, a reflection provider
         * for records might not return type walkers for interfaces the record implements, because that reflection
         * provider does not support calling methods on interfaces.
         * <p>
         * A super type for java.lang.Object is not returned.
         *
         * @return the super types. The order of this array is not important, but the order may (optionally) reflect
         * the declaration order of the super types.
         */
        @NonNull TypeWalker @NonNull [] getSuperTypes();

    }

    /**
     * Creates an invoker that allows calling methods on the given receiver.
     * <p>
     * Usage of the invoker is restricted to methods declared in the given enclosing type. The caller upholds the
     * guarantee that no other methods (such as inherited methods) will be called; if this is not respected, behavior
     * may be unpredictable.
     *
     * @param <R> the declaring type enclosing the methods to be called, which must be a super type of {@code I}
     * @param receiver the receiver object, on which the methods are called
     * @param enclosingType the enclosing type
     * @return an invoker for the receiver
     * @throws ClassCastException if the receiver is not an instance of the enclosing type
     */
    <R> @NonNull Invoker<R> makeInvoker(@NonNull I receiver, TypeToken<R> enclosingType);

    /**
     * A wrapper for an instance whose methods can be called, as long as those methods conform to a certain
     * enclosing type.
     *
     * @param <R> the type of the invoked receiver
     */
    interface Invoker<R> {

        /**
         * Gets the receiver this invoker was created with
         *
         * @return the receiver
         */
        @NonNull R getReceiver();

        /**
         * Invokes a method and gets the return value.
         * <p>
         * The method must be taken from {@link TypeWalker#getViableMethods()}, meaning it must be accessible and
         * located in the type this {@code TypeWalker} is made for. This function is permitted to assume this
         * precondition. Additionally, this function may assume the precondition that the number and type of arguments
         * match the argument array. Violation of these preconditions may cause unexpected behavior, not just throw
         * exceptions.
         *
         * @param methodId the method to call
         * @param arguments method arguments. The array may be <code>null</code> for none, and the arguments must be
         *                  boxed primitive objects to represent primitive values. They must otherwise be compatible
         *                  with the number and type of parameters of the method itself.
         * @return the return value, boxing primitives if necessary
         * @throws Throwable if the method threw a throwable, it is wrapped in this exception
         * @throws DeveloperMistakeException if the method ID did not come from the type walker for this type
         * @throws IllegalArgumentException if the number or type of arguments does not match the method parameters
         */
        @Nullable Object invokeMethod(@NonNull MethodId methodId, @Nullable Object @Nullable ...arguments)
                throws Throwable;

    }

}
