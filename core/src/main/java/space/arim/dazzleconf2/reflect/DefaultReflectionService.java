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

import java.lang.invoke.MethodHandles;

/**
 * The default implementation of {@link ReflectionService}.
 * <p>
 * It uses standard reflection to find and call methods, and {@code java.lang.reflect.Proxy} to generate classes
 * implementing the configuration interface.
 *
 */
public final class DefaultReflectionService implements ReflectionService {

    /**
     * Creates the service
     */
    public DefaultReflectionService() {}

    @Override
    public @NonNull Instantiator makeInstantiator(MethodHandles.@NonNull Lookup lookup) {
        return new DefaultInstantiator(lookup);
    }

    @Override
    public @NonNull MethodMirror makeMethodMirror(MethodHandles.@NonNull Lookup lookup) {
        return new DefaultMethodMirror(lookup);
    }
}
