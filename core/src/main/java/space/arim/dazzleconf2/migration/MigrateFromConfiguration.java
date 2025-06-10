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
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.ConfigurationDefinition;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.internals.ImmutableCollections;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A migration source from another configuration with this library.
 * <p>
 * <b>Warning:</b> This migration source only verifies that the old configuration could be loaded from the current
 * backend, using that backend's recommended key mapper. However, if your old configuration <i>provides default values
 * everywhere</i>, it will load anything, and it will appear to be always detected! See the package javadoc for more
 * information on this problem, or consider adding a {@link Filter} to this migration source.
 *
 * @param <C> the config type
 */
public final class MigrateFromConfiguration<C> implements MigrateSource<C> {

    private final Configuration<C> config;
    private final List<Filter<C>> filters;

    private MigrateFromConfiguration(@NonNull Configuration<C> config, @NonNull List<@NonNull Filter<C>> filters) {
        this.config = Objects.requireNonNull(config);
        this.filters = filters;
    }

    /**
     * Creates
     *
     * @param config the configuration to use
     */
    public MigrateFromConfiguration(@NonNull Configuration<C> config) {
        this(config, ImmutableCollections.emptyList());
    }

    @Override
    public @NonNull LoadResult<@NonNull C> load(@NonNull MigrateContext migrateContext) {
        // Delay updating paths until after success and filters are applied
        List<KeyPath> updatedPaths = new ArrayList<>();

        // Load the configuration
        Backend mainBackend = migrateContext.mainBackend();
        return mainBackend.read(migrateContext.errorSource()).flatMap(document -> {
            if (document == null) {
                return migrateContext.errorSource().throwError("Main backend produced empty document");
            }
            return config.readFrom(document.data(), new ConfigurationDefinition.ReadOptions() {
                @Override
                public @NonNull LoadListener loadListener() {
                    // Use our own load listener to buffer and collect the updated paths
                    return (entryPath, updateReason) -> updatedPaths.add(entryPath);
                }

                @Override
                public @NonNull KeyMapper keyMapper() {
                    return mainBackend.recommendKeyMapper();
                }
            });
        }).flatMap(loaded -> {
            // Apply filters and bail if they test false
            for (Filter<C> filter : filters) {
                if (!filter.isUsable(loaded)) {
                    return migrateContext.errorSource().throwError("Filtered by " + filter);
                }
            }
            // Dispatch updated key paths to the caller's load listener
            for (KeyPath updatedPath : updatedPaths) {
                migrateContext.loadListener().updatedPath(updatedPath, UpdateReason.MIGRATED);
            }
            return LoadResult.of(loaded);
        });
    }

    @Override
    public void onCompletion() {

    }

    @Override
    public @NonNull MigrateSource<C> addFilter(@NonNull Filter<C> filter) {
        List<Filter<C>> newFilters = new ArrayList<>(filters);
        newFilters.add(filter);
        return new MigrateFromConfiguration<>(config, newFilters);
    }
}
