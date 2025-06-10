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
import space.arim.dazzleconf2.DeveloperMistakeException;
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
 * Some configuration formats define null values. If a backend discovers a null value, it is recommended that the
 * backend act in a user-friendly manner by treating the null value as the literal string "null" instead.
 * <p>
 * Backends are not required to support comments, but it is strongly preferred if they do. If comments <i>are</i>
 * supported, the backend is required to report it in {@link Meta#supportsComments(boolean, boolean, CommentLocation)},
 * and comments (where supported) must be read and written alongside data.
 * <p>
 * <b>Data roots</b>
 * <p>
 * A backend will typically require some {@link DataRoot} (or subclass thereof) as the raw source. This requirement is
 * often expressed by a constructor parameter.
 *
 */
public interface Backend {

    /**
     * Reads data. Upon full success, the backend returns a nonnull document.
     * <p>
     * <b>Emptiness</b>
     * <p>
     * If no data exists at the source, a {@code null} value should be returned. An example of this would be if the
     * backend were using a {@link PathRoot} and the file in question did not exist: the implementor of this method
     * would check {@link DataRoot#dataExists()} in this case.
     * <p>
     * In other cases, data might exist but be blank. Here, {@code Backend}s should behave in a manner appropriate
     * to the validity of blank data in their schema. Either {@code null} or an error should be returned depending on
     * whether blank data is a syntax exception. For example, a blank file is a valid document in YAML, and the
     * implementor of this method would return a null document. A blank file is not valid in JSON, so a parse error
     * might be triggered instead.
     * <p>
     * <b>Syntax errors</b>
     * <p>
     * If the loading failed because the data is malformatted, an error result should be returned. The error message
     * can be given as {@link ErrorContext#BACKEND_MESSAGE}. Additionally, backends can recommend a syntax linter by
     * using {@link ErrorContext#SYNTAX_LINTER}.
     * <p>
     * <b>IO errors</b>
     * <p>
     * A backend is permitted to throw {@code UncheckedIOException} to handle I/O errors coming from the data root.
     *
     * @param errorSource a factory for the backend to produce errors
     * @return a load result of the data
     * @throws UncheckedIOException upon I/O failure
     */
    @NonNull LoadResult<@Nullable Document> read(ErrorContext.@NonNull Source errorSource);

    /**
     * Writes the provided data tree to the source.
     * <p>
     * <b>Comments</b>
     * <p>
     * If the comment header is non-empty, some of it may need to be written before the rest of the data, and some of it
     * may need to be written after the data. If not supported by this backend, this comment header may be ignored.
     * <p>
     * <b>Unsupported Data</b>
     * <p>
     * Not all configuration formats follow a data structure that aligns with {@link DataTree}'s key/value model with
     * nestable trees. For example, <code>.properties</code> files don't provide nested sections, and HOCON supports
     * only string-valued keys (integer keys are impossible).
     * <p>
     * When unsupported data structure is encountered, the implementor should throw {@link DeveloperMistakeException}.
     * Throwing an exception is the appropriate way to signal an incompatible configuration structure. While this makes
     * some configuration definitions constricted to compatible formats, such fail-fast behavior follows this library's
     * culture. If need be, library users can always write migrations to change their configuration definition.
     *
     * @param document the document to write
     * @throws UncheckedIOException      upon I/O failure
     * @throws DeveloperMistakeException if some aspects of the data structure are not supported by this backend
     */
    void write(@NonNull Document document);

    /**
     * Recommends a {@link KeyMapper} appropriate to the backend format.
     * <p>
     * When loading a config with {@link Configuration#configureWith(Backend)}, the recommended key mapper will be
     * selected from this method, unless the <code>Configuration</code> already declares its own key mapper.
     *
     * @return the recommended key mapper
     */
    @NonNull KeyMapper recommendKeyMapper();

    /**
     * A document loaded from, or writable to, a backend.
     *
     */
    interface Document {

        /**
         * The document-level comments.
         * <p>
         * These comments exist at the top-level of the document, and they are not attached to any particular entry.
         *
         * @return the document-level comments, or {@code CommentData.empty()} for none
         */
        @NonNull CommentData comments();

        /**
         * The data itself, in the form of a navigable tree
         *
         * @return the data
         */
        @NonNull DataTree data();

        /**
         * Returns a document consisting of the following data, without commments
         *
         * @param dataTree the data
         * @return a document containing the data
         */
        static @NonNull Document simple(@NonNull DataTree dataTree) {
            return new Document() {
                @Override
                public @NonNull CommentData comments() {
                    return CommentData.empty();
                }

                @Override
                public @NonNull DataTree data() {
                    return dataTree;
                }
            };
        }
    }

    /**
     * Gets information about this backend itself.
     * <p>
     * The returned meta should behave consistently if invoked on identical backends. It should not be affected by
     * the state of this {@code Backend} (or data contained in it), but only by its properties and capabilities.
     *
     * @return meta information about the backend
     */
    @NonNull Meta meta();

    /**
     * Defines metadata about a backend.
     * <p>
     * For callers familiar with the {@code java.sql} module, this type is analogous to JDBC's DatabaseMetaData. It
     * provides information about the backend implementation itself, such as its supported capabilities.
     * <p>
     * New methods may be added to this interface in the future, but they will always be default methods.
     * Implementors of {@code Backend} are encouraged to keep up-to-date with this interface by checking minor version
     * release notes.
     *
     */
    interface Meta {

        /**
         * Whether comments are supported in the following context.
         * <p>
         * The context is defined by whether the comments are placed on the document level or on specific entries, if
         * the comments are being read or written, and where the comments are located with respect to entries. For example,
         * {@code documentLevel = true}, false, and {@code ABOVE} specifies the writability of the document-level header,
         * and {@code documentLevel = false}, true, and {@code INLINE} specify the capability to read inline comments on
         * entries. The return value would then indicate whether comments are handled in this context.
         * <p>
         * <b>Implications</b>
         * <p>
         * If comments are not supported in this context, this format backend is free to ignore them during
         * {@link #write(Document)}.
         * <p>
         * If this method returns true for <code>reading = true</code> and <code>reading = false</code> with otherwise
         * identical {@code documentLevel} and {@code location}, that implies that comments in such context will
         * be correctly read -- and identically reconstituted -- by <code>read</code> provided they were written by
         * <code>write</code>.
         * <p>
         * <b>Ignored comments during reading</b>
         * <p>
         * Note that some backends may expose comments in other places, not covered by this library. An example would be
         * comments on list entries - because the API only supports comments on map entries. These comments are impossible
         * to return from a call to <code>read</code>, meaning they are necessarily produced by the end user. Such
         * kind of comments should have no bearing on this method's results.
         *
         * @param documentLevel true for the document level, false for entry-level comments
         * @param reading       true for reading comments, false for writing them
         * @param location      the location of the comments for whom support is queried
         * @return if the backend supports comments in this context
         */
        boolean supportsComments(boolean documentLevel, boolean reading, @NonNull CommentLocation location);

        /**
         * Whether this backend supports preservation of data entry order.
         * <p>
         * The {@code reading} parameter defines where ordering happens. If ordering is preserved when reading, this
         * method should return true when {@code reading = true}, and if ordering is preserved when writing, it should
         * return true when {@code writing = true}.
         * <p>
         * <b>Implications</b>
         * <p>
         * If order is not supported during reading, entries in a data tree may be read in any order. If not
         * supported during writing, the serialized format may be written in a different order.
         * <p>
         * If order is supported in both contexts, then the backend should preserve a stable iteration order across
         * multiple calls to {@code read} and {@code write} using the same data. Loading a data tree should produce an
         * order which remains stable if that same tree is written and re-read at a later point.
         *
         * @param reading true for reading data, false for writing it
         * @return if order is preserved across the reading or writing operation
         */
        boolean supportsOrder(boolean reading);

    }
}
