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

package space.arim.dazzleconf2.migration;

import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;

import java.util.Objects;

/**
 * Simple migration source from another configuration with this library
 *
 * @param <C> the config type
 */
public final class MigrateFromConfig<C> implements MigrateSource<C> {

    private final Configuration<C> config;

    /**
     * Creates
     *
     * @param config the configuration to use
     */
    public MigrateFromConfig(Configuration<C> config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public LoadResult<C> load(Backend mainBackend) {
        return mainBackend.readTree().flatMap(config::readFrom);
    }

    @Override
    public void onCompletion() {

    }
}
