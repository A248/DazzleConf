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
import space.arim.dazzleconf2.backend.Printable;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PrintableTest {

    @Test
    public void preBuilt() throws IOException {
        assertThrows(NullPointerException.class, () -> Printable.preBuilt(null));

        Printable prebuilt = Printable.preBuilt("val");
        assertEquals("val", prebuilt.printString());

        StringBuilder output = new StringBuilder();
        prebuilt.printTo(output);
        assertEquals("val", output.toString());

        output.append(' ');
        prebuilt.printTo((Appendable) output);
        assertEquals("val val", output.toString());

        assertEquals("val", prebuilt.toString());
    }

    @Test
    public void join() throws IOException {
        assertThrows(NullPointerException.class, () -> Printable.join((Printable[]) null));

        Printable joined = Printable.join(
                Printable.preBuilt("v1"),
                Printable.join(Printable.preBuilt(" "), Printable.preBuilt("is"), Printable.preBuilt(" ")),
                Printable.preBuilt("joined")
        );
        assertEquals("v1 is joined", joined.printString());
        {
            StringBuilder output = new StringBuilder();
            joined.printTo(output);
            assertEquals("v1 is joined", output.toString());
        }
        Appendable output = new StringBuilder();
        joined.printTo(output);
        assertEquals("v1 is joined", output.toString());

        assertEquals("v1 is joined", joined.toString());
    }
}
