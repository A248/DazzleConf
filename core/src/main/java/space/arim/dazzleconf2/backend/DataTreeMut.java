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

package space.arim.dazzleconf2.backend;

/**
 * A data tree which can unmistakably be modified
 *
 */
public final class DataTreeMut extends DataTree {

    /**
     * Creates
     */
    public DataTreeMut() {}

    /**
     * Sets the entry at the specified key
     *
     * @param key the key
     * @param entry the entry; if null, clears any existing entry
     * @throws IllegalArgumentException if the provided key is not a valid canonical type
     */
    public void set(Object key, Entry entry) {
        if (!validateKey(key)) {
            throw new IllegalArgumentException("Not a canonical key: " + key);
        }
        if (entry == null) {
            data.remove(key);
        } else {
            data.put(key, entry);
        }
    }

    /**
     * Clears any entry at the specified key
     *
     * @param key the key
     * @throws IllegalArgumentException if the provided key is not a valid canonical type
     */
    public void remove(Object key) {
        if (!validateKey(key)) {
            throw new IllegalArgumentException("Not a canonical key: " + key);
        }
        data.remove(key);
    }

    /**
     * Clears all data
     */
    public void clear() {
        data.clear();
    }

}
