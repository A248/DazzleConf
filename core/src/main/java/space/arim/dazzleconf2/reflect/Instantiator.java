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
 * Low level service capable of generating runtime implementations of interfaces
 *
 */
public interface Instantiator {

    /**
     * Generates the target class
     *
     * @param classLoader where to generate the class file if necessary
     * @param targets the target interfaces to implement
     * @param methodYield a map of methods to the values they are to yield
     * @return the generated implementation
     */
    @NonNull Object generate(@NonNull ClassLoader classLoader, @NonNull Set<@NonNull Class<?>> targets,
                             @NonNull MethodYield methodYield);

    /**
     * Makes a reloadable shell for the given interface type. The interface's methods are conveniently included as
     * parameters.
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
     *
     * @param classLoader where to generate the class file if necessary
     * @param iface the interface type
     * @return the instance
     * @param <I> the interface type
     */
    <I> @NonNull I generateEmpty(@NonNull ClassLoader classLoader, @NonNull Class<I> iface);

}
