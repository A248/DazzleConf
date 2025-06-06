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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;

/**
 * Implementation of data root for a string
 */
public final class StringRoot implements ReadableRoot {

    private String content;

    /**
     * Creates from the given initial ccontent
     * @param content the content
     */
    public StringRoot(@NonNull String content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    public boolean dataExists() throws IOException {
        return true;
    }

    @Override
    public @NonNull String readString() {
        return content;
    }

    @Override
    public <R> R openReader(@NonNull Operation<R, @NonNull Reader> operation) throws IOException {
        try (Reader reader = new StringReader(content)) {
            return operation.operateUsing(reader);
        }
    }

    @Override
    public void writeString(@NonNull String content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    public <R> R openWriter(@NonNull Operation<R, @NonNull Writer> operation) throws IOException {
        StringWriter writer = new StringWriter();
        R outcome = operation.operateUsing(writer);
        content = writer.toString();
        return outcome;
    }
}
