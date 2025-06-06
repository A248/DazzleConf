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

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.engine.UpdateReason;

import java.io.UncheckedIOException;

/**
 * A source for an old configuration version.
 * <p>
 * A migration source is responsible for loading the old version, checking whether it is applicable, and if so,
 * returning it.
 *
 * @param <C> the config type
 */
public interface MigrateSource<C> {

    /**
     * Attempts to load this configuration version.
     * <p>
     * If the old version isn't detectable or in usable, returns an error.
     * <p>
     * <b>Updated paths</b>
     * <p>
     * If the loading the old configuration produced any new keys, or updated existing ones, the implementor should use
     * the migration context's load listener to flag these updates. {@link UpdateReason#MIGRATED} should be used for
     * all key updates.
     * <p>
     * Importantly, updates should only be flagged when the implementation is sure it will return a success value,
     * and after filters have been applied. Please see {@link MigrateFromConfiguration} for a reference implementation.
     *
     * @param migrateContext migration context to use if necessary
     * @return the old configuration version, or an error if not found and usable
     * @throws UncheckedIOException upon an I/O failure
     */
    @NonNull LoadResult<@NonNull C> load(@NonNull MigrateContext migrateContext);

    /**
     * Signals that the migration was fully completed.
     * <p>
     * This means that the old config version may not be necessary to keep around. It might be time to delete, or move
     * to a different place (like config_old.yml) for archival purposes.
     *
     * @throws UncheckedIOException upon an I/O failure
     */
    void onCompletion();

    /**
     * Returns a new migration source, with the given filter applied.
     * <p>
     * Values from the original {@link #load(MigrateContext)} will be passed through the filter, and if the filter
     * returns false, the resulting {@code load(MigrateContext)} will return an empty error instead.
     *
     * @param filter the source filter to apply
     * @return a new migration source, taking into account the filter
     * @throws UnsupportedOperationException if the implementation does not support adding filters
     */
    @NonNull MigrateSource<C> addFilter(@NonNull Filter<C> filter);

    /**
     * A filter for old configuration versions.
     * <p>
     * Instances of this type can be applied by using {@link MigrateSource#addFilter(Filter)}.
     * <p>
     * Normally, a {@code MigrateSource} is responsible for detecting the validity of an old version. Still, this type
     * exists to make the API more usable, so that callers don't have to re-implement {@code MigrateSource} themselves.
     *
     * @param <C_OLD> the old configuration type
     */
    interface Filter<C_OLD> {

        /**
         * Checks whether the given old configuration should be used.
         * <p>
         * This method is called <i>after</i> the original {@link MigrateSource#load(MigrateContext)} to filter the value
         * coming from it. If it returns false, the {@code Transition} will not be invoked.
         *
         * @param oldConfig the old configuration
         * @return true if usable
         */
        boolean isUsable(@NonNull C_OLD oldConfig);

    }
}
