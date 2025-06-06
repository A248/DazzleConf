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

import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * A migration package
 *
 * @param <C_OLD> the old config type
 * @param <C_NEW> the new config type
 */
public final class Migration<C_OLD, C_NEW> {

    private final MigrateSource<C_OLD> migrateSource;
    private final Transition<C_OLD, C_NEW> transition;

    /**
     * Creates from a source and transition
     * @param migrateSource the migration source
     * @param transition the transition
     */
    public Migration(@NonNull MigrateSource<C_OLD> migrateSource, @NonNull Transition<C_OLD, C_NEW> transition) {
        this.migrateSource = Objects.requireNonNull(migrateSource, "migrateSource");
        this.transition = Objects.requireNonNull(transition, "transition");
    }

    /**
     * Tries to migrate.
     *
     * @param migrateContext the migration context
     * @return a load result that yields the newly transitioned configuration
     */
    public @NonNull LoadResult<@NonNull C_NEW> tryMigrate(@NonNull MigrateContext migrateContext) {
        return migrateSource.load(migrateContext).map((loaded) -> {
            C_NEW transitioned = transition.migrateFrom(loaded, migrateContext);
            return Objects.requireNonNull(transitioned, "transition returned null");
        });
    }

    /**
     * Signals that the migration was fully completed.
     * <p>
     * This means that the old config version may not be necessary to keep around. It might be time to delete, or move
     * to a different place (like config_old.yml) for archival purposes.
     *
     * @throws UncheckedIOException upon an I/O failure
     */
    public void onCompletion() {
        migrateSource.onCompletion();
    }

}
