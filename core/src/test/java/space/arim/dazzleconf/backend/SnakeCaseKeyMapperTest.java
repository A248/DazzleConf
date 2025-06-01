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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.SnakeCaseKeyMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SnakeCaseKeyMapperTest {

    private final KeyMapper keyMapper = new SnakeCaseKeyMapper();

    @Test
    public void simple() {
        assertEquals("brave", keyMapper.labelToKey("brave"));
    }

    @Test
    public void standard1() {
        assertEquals("my-brave-world", keyMapper.labelToKey("myBraveWorld"));
    }

    @Test
    public void standard2() {
        assertEquals("my-brave-world-two", keyMapper.labelToKey("myBraveWorldTwo"));
    }

    @Test
    public void malformed1() {
        assertEquals("my-brave", keyMapper.labelToKey("MyBrave"));
    }

    @Test
    public void malformed2() {
        assertEquals("m-b-w", keyMapper.labelToKey("MBW"));
    }

    @Test
    public void equality() {
        EqualsVerifier.forClass(SnakeCaseKeyMapper.class).verify();
    }
}
