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
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.io.UncheckedIOException;

/**
 * Configuration backend for reading and writing data trees.
 * <p>
 * <b>Configuration formats and options</b>
 * <p>
 * A backend typically represents a configuration format, such as YAML or Json, using another library to handle parsing
 * syntax. Some formats will expose the capability to behave differently - whether to change printing style, support
 * commenting workarounds, or accept escape sequences. Backends can provide such options through constructor
 * parameters.
 * <p>
 * Backends are not required to support comments, but it is strongly preferred if they do. If comments <i>are</i>
 * supported, the backend <b>must</b> round-trip those comments between successive calls to <code>read</code> and
 * <code>write</code>.
 * <p>
 * <b>Data roots</b>
 * <p>
 * A backend will typically require some {@link DataRoot} (or subclass thereof) as the raw source. This requirement is
 * often expressed by a constructor parameter.
 *
 */
public interface Backend {

    /**
     * Reads data.
     * <p>
     * <b>Success values and streaming</b>
     * <p>
     * Upon success, the backend returns streamable data. It is recommended for the backend to implement
     * {@link DataStreamable#getAsStream()} in an efficient manner.
     * <p>
     * Streams cannot communicate errors, however, meaning that any errors need to be returned from this method itself.
     * Thus, this method cannot provide "pure" streaming, and some kind of intermediate buffer (like format-specific
     * data) will need to be loaded into memory before this method can return.
     * <p>
     * However, there is still value in the backend implementing a stream-efficient return value. Some callers might
     * want to filter keys and values before adding them to a data tree, and other callers may not want a tree at all.
     * <p>
     * <b>Emptiness</b>
     * <p>
     * If no data exists at the source, a {@code null} value should be returned. An example of this would be if the
     * backend were using a {@link PathRoot} and the file in question did not exist: the implementor of this method
     * would check {@link DataRoot#dataExists()} in this case.
     * <p>
     * In other cases, data might exist, but it will be blank. For example, a blank file is a valid document in YAML,
     * and the implementor of this method would thus return a non-null {@code DataTree} with no content in it.
     * <p>
     * <b>Syntax errors</b>
     * <p>
     * If the loading failed because the data exists but was malformatted, an error result should be returned. The
     * error message can be given as {@link ErrorContext#BACKEND_MESSAGE}.
     * <p>
     * <b>IO errors</b>
     * <p>
     * A backend is permitted to throw {@code UncheckedIOException} in two places: either this method itself, or in
     * usage of the {@code Stream} returned by the streamable instance. The latter case is rare but theoretically
     * possible.
     *
     * @return a load result of the data
     * @throws UncheckedIOException upon I/O failure
     */
    @NonNull LoadResult<? extends @Nullable DataStreamable> read();

    /**
     * Writes the provided data to the source.
     * <p>
     * Implementations are encouraged to stream the data (via {@link DataStreamable#getAsStream()}) where possible.
     * Streaming may be more efficient in some cases.
     * <p>
     * If a comment header is provided, some of it may need to be written before the rest of the data, and some of it
     * may need to be written after the data. If not supported by this backend, this comment header may be ignored.
     *
     * @param topLevelComments the comments applying to the whole document
     * @param data the streamable data tree
     * @throws UncheckedIOException upon I/O failure
     */
    void write(@Nullable CommentData topLevelComments, @NonNull DataStreamable data);

    /**
     * Whether comments are supported in the following location.
     * <p>
     * If comments are not supported there, this format backend is free to ignore them during
     * {@link #write(CommentData, DataStreamable)}.
     * <p>
     * If comments <i>are</i> supported in this location, they <i>must</i> be supported by both <code>write</code>
     * and <code>read</code> methods. That is, comments at this location must correctly round-trip and
     * {@link DataEntry#getComments()} should return the same value after being written and re-read.
     *
     * @param location where are we talking about
     * @return if comments are supported in this location
     */
    boolean supportsComments(@NonNull CommentLocation location);

    /**
     * Recommends a {@link KeyMapper} appropriate to the backend format.
     * <p>
     * When loading a config with {@link Configuration#configureWith(Backend)}, the recommended key mapper will be
     * selected from this method, unless the <code>Configuration</code> already declares its own key mapper.
     *
     * @return the recommended key mapper
     */
    @NonNull KeyMapper recommendKeyMapper();

}
