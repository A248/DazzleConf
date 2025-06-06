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

/**
 * An optional API for migrations.
 * <p>
 * <b>Migration sources</b>
 * <p>
 * An eternal problem, since the advent of configurations, is how to detect and update old versions. In this package,
 * {@link space.arim.dazzleconf2.migration.MigrateSource} is trusted with both loading and detecting of usable old
 * configuration versions.
 * <p>
 * <b>It is imperative that {@code MigrateSource} implementations do not detect the latest, up-to-date configuration as
 * the old version.</b> If users of this package are not careful, they will find their configuration stuck in a loop:
 * the library will perpetually update the old version to the new version, which is then detected as the old version,
 * and so on.
 * <p>
 * If the migration produced any new keys, or updated existing ones, it can signal to the migration context's load
 * listener (via {@link space.arim.dazzleconf2.migration.MigrateContext#loadListener()}). Migration sources are
 * encouraged to flag these updates, even if keys might be arranged by later transitions. {@code UpdateReason.MIGRATED}
 * is an appropriate update reason.
 * <p>
 * <b>Transitions</b>
 * <p>
 * After the old config is loaded, a {@link space.arim.dazzleconf2.migration.Transition} transforms it into the latest
 * version. Transitions are designed to be infallible, so that chaining is supported.
 * <p>
 * Like the migration source, the transition can also inform the {@code MigrateContext#loadListener()} if it decided
 * to update any paths. This happens frequently, for example, if keys are moved, if types are changed, or sections
 * are expanded or consolidated. The transition should pass the <i>old path</i> to the load listener if a key moved,
 * and it should pass multiple old paths (calling the listener each time) if multiple are relevant.
 * <p>
 * <b>Integration and usage</b>
 * <p>
 * A source and a transition together comprise a {@link space.arim.dazzleconf2.migration.Migration}. Migrations are
 * addable to a configuration at construction time, and they are used by the <code>configureWith</code> and
 * <code>configureOrFallback</code> methods on {@link space.arim.dazzleconf2.Configuration}.
 * <p>
 * Other configuration reading methods, which do <i>not</i> take a {@code Backend}, do not use migrations. However,
 * library users reserve the right to call migrations directly, or to provide their own logic for handling migrations
 * (including by using some, but not all, of the APIs in this package).
 *
 *
 */
package space.arim.dazzleconf2.migration;