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

package space.arim.dazzleconf2;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A handler for managing reloading of an inner configuration object.
 * <p>
 * This handler provides a proxy, called the shell ({@link #getShell()}) which delegates all calls made on it to an
 * inner delegate. The delegate can also be set or get via this interface. If the current delegate is {@code null},
 * calls to methods on the shell will throw {@code NullPointerException}.
 *
 * @param <C> the type of the config
 */
public interface ReloadShell<C> {

    /**
     * Sets the inner configuration object to the provided value.
     * <p>
     * This function is thread safe; the assignment is performed with at least acquire/release semantics.
     *
     * @param delegate the delegate value
     */
    void setCurrentDelegate(@Nullable C delegate);

    /**
     * Gets the current inner delegate
     *
     * @return the delegate value
     */
    @Nullable C getCurrentDelegate();

    /**
     * Gets the configuration shell, a proxy to the current delegate.
     * <p>
     * Methods called on it will reflect the latest state of the configuration (set with
     * {@link #setCurrentDelegate(Object)}) at the time of the method call. Thus, the proxy acts as convenient wrapper
     * that automatically returns up-to-date values.
     * <p>
     * This means the shell itself (the returned value) can be conveniently stored in multiple locations, without
     * needing to update these fields when the underlying configuration (the delegate) is reloaded.
     *
     * @return the configuration shell, a proxy to the current delegate
     */
    @NonNull C getShell();

}
