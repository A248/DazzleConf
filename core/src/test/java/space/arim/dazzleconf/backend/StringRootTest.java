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
import space.arim.dazzleconf2.backend.StringRoot;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringRootTest {

    private final StringRoot stringRoot = new StringRoot("original");

    @Test
    public void dataExists() throws IOException {
        assertTrue(stringRoot.dataExists());
    }

    @Test
    public void readString() throws IOException {
        assertEquals("original", stringRoot.readString());
    }

    @Test
    public void openReader() throws IOException {
        assertEquals("original", stringRoot.openReader(reader -> {
            StringWriter output = new StringWriter();
            reader.transferTo(output);
            return output.toString();
        }));
    }

    @Test
    public void writeString() {
        stringRoot.writeString("changed");
        assertEquals("changed", stringRoot.readString());
    }

    @Test
    public void openWriter() throws IOException {
        assertEquals("written", stringRoot.openWriter(writer -> {
            writer.write("wrote");
            return "written";
        }));
        assertEquals("wrote", stringRoot.readString());
    }
}
