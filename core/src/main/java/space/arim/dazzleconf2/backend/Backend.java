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
 * Configuration format backend for reading and writing data trees
 *
 */
public interface Backend {

    /**
     * Reads a data tree.
     * <p>
     * If no data exists (empty source), a null tree is returned. If the loading failed because the data
     * exists but was malformatted, this may be reflected by {@link ErrorContext#BACKEND_MESSAGE} in the load result.
     *
     * @return a load result of the data tree
     * @throws UncheckedIOException upon I/O failure
     */
    @NonNull LoadResult<@Nullable DataTree> readTree();

    /**
     * Writes the provided data tree to the source
     *
     * @param tree the data tree
     * @throws UncheckedIOException upon I/O failure
     */
    void writeTree(@NonNull DataTree tree);

    /**
     * Whether comments are supported in the following location. If comments are not supported there, this format
     * backend is free to ignore them during {@link #writeTree(DataTree)}
     *
     * @param location where are we talking about
     * @return if comments are supported in this location
     */
    boolean supportsComments(@NonNull CommentLocation location);

    /**
     * Recommends a {@link KeyMapper} appropriate to the backend format.
     *
     * When loading a config with {@link Configuration#configureWith(Backend)}, the recommended key mapper will be
     * selected from this method, unless the <code>Configuration</code> already declares its own key mapper.
     *
     * @return the recommended key mapper
     */
    @NonNull KeyMapper recommendKeyMapper();

}
