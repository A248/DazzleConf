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

package space.arim.dazzleconf2.data;

import space.arim.dazzleconf2.LoadResult;

import java.io.IOException;

public interface DataSource {

    /**
     * Writes the provided data tree to the source
     *
     * @param tree the data tree
     * @throws IOException upon I/O failure
     */
    void writeTree(DataTree tree) throws IOException;

    /**
     * Reads a load result
     *
     * @return a load result of the data tree
     */
    LoadResult<DataTree> readTree();

}
