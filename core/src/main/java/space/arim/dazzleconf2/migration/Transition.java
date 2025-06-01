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

/**
 * Converter interface for moving from one configuration object to another
 * @param <C_OLD> the old config type
 * @param <C_NEW> the new config type
 */
public interface Transition<C_OLD, C_NEW> {

    /**
     * Migrates from one configuration to another
     *
     * @param previous the old config object
     * @param migrateContext the migration context
     * @return the new config object
     */
    @NonNull C_NEW migrateFrom(@NonNull C_OLD previous, @NonNull MigrateContext migrateContext);

    /**
     * Chains on another transition after this one
     *
     * @param next the next transition
     * @return the combined product
     * @param <C_NEWER> the newest config type
     */
    default <C_NEWER> @NonNull Transition<C_OLD, C_NEWER> chain(@NonNull Transition<C_NEW, C_NEWER> next) {
        class Chained implements Transition<C_OLD, C_NEWER> {

            @Override
            public @NonNull C_NEWER migrateFrom(@NonNull C_OLD previous, @NonNull MigrateContext migrateContext) {
                C_NEW intermediate = Transition.this.migrateFrom(previous, migrateContext);
                return next.migrateFrom(intermediate, migrateContext);
            }
        }
        return new Chained();
    }
}
