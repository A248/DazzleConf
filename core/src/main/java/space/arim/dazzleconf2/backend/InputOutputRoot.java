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
import space.arim.dazzleconf2.internals.ReadWriteIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * A base class for data root that makes use of {@code InputStream} and {@code OutputStream}.
 * <p>
 * This class is designed to be extended, and implementors are only required to define how {@code InputStream}s and
 * {@code OutputStream}s are to be provided, and whether data exists. This class then implements all other methods on
 * that basis. For example, to use a resource inside a jar file, implementors can provide the stream representing that
 * resource, while throwing an exception if output is requested.
 * <pre>
 *     {@code
 * class FromResource extends InputOutputRoot {
 *
 *     private final String resourceName;
 *
 *     FromResource(String resourceName) {
 *         super(StandardCharsets.UTF_8);
 *         this.resourceName = resourceName;
 *     }
 *
 *     @Override
 *     public <R> R openInputStream(@NonNull Operation<R, @NonNull InputStream> operation) throws IOException {
 *         URL resourceURL = FromResource.class.getResource('/' + resourceName);
 *         Objects.requireNonNull(resourceURL, "no jar resource");
 *         try (InputStream inputStream = operation.handlesBuffering() ?
 *                 resourceURL.openStream() : new BufferedInputStream(resourceURL.openStream())) {
 *             return operation.operateUsing(inputStream);
 *         }
 *     }
 *
 *     @Override
 *     public <R> R openOutputStream(@NonNull Operation<R, @NonNull OutputStream> operation) throws IOException {
 *         // Effectively a read-only data root, since jar file resources are not writable
 *         throw new UnsupportedOperationException();
 *     }
 *
 *     @Override
 *     public boolean dataExists() throws IOException {
 *         return true; // Assume the jar resource exists
 *     }
 * }
 *     }
 * </pre>
 * <p>
 *
 */
public abstract class InputOutputRoot implements ReadableRoot, BinaryRoot {

    private final Charset charset;

    /**
     * Sets the charset to use for read/write operations involving textual streams
     *
     * @param charset the charset to use for {@code ReadableRoot} operations
     */
    public InputOutputRoot(@NonNull Charset charset) {
        this.charset = Objects.requireNonNull(charset);
    }

    @Override
    public @NonNull String readString() throws IOException {
        return openReader(reader -> {
            StringWriter stringWriter = new StringWriter();
            ReadWriteIO.transferToWriter(reader, stringWriter);
            return stringWriter.toString();
        });
    }

    @Override
    public <R> R openReader(@NonNull Operation<R, @NonNull Reader> operation) throws IOException {
        return openInputStream(new Operation<R, InputStream>() {
            @Override
            public boolean handlesBuffering() {
                return operation.handlesBuffering();
            }

            @Override
            public R operateUsing(InputStream inputStream) throws IOException {
                try (Reader reader = new InputStreamReader(inputStream, charset)) {
                    return operation.operateUsing(reader);
                }
            }
        });
    }

    @Override
    public void writeString(@NonNull String content) throws IOException {
        openWriter(writer -> {
            writer.write(content);
            return null;
        });
    }

    @Override
    public <R> R openWriter(@NonNull Operation<R, @NonNull Writer> operation) throws IOException {
        return openOutputStream(new Operation<R, OutputStream>() {
            @Override
            public boolean handlesBuffering() {
                return operation.handlesBuffering();
            }

            @Override
            public R operateUsing(OutputStream outputStream) throws IOException {
                try (Writer writer = new OutputStreamWriter(outputStream, charset)) {
                    return operation.operateUsing(writer);
                }
            }
        });
    }

    @Override
    public <R> R openReadChannel(@NonNull Operation<R, @NonNull ReadableByteChannel> operation) throws IOException {
        return openInputStream(new Operation<R, InputStream>() {
            @Override
            public boolean handlesBuffering() {
                return operation.handlesBuffering();
            }

            @Override
            public R operateUsing(InputStream inputStream) throws IOException {
                try (ReadableByteChannel inputChannel = Channels.newChannel(inputStream)) {
                    return operation.operateUsing(inputChannel);
                }
            }
        });
    }

    @Override
    public <R> R openWriteChannel(@NonNull Operation<R, @NonNull WritableByteChannel> operation) throws IOException {
        return openOutputStream(new Operation<R, OutputStream>() {
            @Override
            public boolean handlesBuffering() {
                return operation.handlesBuffering();
            }

            @Override
            public R operateUsing(OutputStream outputStream) throws IOException {
                try (WritableByteChannel outputChannel = Channels.newChannel(outputStream)) {
                    return operation.operateUsing(outputChannel);
                }
            }
        });
    }
}
