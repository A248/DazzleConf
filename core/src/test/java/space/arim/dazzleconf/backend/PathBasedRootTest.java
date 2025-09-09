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

package space.arim.dazzleconf.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import space.arim.dazzleconf2.backend.BinaryRoot;
import space.arim.dazzleconf2.backend.DataRoot;
import space.arim.dazzleconf2.backend.ReadableRoot;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

@TestInstance(PER_METHOD)
public abstract class PathBasedRootTest<R extends ReadableRoot & BinaryRoot> {

    private final Path file;
    private final R dataRoot;

    abstract R createRoot(Path file, Charset charset);

    public PathBasedRootTest(@TempDir Path target) {
        file = target.resolve("file.txt");
        dataRoot = createRoot(file, StandardCharsets.ISO_8859_1);
    }

    @Test
    public void dataExists() throws IOException {
        assertFalse(dataRoot.dataExists());
        Files.writeString(file, "", StandardCharsets.ISO_8859_1);
        assertTrue(dataRoot.dataExists());
    }

    @Test
    public void readString() throws IOException {
        Files.writeString(file, "content");
        assertEquals("content", dataRoot.readString());
    }

    @Test
    public void writeString() throws IOException {
        dataRoot.writeString("content");
        assertEquals("content", Files.readString(file));
    }

    @Test
    public void readWriteString() throws IOException {
        Files.writeString(file, "", StandardCharsets.ISO_8859_1);
        assertEquals("", dataRoot.readString());
        dataRoot.writeString("content");
        assertEquals("content", dataRoot.readString());
    }

    @Test
    public void openReader() throws IOException {
        Files.writeString(file, "content");
        assertEquals("content", dataRoot.openReader(reader -> {
            StringWriter output = new StringWriter();
            reader.transferTo(output);
            return output.toString();
        }));
    }

    @Test
    public void openWriter() throws IOException {
        assertEquals(3, (int) dataRoot.openWriter(writer -> {
            writer.write("content");
            return 3;
        }));
        assertEquals("content", Files.readString(file));
    }

    @Test
    public void openReaderHandlesBuffering() throws IOException {
        Files.writeString(file, "content");
        assertEquals("content", dataRoot.openReader(new DataRoot.Operation<String, Reader>() {
            @Override
            public boolean handlesBuffering() {
                return true;
            }

            @Override
            public String operateUsing(Reader reader) throws IOException {
                StringWriter output = new StringWriter();
                reader.transferTo(output);
                return output.toString();
            }
        }));
    }

    @Test
    public void openWriterHandlesBuffering() throws IOException {
        assertEquals(3, (int) dataRoot.openWriter(new DataRoot.Operation<Integer, Writer>() {
            @Override
            public boolean handlesBuffering() {
                return true;
            }

            @Override
            public Integer operateUsing(Writer writer) throws IOException {
                writer.write("content");
                return 3;
            }
        }));
        assertEquals("content", Files.readString(file));
    }

    @Test
    public void openReaderWriter() throws IOException {
        Files.writeString(file, "", StandardCharsets.ISO_8859_1);
        assertEquals("", dataRoot.openReader(reader -> {
            StringWriter output = new StringWriter();
            reader.transferTo(output);
            return output.toString();
        }));
        assertEquals(true, dataRoot.openWriter(writer -> {
            writer.write("content");
            return true;
        }));
        assertEquals("content", dataRoot.openReader(reader -> {
            StringWriter output = new StringWriter();
            reader.transferTo(output);
            return output.toString();
        }));
    }

    @Test
    public void openReadChannel() throws IOException {
        byte[] randomData = new byte[16];
        ThreadLocalRandom.current().nextBytes(randomData);
        Files.write(file, randomData);
        byte[] read = dataRoot.openReadChannel(channel -> {
            ByteBuffer buffer = ByteBuffer.allocate(randomData.length);
            channel.read(buffer);
            return buffer.array();
        });
        assertArrayEquals(randomData, read);
    }

    @Test
    public void openInputStream() throws IOException {
        byte[] randomData = new byte[16];
        ThreadLocalRandom.current().nextBytes(randomData);
        Files.write(file, randomData);
        byte[] read = dataRoot.openInputStream(InputStream::readAllBytes);
        assertArrayEquals(randomData, read);
    }

    @Test
    public void openWriteChannel() throws IOException {
        byte[] randomData = new byte[16];
        ThreadLocalRandom.current().nextBytes(randomData);
        String success = dataRoot.openWriteChannel(channel -> {
            channel.write(ByteBuffer.wrap(randomData));
            return "success";
        });
        assertEquals("success", success);
        assertArrayEquals(randomData, Files.readAllBytes(file));
    }

    @Test
    public void openOutputStream() throws IOException {
        byte[] randomData = new byte[16];
        ThreadLocalRandom.current().nextBytes(randomData);
        String success = dataRoot.openOutputStream(outputStream -> {
            outputStream.write(randomData);
            return "success";
        });
        assertEquals("success", success);
        assertArrayEquals(randomData, Files.readAllBytes(file));
    }

}
