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

import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.reflect.DefaultInstantiator;
import space.arim.dazzleconf2.reflect.Instantiator;

import java.util.Objects;

/**
 * Builder for {@link Configuration}
 *
 * @param <C> the configuration type
 */
public final class ConfigurationBuilder<C> {

    private final Class<C> configClass;

    // Everything here has defaults
    private Instantiator instantiator = new DefaultInstantiator();

    /**
     * Creates
     */
    public ConfigurationBuilder(Class<C> configClass) {
        this.configClass = Objects.requireNonNull(configClass, "config class");
    }

    public FinalStep<C> backend(Backend backend) {
        return new FinalStep<>(this, backend);
    }

    public static final class FinalStep<C> {

        private final ConfigurationBuilder<C> builder;
        private final Backend backend;

        FinalStep(ConfigurationBuilder<C> builder, Backend backend) {
            this.builder = builder;
            this.backend = Objects.requireNonNull(backend, "backend");
        }
    }
}
