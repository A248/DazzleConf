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
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.UpdateListener;
import space.arim.dazzleconf2.engine.UpdateReason;

/**
 * Contextual resources to be used by migrations.
 * <p>
 * This interface is implemented by the caller when passed to migrations.
 */
public interface MigrateContext extends UpdateListener {

    /**
     * The backend being used to load the main configuration.
     * <p>
     * This will not necessarily be used by all migrations. Some migrations, for example, may source from a separate
     * file, or combine different backends to get the data they need.
     *
     * @return the main backend being used to load the configuraion
     */
    @NonNull Backend mainBackend();

    /**
     * Allows migrations to signal updated paths.
     * <p>
     * Migrations may optionally use this method to identify which parts of a configuration (if applicable) were
     * changed during the course of migration.

     * @param entryPath the path of the entry
     * @param updateReason the update reason, note that some implementors will discard this parameter in favor of
     *                     {@code UpdateReason.MIGRATED} even if a different value is passed
     */
    @Override
    void notifyUpdate(@NonNull KeyPath entryPath, @NonNull UpdateReason updateReason);

    /**
     * A factory allowing migrations to produce errors.
     * <p>
     * Unlike other parts of the API, the error source is provided as a getter, and not as an extended interface.
     * This reflects the want to make implementing {@code MigrateContext} convenient, if library users wish to call
     * migrations using their own approach.
     *
     * @return a factory for error production
     */
    ErrorContext.@NonNull Source errorSource();

}
