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

package space.arim.dazzleconf.engine;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.engine.KeyPath;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyPathTest {

    @Test
    public void emptyPath() {
        assertEquals("", new KeyPath().toString());
        assertArrayEquals(new String[0], new KeyPath().intoParts());
    }

    @Test
    public void construct() {
        String target = "my-brave-world.this-feature.enabled";
        String[] parts = new String[] {"my-brave-world", "this-feature", "enabled"};

        assertEquals(target, new KeyPath(parts).toString());
        assertArrayEquals(parts, new KeyPath(parts).intoParts());
    }

    @Test
    public void addFront() {
        KeyPath keyPath = new KeyPath();
        keyPath.addFront("enabled");
        keyPath.addFront("this-feature");
        keyPath.addFront("my-brave-world");
        assertEquals("my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void addBack() {
        KeyPath keyPath = new KeyPath();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        assertEquals("my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously1() {
        KeyPath keyPath = new KeyPath();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously2() {
        KeyPath keyPath = new KeyPath();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously3() {
        KeyPath keyPath = new KeyPath();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously4() {
        KeyPath keyPath = new KeyPath();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

}
