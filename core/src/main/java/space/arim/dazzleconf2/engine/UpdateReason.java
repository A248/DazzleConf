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

/**
 * A reason why a particular path within a configuration was updated
 */
public enum UpdateReason {
    /**
     * Used when an option was missing, so the value was supplied with {@link DefaultValues#ifMissing()}.
     * <p>
     * The path thus represents that of the formerly missing value.
     */
    MISSING,
    /**
     * An existing option was updated to another value.
     * <p>
     * This typically happens when a serializer decides a better representation of the value is available, e.g. a
     * string representation of a number is replaced with the number itself.
     */
    UPDATED,
    /**
     * Used in context of migrations, such as changes in the organization or format of the config.
     * <p>
     * Potential usage:
     * <ul>
     *     <li>A configuration entry moved to a different place.</li>
     *     <li>A configuration entry was split up into separate options, or separate options were combined.</li>
     *     <li>The representation of a value changed, and the new config version requires converting to the new
     *     representation. For example, changing the format of Minecraft clickable messages from legacy formatting to
     *     MiniMessage.</li>
     *     <li>There is a path for the configuration version itself, and that version had to be updated.</li>
     * </ul>
     */
    MIGRATED,
    /**
     * A catch-all for other or unknown reasons.
     */
    OTHER
}
