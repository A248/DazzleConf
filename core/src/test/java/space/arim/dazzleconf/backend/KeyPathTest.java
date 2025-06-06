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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ArgumentsSource;
import space.arim.dazzleconf2.backend.DefaultKeyMapper;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.backend.SnakeCaseKeyMapper;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static space.arim.dazzleconf.Utilities.assertEqualsBothWays;
import static space.arim.dazzleconf.Utilities.assertNotEqualsBothWays;

@ParameterizedClass
@ArgumentsSource(KeyPathVerify.Provider.class)
public class KeyPathTest {

    private final KeyPathVerify verify;

    public KeyPathTest(KeyPathVerify verify) {
        this.verify = verify;
    }

    @Test
    public void emptyPath() {
        assertEquals("", new KeyPath.Mut().toString());
        assertArrayEquals(new String[0], new KeyPath.Mut().intoParts());
        assertTrue(new KeyPath.Mut().isEmpty());
        assertEquals(0, new KeyPath.Mut().size());
    }

    @Test
    public void construct() {
        String target = "my-brave-world.this-feature.enabled";
        String[] parts = new String[] {"my-brave-world", "this-feature", "enabled"};

        assertEquals(target, new KeyPath.Mut(parts).toString());
        assertArrayEquals(parts, new KeyPath.Mut(parts).intoParts());
        assertFalse(new KeyPath.Mut(parts).isEmpty());
        assertEquals(3, new KeyPath.Mut(parts).size());
    }

    @Test
    public void addFront() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("enabled");
        keyPath.addFront("this-feature");
        keyPath.addFront("my-brave-world");
        verify.assertEq(keyPath, "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void addBack() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void addFrontKeyMap() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("thisFeature");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        verify.assertEq(keyPath, "this-feature");
    }

    @Test
    public void addFrontKeyMapPostCall() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addFront("thisFeature");
        verify.assertEq(keyPath, "thisFeature");
    }

    @Test
    public void addBackKeyMap() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("thisFeature");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        verify.assertEq(keyPath, "this-feature");
    }

    @Test
    public void addBackKeyMapPostCall() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addBack("thisFeature");
        verify.assertEq(keyPath, "thisFeature");
    }

    @Test
    public void buildVariously1() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously2() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously3() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously4() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
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
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
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
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously7() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously8() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously9() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addBack("enabled");
        keyPath.addFront("sectionMapped");
        verify.assertEq(keyPath, "sectionMapped", "myBraveWorld", "thisFeature", "enabled");
    }

    @Test
    public void buildVariously10() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addBack("enabled");
        keyPath.addFront("sectionMapped");
        verify.assertEq(keyPath, "sectionMapped", "myBraveWorld", "thisFeature", "enabled");
    }

    @Test
    public void buildVariously11() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "sectionMapped", "myBraveWorld", "thisFeature", "enabled");
    }

    @Test
    public void buildVariously12() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "sectionMapped", "myBraveWorld", "thisFeature", "enabled");
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
        verify.assertEq(keyPath, "section-mapped", "my-brave-world", "this-feature", "enabled");
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
        verify.assertEq(keyPath, "section-mapped", "my-brave-world", "this-feature", "enabled");
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
        verify.assertEq(keyPath, "section-mapped", "my-brave-world", "this-feature", "enabled");
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
        verify.assertEq(keyPath, "section-mapped", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously17() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "sectionMapped", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously18() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.applyKeyMapper(new SnakeCaseKeyMapper());
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "sectionMapped", "my-brave-world", "this-feature", "enabled");
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
    public void equalityEmpty() {
        assertEqualsBothWays(new KeyPath.Mut(), KeyPath.empty());
        assertEqualsBothWays(new KeyPath.Immut(), KeyPath.empty());
        assertEqualsBothWays(new KeyPath.Mut(), new KeyPath.Immut());
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

    @AfterEach
    public void sharedEmptyNotModified() throws NoSuchFieldException, IllegalAccessException {
        Field field = KeyPath.class.getDeclaredField("SHARED_EMPTY_PARTS");
        field.setAccessible(true);
        ArrayDeque<?> sharedEmptyParts = (ArrayDeque<?>) field.get(null);
        assertTrue(sharedEmptyParts.isEmpty());
    }
}
