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
import space.arim.dazzleconf2.internals.FileIO;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Data root from a path
 *
 */
public final class PathRoot implements ReadableRoot, BinaryRoot {

    private final Path path;
    private final Charset charset;

    /**
     * Creates from a path and a charset, neither of which can be null
     * @param path the path
     * @param charset the charset
     */
    public PathRoot(@NonNull Path path, @NonNull Charset charset) {
        this.path = Objects.requireNonNull(path, "path");
        this.charset = Objects.requireNonNull(charset, "charset");
    }

    @Override
    public boolean dataExists() throws IOException {
        return Files.exists(path);
    }

    @Override
    public @NonNull String readString() throws IOException {
        return FileIO.readString(path, charset);
    }

    @Override
    public <R> R openReader(@NonNull Operation<R, @NonNull Reader> operation) throws IOException {
        class ReadableByteChannelOperation implements Operation<R, ReadableByteChannel> {

            @Override
            public R operateUsing(ReadableByteChannel read) throws IOException {
                try (Reader reader = Channels.newReader(read, charset.newDecoder(), -1)) {
                    if (operation.handlesBuffering()) {
                        return operation.operateUsing(reader);
                    } else {
                        try (BufferedReader buffered = new BufferedReader(reader)) {
                            return operation.operateUsing(buffered);
                        }
                    }
                }
            }
        }
        return openReadChannel(new ReadableByteChannelOperation());
    }

    @Override
    public void writeString(@NonNull String content) throws IOException {
        FileIO.writeString(path, content, charset);
    }

    @Override
    public <R> R openWriter(@NonNull Operation<R, @NonNull Writer> operation) throws IOException {
        class WritableChannelOperation implements Operation<R, WritableByteChannel> {

            @Override
            public R operateUsing(WritableByteChannel write) throws IOException {
                try (Writer writer = Channels.newWriter(write, charset.newEncoder(), -1)) {
                    if (operation.handlesBuffering()) {
                        return operation.operateUsing(writer);
                    } else {
                        try (BufferedWriter buffered = new BufferedWriter(writer)) {
                            return operation.operateUsing(buffered);
                        }
                    }
                }
            }
        }
        return openWriteChannel(new WritableChannelOperation());
    }

    @Override
    public <R> R openReadChannel(@NonNull Operation<R, @NonNull ReadableByteChannel> operation) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            return operation.operateUsing(fileChannel);
        }
    }

    @Override
    public <R> R openInputStream(@NonNull Operation<R, @NonNull InputStream> operation) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            return operation.operateUsing(inputStream);
        }
    }

    @Override
    public <R> R openWriteChannel(@NonNull Operation<R, @NonNull WritableByteChannel> operation) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(path,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            return operation.operateUsing(fileChannel);
        }
    }

    @Override
    public <R> R openOutputStream(@NonNull Operation<R, @NonNull OutputStream> operation) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(path,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            return operation.operateUsing(outputStream);
        }
    }
}
