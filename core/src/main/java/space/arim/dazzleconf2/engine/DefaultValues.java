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

package space.arim.dazzleconf2.engine;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * Storage for default values
 *
 * @param <V> the value type
 */
public interface DefaultValues<V> {

    /**
     * Gets the default value
     *
     * @return the default value
     */
    @NonNull V defaultValue();

    /**
     * Gets the value if missing in an existing configuration
     *
     * @return the value if missing
     */
    @NonNull V ifMissing();

    /**
     * Creates a simple implementation from the given value.
     * <p>
     * The value provided doubles as the default value and the missing value.
     *
     * @param defaultValue the value to use
     * @return a default values implementation
     * @param <V> the type being provided
     */
    static <V> DefaultValues<V> simple(@NonNull V defaultValue) {
        class Simple implements DefaultValues<V> {

            @Override
            public @NonNull V defaultValue() {
                return defaultValue;
            }

            @Override
            public @NonNull V ifMissing() {
                return defaultValue;
            }
        }
        Objects.requireNonNull(defaultValue);
        return new Simple();
    }
}
