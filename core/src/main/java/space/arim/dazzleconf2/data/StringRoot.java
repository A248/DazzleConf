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
import java.util.Objects;

/**
 * Implementation of data root for a string
 */
public final class StringRoot implements HumanReadableRoot {

    private String content;

    /**
     * Creates from the given nonnull content
     * @param content the content
     */
    public StringRoot(String content) {
        this.content = Objects.requireNonNull(content);
    }

    /**
     * Gets the current content, which may change
     * @return the content
     */
    public String getContent() {
        return content;
    }

    @Override
    public String readToString() throws IOException {
        return content;
    }

    @Override
    public <R> R useReader(Operation<R, Reader> operation) throws IOException {
        try (Reader reader = new StringReader(content)) {
            return operation.operateUsing(reader);
        }
    }

    @Override
    public void writeString(String content) throws IOException {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    public <R> R openWriter(Operation<R, Writer> operation) throws IOException {
        StringWriter writer = new StringWriter();
        R outcome = operation.operateUsing(writer);
        this.content = writer.toString();
        return outcome;
    }

}
