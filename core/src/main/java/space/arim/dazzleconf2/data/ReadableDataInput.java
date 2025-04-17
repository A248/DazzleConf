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

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Unifying interface for string based data input.
 *
 */
public interface ReadableDataInput {

    /**
     * Reads to a string using the given charset
     *
     * @param charset the character set to use
     * @return the read content
     * @throws IOException upon failure to open
     */
    String readToString(Charset charset) throws IOException;

    /**
     * Opens a reader to the data using the character set
     *
     * @param charset the character set to use
     * @return the reader, which should be closed when finished
     * @throws IOException upon failure to open
     */
    Reader openReader(Charset charset) throws IOException;

}
