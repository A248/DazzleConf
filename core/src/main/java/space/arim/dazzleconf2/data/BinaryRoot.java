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

import java.io.*;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface BinaryRoot extends DataRoot {

    /**
     * Opens a read channel for the data
     *
     * @param <R> the result type
     * @param operation the operation
     * @return the operation result
     * @throws IOException upon failure to open
     */
    <R> R openReadChannel(Operation<R, ReadableByteChannel> operation) throws IOException;

    /**
     * Opens an input stream for the data
     *
     * @param <R> the result type
     * @param operation the operation
     * @return the operation result
     * @throws IOException upon failure to open
     */
    <R> R openInputStream(Operation<R, InputStream> operation) throws IOException;

    /**
     * Opens a write channel to the data
     *
     * @param <R> the result type
     * @param operation the operation
     * @return the operation result
     * @throws IOException upon failure to open
     */
    <R> R openWriteChannel(Operation<R, WritableByteChannel> operation) throws IOException;

    /**
     * Opens an output stream to the data
     *
     * @param <R> the result type
     * @param operation the operation
     * @return the operation result
     * @throws IOException upon failure to open
     */
    <R> R openOutputStream(Operation<R, OutputStream> operation) throws IOException;

}
