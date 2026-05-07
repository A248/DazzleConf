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

package space.arim.dazzleconf.backend.hocon;

import space.arim.dazzleconf2.engine.CommentLocation;

/**
 * Controls how comments should be managed by the {@link HoconBackend}.
 * <p>
 * The choice of this enum is set using {@link HoconBackend.Builder#commentMode(HoconCommentMode)}. Regardless of
 * the choice, only comments above entries or documents can be supported, i.e. {@link CommentLocation#ABOVE}.
 *
 */
public enum HoconCommentMode {
    /**
     * Instructs the backend to write comments, every time, but never to load or read them.
     * <ul>
     *     <li>Advantage: Document header is written.</li>
     *     <li>Disadvantage: User comments are erased every time the configuration is rewritten to file.</li>
     * </ul>
     */
    WRITE_ALWAYS,
    /**
     * Instructs the backend to read and write entry comments. Comments above each entry will thus be preserved after
     * each read/write cycle ("round-trip"). The document header is unsupported and will be skipped.
     * <ul>
     *     <li>Advantage: User comments are preserved when the configuration is loaded and rewritten.</li>
     *     <li>Disadvantage: Document header must be skipped. {@link space.arim.dazzleconf2.engine.Comments}, if applied
     *     to the top-level configuration interface, will be ignored.</li>
     * </ul>
     */
    ROUND_TRIP_OMIT_HEADER,
}
