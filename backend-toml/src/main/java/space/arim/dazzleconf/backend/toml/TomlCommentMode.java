/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

package space.arim.dazzleconf.backend.toml;

/**
 * Controls how comments should be managed by the {@link TomlBackend}.
 * <p>
 * The choice of this enum is set using {@link TomlBackend.Builder#commentMode(TomlCommentMode)}.
 */
public enum TomlCommentMode {
    /**
     * Instructs the backend to write comments, every time, but never to load or read them.
     * <ul>
     *     <li>Advantage: All comments from the interface are written.</li>
     *     <li>Disadvantage: User comments are erased every time the configuration is rewritten to file.</li>
     * </ul>
     */
    WRITE_ALWAYS,
    /**
     * Instructs the backend to read and write comments above the entry. Comments above each entry will thus be
     * preserved after each read/write cycle ("round-trip").
     * <p>
     * A few caveats apply. Comments in any other location are unsupported and will be skipped: namely the document
     * header, inline comments, and below comments. Also, handling of comments on TOML tables is undefined if the TOML
     * table is written in many pieces across the file.
     * <ul>
     *     <li>Advantage: User comments above entries are preserved when the configuration is loaded and rewritten.</li>
     *     <li>Disadvantage: Document header, inline comments, and below comments must be skipped.</li>
     * </ul>
     */
    ROUND_TRIP_ABOVE_ONLY,
}
