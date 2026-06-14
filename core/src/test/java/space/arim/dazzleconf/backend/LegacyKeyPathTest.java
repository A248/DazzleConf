/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static space.arim.dazzleconf.Utilities.assertEqualsBothWays;
import static space.arim.dazzleconf.Utilities.assertNotEqualsBothWays;

@ParameterizedClass
@ArgumentsSource(KeyPathVerify.Provider.class)
public class LegacyKeyPathTest {

    private final KeyPathVerify verify;

    public LegacyKeyPathTest(KeyPathVerify verify) {
        this.verify = verify;
    }

    @Test
    public void addFrontKeyMap() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("thisFeature");
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        verify.assertEq(keyPath, "this-feature");
    }

    @Test
    public void addFrontKeyMapPostCall() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addFront("thisFeature");
        verify.assertEq(keyPath, "thisFeature");
    }

    @Test
    public void addBackKeyMap() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("thisFeature");
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        verify.assertEq(keyPath, "this-feature");
    }

    @Test
    public void addBackKeyMapPostCall() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addBack("thisFeature");
        verify.assertEq(keyPath, "thisFeature");
    }

    @Test
    public void buildVariously9() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addBack("enabled");
        keyPath.addFront("sectionMapped");
        verify.assertEq(keyPath, "sectionMapped", "myBraveWorld", "thisFeature", "enabled");
    }

    @Test
    public void buildVariously10() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addBack("enabled");
        keyPath.addFront("sectionMapped");
        verify.assertEq(keyPath, "sectionMapped", "myBraveWorld", "thisFeature", "enabled");
    }

    @Test
    public void buildVariously11() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "sectionMapped", "myBraveWorld", "thisFeature", "enabled");
    }

    @Test
    public void buildVariously12() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "sectionMapped", "myBraveWorld", "thisFeature", "enabled");
    }

    @Test
    public void buildVariously13() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addBack("enabled");
        keyPath.addFront("sectionMapped");
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        verify.assertEq(keyPath, "section-mapped", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously14() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addBack("enabled");
        keyPath.addFront("sectionMapped");
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        verify.assertEq(keyPath, "section-mapped", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously15() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        verify.assertEq(keyPath, "section-mapped", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously16() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        verify.assertEq(keyPath, "section-mapped", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously17() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "sectionMapped", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void buildVariously18() {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath.addBack("myBraveWorld");
        keyPath.addBack("thisFeature");
        keyPath.applyKeyMapper(new KebabCaseKeyMapper());
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addFront("sectionMapped");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "sectionMapped", "my-brave-world", "this-feature", "enabled");
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
        KeyMapper keyMapper = new KebabCaseKeyMapper();
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
        Field field = KeyPath.Base.class.getDeclaredField("SHARED_EMPTY_PARTS");
        field.setAccessible(true);
        ArrayDeque<?> sharedEmptyParts = (ArrayDeque<?>) field.get(null);
        assertTrue(sharedEmptyParts.isEmpty());
    }
}
