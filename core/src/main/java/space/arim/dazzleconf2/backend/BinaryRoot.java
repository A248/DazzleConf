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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Data roots which are capable of working with raw bytes.
 *
 */
public interface BinaryRoot extends DataRoot {

    /**
     * Opens a read channel for the data
     *
     * @param <R> the result type
     * @param operation the operation
     * @return the operation result
     * @throws IOException upon failure to open
     */
    <R> R openReadChannel(@NonNull Operation<R, @NonNull ReadableByteChannel> operation) throws IOException;

    /**
     * Opens an input stream for the data
     *
     * @param <R> the result type
     * @param operation the operation
     * @return the operation result
     * @throws IOException upon failure to open
     */
    <R> R openInputStream(@NonNull Operation<R, @NonNull InputStream> operation) throws IOException;

    /**
     * Opens a write channel to the data
     *
     * @param <R> the result type
     * @param operation the operation
     * @return the operation result
     * @throws IOException upon failure to open
     */
    <R> R openWriteChannel(@NonNull Operation<R, @NonNull WritableByteChannel> operation) throws IOException;

    /**
     * Opens an output stream to the data
     *
     * @param <R> the result type
     * @param operation the operation
     * @return the operation result
     * @throws IOException upon failure to open
     */
    <R> R openOutputStream(@NonNull Operation<R, @NonNull OutputStream> operation) throws IOException;

}
