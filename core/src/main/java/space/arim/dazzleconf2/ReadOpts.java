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

package space.arim.dazzleconf2;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.engine.KeyMapper;
import space.arim.dazzleconf2.engine.LoadListener;

import java.util.List;
import java.util.Objects;

final class ReadOpts implements ConfigurationDefinition.ReadOptions {

    private final LoadListener loadListener;
    private final KeyMapper keyMapper;
    private final int maximumErrorCollect;

    static final int DEFAULT_MAX_ERROR_TO_COLLECT = 10;

    private ReadOpts(LoadListener loadListener, KeyMapper keyMapper, int maximumErrorCollect) {
        this.loadListener = Objects.requireNonNull(loadListener, "loadListener");
        this.keyMapper = Objects.requireNonNull(keyMapper, "keyMapper");
        this.maximumErrorCollect = maximumErrorCollect;
    }

    ReadOpts(LoadListener loadListener, KeyMapper keyMapper) {
        this(loadListener, keyMapper, DEFAULT_MAX_ERROR_TO_COLLECT);
    }

    @Override
    public @NonNull LoadListener loadListener() {
        return loadListener;
    }

    @Override
    public @NonNull KeyMapper keyMapper() {
        return keyMapper;
    }

    @Override
    public int maximumErrorCollect() {
        return maximumErrorCollect;
    }

}
