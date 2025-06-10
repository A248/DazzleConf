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

package space.arim.dazzleconf.backend.toml;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.JToml_Access;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.ReadableRoot;
import space.arim.dazzleconf2.backend.SnakeCaseKeyMapper;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.Objects;

public final class TomlBackend implements Backend {

    private final ReadableRoot dataRoot;
    private final JToml jToml;

    public TomlBackend(@NonNull ReadableRoot dataRoot) {
        this.dataRoot = Objects.requireNonNull(dataRoot);

        JTomlOptions jTomlOptions = JTomlOptions.builder().set(JTomlOption.WRITE_EMPTY_TABLES, true).build();
        jToml = new JToml_Access(jTomlOptions).getInstance();
    }

    @Override
    public @NonNull LoadResult<@Nullable Document> read(ErrorContext.@NonNull Source errorSource) {
        dataRoot.openReader(reader -> {
            jToml.read(reader);
            return null;
        });
        return null;
    }

    @Override
    public void write(@NonNull Document document) {

    }

    @Override
    public @NonNull KeyMapper recommendKeyMapper() {
        return new SnakeCaseKeyMapper();
    }

    @Override
    public @NonNull Meta meta() {
        return new Meta() {
            @Override
            public boolean supportsComments(boolean documentLevel, boolean reading, @NonNull CommentLocation location) {
                return false;
            }

            @Override
            public boolean supportsOrder(boolean reading) {
                return false;
            }
        };
    }
}
