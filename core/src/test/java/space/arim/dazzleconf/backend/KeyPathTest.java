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
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.backend.SnakeCaseKeyMapper;

import static org.junit.jupiter.api.Assertions.*;

public class KeyPathTest {

    @Test
    public void emptyPath() {
        assertEquals("", new KeyPath.Mut().toString());
        assertArrayEquals(new String[0], new KeyPath.Mut().intoParts());
    }

    @Test
    public void construct() {
        String target = "my-brave-world.this-feature.enabled";
        String[] parts = new String[] {"my-brave-world", "this-feature", "enabled"};

        assertEquals(target, new KeyPath.Mut(parts).toString());
        assertArrayEquals(parts, new KeyPath.Mut(parts).intoParts());
    }

    @Test
    public void addFront() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("enabled");
        keyPath.addFront("this-feature");
        keyPath.addFront("my-brave-world");
        assertEquals("my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void addBack() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        assertEquals("my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void lockChanges() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        assertThrows(IllegalStateException.class, () -> keyPath.addFront("nope-1"));
        assertThrows(IllegalStateException.class, () -> keyPath.addBack("nope-2"));
        assertEquals("my-brave-world.this-feature", keyPath.toString());
        assertArrayEquals(new String[] {"my-brave-world", "this-feature"}, keyPath.intoParts());
    }

    @Test
    public void addFrontKeyMap() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addFront("thisFeature");
        assertEquals("this-feature", keyPath.toString());
        assertArrayEquals(new String[] {"this-feature"}, keyPath.intoParts());
    }

    @Test
    public void addFrontKeyMapPostFacto() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("thisFeature");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        assertEquals("this-feature", keyPath.toString());
        assertArrayEquals(new String[] {"this-feature"}, keyPath.intoParts());
    }

    @Test
    public void addBackKeyMap() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addBack("thisFeature");
        assertEquals("this-feature", keyPath.toString());
        assertArrayEquals(new String[] {"this-feature"}, keyPath.intoParts());
    }

    @Test
    public void addBackKeyMapPostFacto() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("thisFeature");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        assertEquals("this-feature", keyPath.toString());
        assertArrayEquals(new String[] {"this-feature"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously1() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously2() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously3() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously4() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously5() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously6() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously7() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously8() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        assertEquals("section.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously9() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addBack("enabled");
        keyPath.addFront("sectionMapped");
        assertEquals("section-mapped.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section-mapped", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously10() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addBack("enabled");
        keyPath.addFront("sectionMapped");
        assertEquals("section-mapped.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section-mapped", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously11() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        assertEquals("section-mapped.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section-mapped", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously12() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        assertEquals("section-mapped.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section-mapped", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously13() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addBack("enabled");
        keyPath.addFront("sectionMapped");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        assertEquals("section-mapped.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section-mapped", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously14() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addBack("enabled");
        keyPath.addFront("sectionMapped");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        assertEquals("section-mapped.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section-mapped", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously15() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        assertEquals("section-mapped.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section-mapped", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }

    @Test
    public void buildVariously16() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        assertEquals("section-mapped.my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"section-mapped", "my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
    }
}
