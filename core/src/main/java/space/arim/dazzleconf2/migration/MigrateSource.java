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

import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;

import java.io.UncheckedIOException;

/**
 * A source for an old configuration version
 * @param <C> the config type
 */
public interface MigrateSource<C> {

    /**
     * Attempts to load this configuration version. If the old version isn't detectable or in use, returns an error.
     * <p>
     * The provided backend is the one being used to load the main configuration. However, implementations may or may
     * not wish to make use of it.
     *
     * @param mainBackend the backend for the main configuration, which may or may not be needed
     * @return the old configuration version, or an error if not found and detected
     * @throws UncheckedIOException upon an I/O failure
     */
    LoadResult<C> load(Backend mainBackend);

    /**
     * Signals that the migration was fully completed.
     * <p>
     * This means that the old config version may not be necessary to keep around. It might be time to delete, or move
     * to a different place (like config_old.yml) for archival purposes.
     *
     * @throws UncheckedIOException upon an I/O failure
     */
    void onCompletion();
}
