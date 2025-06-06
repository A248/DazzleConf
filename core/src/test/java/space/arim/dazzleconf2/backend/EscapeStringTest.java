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

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EscapeStringTest {

    @TestFactory
    public Stream<DynamicTest> roundTrip() {
        return Stream.generate(() -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int[] data = new int[8];
            for (int n = 0; n < data.length; n++) {
                data[n] = random.nextInt(Character.MIN_CODE_POINT, Character.MAX_CODE_POINT);
            }
            return new String(data, 0, data.length);
        }).limit(100L).map(string -> {
            return DynamicTest.dynamicTest("For data " + string, () -> {
                String quotedAsLiteral = new EscapeString(string).printString();
                // Remove leading and trailing "
                String inner = quotedAsLiteral.substring(1, quotedAsLiteral.length() - 1);
                assertEquals(string, StringEscapeUtils.unescapeJava(inner), () -> {
                    return "Failed to recuperate string \n" + string + "\n. Quoted literal was \n" + quotedAsLiteral;
                });
            });
        });
    }
}
