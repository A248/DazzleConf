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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.backend.DefaultKeyMapper;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.backend.SnakeCaseKeyMapper;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static space.arim.dazzleconf.Utilities.assertEqualsBothWays;
import static space.arim.dazzleconf.Utilities.assertNotEqualsBothWays;

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
        List<CharSequence> output = new ArrayList<>();
        keyPath.forEach(output::add);
        assertEquals(List.of("my-brave-world", "this-feature", "enabled"), output);
        assertEquals(output, keyPath.intoPartsList());
    }

    @Test
    public void addBack() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        assertEquals("my-brave-world.this-feature.enabled", keyPath.toString());
        assertArrayEquals(new String[] {"my-brave-world", "this-feature", "enabled"}, keyPath.intoParts());
        List<CharSequence> output = new ArrayList<>();
        keyPath.forEach(output::add);
        assertEquals(List.of("my-brave-world", "this-feature", "enabled"), output);
        assertEquals(output, keyPath.intoPartsList());
    }

    @Test
    public void addFrontKeyMap() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addFront("thisFeature");
        assertEquals("this-feature", keyPath.toString());
        assertArrayEquals(new String[] {"this-feature"}, keyPath.intoParts());
        List<CharSequence> output = new ArrayList<>();
        keyPath.forEach(output::add);
        assertEquals(List.of("this-feature"), output);
        assertEquals(output, keyPath.intoPartsList());
    }

    @Test
    public void addFrontKeyMapPostFacto() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("thisFeature");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        assertEquals("this-feature", keyPath.toString());
        assertArrayEquals(new String[] {"this-feature"}, keyPath.intoParts());
        List<CharSequence> output = new ArrayList<>();
        keyPath.forEach(output::add);
        assertEquals(List.of("this-feature"), output);
        assertEquals(output, keyPath.intoPartsList());
    }

    @Test
    public void addBackKeyMap() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addBack("thisFeature");
        assertEquals("this-feature", keyPath.toString());
        assertArrayEquals(new String[] {"this-feature"}, keyPath.intoParts());
        List<CharSequence> output = new ArrayList<>();
        keyPath.forEach(output::add);
        assertEquals(List.of("this-feature"), output);
        assertEquals(output, keyPath.intoPartsList());
    }

    @Test
    public void addBackKeyMapPostFacto() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("thisFeature");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        assertEquals("this-feature", keyPath.toString());
        assertArrayEquals(new String[] {"this-feature"}, keyPath.intoParts());
        List<CharSequence> output = new ArrayList<>();
        keyPath.forEach(output::add);
        assertEquals(List.of("this-feature"), output);
        assertEquals(output, keyPath.intoPartsList());
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

    @Test
    public void keyMapperAlreadySet() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        assertThrows(IllegalStateException.class, () -> keyPath.applyKeyMapper(new DefaultKeyMapper()));
    }

    @Test
    public void keyMapperAlreadySetToSame() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        KeyMapper keyMapper = new SnakeCaseKeyMapper();
        keyPath.applyKeyMapper(keyMapper);
        assertThrows(IllegalStateException.class, () -> keyPath.applyKeyMapper(keyMapper));
    }

    @Test
    public void intoMutOnMut() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        assertSame(keyPath, keyPath.intoMut());
    }

    @Test
    public void intoMutOnImmut() {
        KeyPath.Immut keyPath = new KeyPath.Immut("hi", "there");
        assertArrayEquals(new String[] {"hi", "there"}, keyPath.intoMut().intoParts());
    }

    @Test
    public void intoMutOnImmutCannotMutateImmut() {
        KeyPath.Immut original = new KeyPath.Immut("hi", "there");
        KeyPath.Mut mutable = original.intoMut();
        mutable.addFront("start");
        mutable.addBack("bye");
        assertArrayEquals(new String[] {"hi", "there"}, original.intoParts());
        assertArrayEquals(new String[] {"start", "hi", "there", "bye"}, mutable.intoParts());
    }

    @Test
    public void intoImmutOnImmut() {
        KeyPath.Immut keyPath = new KeyPath.Immut("hi", "there");
        assertSame(keyPath, keyPath.intoImmut());
    }

    @Test
    public void intoImmutOnMut() {
        KeyPath.Mut keyPath = new KeyPath.Mut("hi", "there");
        assertArrayEquals(new String[] {"hi", "there"}, keyPath.intoImmut().intoParts());
    }

    @Test
    public void intoImmutOnMutCannotBeMutated() {
        KeyPath.Mut original = new KeyPath.Mut("hi", "there");
        KeyPath.Immut snapshot = original.intoImmut();
        original.addFront("start");
        original.addBack("bye");
        assertArrayEquals(new String[] {"hi", "there"}, snapshot.intoParts());
        assertArrayEquals(new String[] {"start", "hi", "there", "bye"}, original.intoParts());
    }

    @Test
    public void equality() {
        KeyPath.Mut original = new KeyPath.Mut("bookCalled", "thereThere");
        KeyPath.Mut copy = new KeyPath.Mut(original);
        assertEquals(original, original);
        assertEquals(original, copy);
        assertEquals(copy, original);

        KeyPath.Mut withKeyMapper = new KeyPath.Mut(original);
        original.applyKeyMapper(new DefaultKeyMapper());
        assertEqualsBothWays(original, withKeyMapper);
        assertEqualsBothWays(new KeyPath.Immut("bookCalled", "thereThere"), withKeyMapper);
    }

    @Test
    public void equalityDifferentKeyMapper() {
        KeyPath.Mut original = new KeyPath.Mut("bookCalled", "thereThere");
        KeyPath.Mut twin = new KeyPath.Mut(original);
        KeyMapper keyMapper = new SnakeCaseKeyMapper();
        twin.applyKeyMapper(keyMapper);
        assertNotEqualsBothWays(original, twin);
        assertEqualsBothWays(new KeyPath.Mut("book-called", "there-there"), twin);
        assertEqualsBothWays(new KeyPath.Immut("book-called", "there-there"), twin);

        original.applyKeyMapper(keyMapper);
        assertEqualsBothWays(original, twin);
        original.addBack("newKey");
        assertNotEqualsBothWays(original, twin);
    }

    @AfterAll
    public static void sharedEmptyNotModified() throws NoSuchFieldException, IllegalAccessException {
        Field field = KeyPath.class.getDeclaredField("SHARED_EMPTY_PARTS");
        field.setAccessible(true);
        ArrayDeque<?> sharedEmptyParts = (ArrayDeque<?>) field.get(null);
        assertTrue(sharedEmptyParts.isEmpty());
    }
}
