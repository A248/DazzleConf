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

package space.arim.dazzleconf.backend.hocon;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.TestingErrorSource;
import space.arim.dazzleconf.ErrorContext;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.StringRoot;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadHoconTest {

    private final ErrorContext.Source errorSource = new TestingErrorSource().makeErrorSource();

    @Test
    public void readOrdered() {
        StringRoot stringRoot = new StringRoot("");
        stringRoot.writeString("""
                zeroth-option = false
                first = true
                second = false
                third = 3
                fourth = "hi there"
                fifth = "who are you?"
                """);

        HoconBackend backend = new HoconBackend(stringRoot);
        DataTree read = backend.read(errorSource).getOrThrow().data();
        List<String> keys = new ArrayList<>(read.size());
        read.forEach((key, value) -> keys.add(key.toString()));
        assertEquals(List.of("zeroth-option", "first", "second", "third", "fourth", "fifth"), keys);
    }
}
