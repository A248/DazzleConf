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

public interface FormatBackend {

    DataTree loadTreeFrom(ReadableDataInput dataInput);

    /**
     * Whether comments are supported in the following location. If comments are not supported there, this format
     * backend is free to ignore them during the serialization process.
     *
     * @param location where are we talking about
     * @return if comments are supported in this location
     */
    boolean supportsComments(DataEntry.CommentLocation location);

}
