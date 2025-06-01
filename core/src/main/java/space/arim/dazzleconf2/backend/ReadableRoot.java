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
import java.io.Reader;
import java.io.Writer;

/**
 * Unifying interface for string based data roots.
 *
 */
public interface ReadableRoot extends DataRoot {

    /**
     * Reads to a string
     *
     * @return the read content
     * @throws IOException upon failure to open
     */
    @NonNull String readString() throws IOException;

    /**
     * Opens a reader to the data
     *
     * @param <R> the result type
     * @param operation the operation
     * @return the operation result
     * @throws IOException upon failure to open
     */
    <R> R openReader(@NonNull Operation<R, @NonNull Reader> operation) throws IOException;

    /**
     * Writes to a string
     *
     * @param content the content to write
     * @throws IOException upon failure to write
     */
    void writeString(@NonNull String content) throws IOException;

    /**
     * Opens a writer for the data
     *
     * @param <R> the result type
     * @param operation the operation
     * @return the operation result
     * @throws IOException upon failure to open
     */
    <R> R openWriter(@NonNull Operation<R, @NonNull Writer> operation) throws IOException;

}
