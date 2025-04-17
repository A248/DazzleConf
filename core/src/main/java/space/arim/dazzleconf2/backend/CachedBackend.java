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

import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.engine.KeyMapper;

import java.util.Objects;

/**
 * A wrapper for another backend. <b>Not thread safe.</b> This wrapper stores the last known tree which was read,
 * and caches it so that it can be reused.
 * <p>
 * This wrapper assumes that it, and only it, is the one writing to the delegate backend with
 * {@link Backend#writeTree(DataTree)}. If the monopoly on writing is violated, this should not be used.
 */
public final class CachedBackend implements Backend {

    private final Backend delegate;
    private LoadResult<DataTree> currentTree;

    /**
     * Creates from the given delegate
     *
     * @param delegate the delegate
     */
    public CachedBackend(Backend delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public LoadResult<DataTree> readTree() {
        if (currentTree == null) {
            currentTree = delegate.readTree();
        }
        return currentTree;
    }

    @Override
    public void writeTree(DataTree tree) {
        // If delegate#writeTree throws an exception, we can't rely on whether that operation completed
        currentTree = null;
        delegate.writeTree(tree);
        currentTree = LoadResult.of(tree);
    }

    @Override
    public boolean supportsComments(DataTree.CommentLocation location) {
        return delegate.supportsComments(location);
    }

    @Override
    public KeyMapper recommendKeyMapper() {
        return delegate.recommendKeyMapper();
    }
}
