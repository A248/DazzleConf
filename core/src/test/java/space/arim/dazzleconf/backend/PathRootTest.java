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
import space.arim.dazzleconf2.backend.DataRoot;
import space.arim.dazzleconf2.backend.PathRoot;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PathRootTest {

    private final Path file;
    private final PathRoot pathRoot;

    public PathRootTest(@TempDir Path target) {
        this.file = target.resolve("file.txt");
        pathRoot = new PathRoot(file, StandardCharsets.ISO_8859_1);
    }

    @Test
    public void dataExists() throws IOException {
        assertFalse(pathRoot.dataExists());
        Files.writeString(file, "", StandardCharsets.ISO_8859_1);
        assertTrue(pathRoot.dataExists());
    }

    @Test
    public void readString() throws IOException {
        Files.writeString(file, "content");
        assertEquals("content", pathRoot.readString());
    }

    @Test
    public void writeString() throws IOException {
        pathRoot.writeString("content");
        assertEquals("content", Files.readString(file));
    }

    @Test
    public void readWriteString() throws IOException {
        Files.writeString(file, "", StandardCharsets.ISO_8859_1);
        assertEquals("", pathRoot.readString());
        pathRoot.writeString("content");
        assertEquals("content", pathRoot.readString());
    }

    @Test
    public void openReader() throws IOException {
        Files.writeString(file, "content");
        assertEquals("content", pathRoot.openReader(reader -> {
            StringWriter output = new StringWriter();
            reader.transferTo(output);
            return output.toString();
        }));
    }

    @Test
    public void openWriter() throws IOException {
        assertEquals(3, (int) pathRoot.openWriter(writer -> {
            writer.write("content");
            return 3;
        }));
        assertEquals("content", Files.readString(file));
    }

    @Test
    public void openReaderHandlesBuffering() throws IOException {
        Files.writeString(file, "content");
        assertEquals("content", pathRoot.openReader(new DataRoot.Operation<String, Reader>() {
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
        assertEquals(3, (int) pathRoot.openWriter(new DataRoot.Operation<Integer, Writer>() {
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
        assertEquals("", pathRoot.openReader(reader -> {
            StringWriter output = new StringWriter();
            reader.transferTo(output);
            return output.toString();
        }));
        assertEquals(true, pathRoot.openWriter(writer -> {
            writer.write("content");
            return true;
        }));
        assertEquals("content", pathRoot.openReader(reader -> {
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
        byte[] read = pathRoot.openReadChannel(channel -> {
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
        byte[] read = pathRoot.openInputStream(InputStream::readAllBytes);
        assertArrayEquals(randomData, read);
    }

    @Test
    public void openWriteChannel() throws IOException {
        byte[] randomData = new byte[16];
        ThreadLocalRandom.current().nextBytes(randomData);
        String success = pathRoot.openWriteChannel(channel -> {
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
        String success = pathRoot.openOutputStream(outputStream -> {
            outputStream.write(randomData);
            return "success";
        });
        assertEquals("success", success);
        assertArrayEquals(randomData, Files.readAllBytes(file));
    }
}
