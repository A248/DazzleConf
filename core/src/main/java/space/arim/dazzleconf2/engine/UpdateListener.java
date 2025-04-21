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
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.migration.Migration;

import java.util.List;

/**
 * Listener that lets the implementor know about various configuration events
 */
public interface UpdateListener extends LoadListener {

    /**
     * Signals that the configuration didn't exist, so the whole thing was freshly written from default values
     */
    void loadedDefaults();

    /**
     * Notifies that a migration from a previous version was successful
     * @param migration the migration
     */
    void migratedFrom(@NonNull Migration<?, ?> migration);

    /**
     * Notifies that a migration was attempted from a previous version, unsuccessfully
     * @param migration the migration
     * @param errorContexts the failure contexts
     */
    void migrationSkip(@NonNull Migration<?, ?> migration, @NonNull List<@NonNull ErrorContext> errorContexts);

}
