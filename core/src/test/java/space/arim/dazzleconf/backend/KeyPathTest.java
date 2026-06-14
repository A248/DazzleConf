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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.reflect.Field;
import java.util.ArrayDeque;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static space.arim.dazzleconf.Utilities.assertEqualsBothWays;

public class KeyPathTest {

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void emptyPath(KeyPathVerify verify) {
        verify.assertEq(new KeyPath.Mut());
        assertTrue(new KeyPath.Mut().isEmpty());
        assertEquals(0, new KeyPath.Mut().size());
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void emptyPathImmut(KeyPathVerify verify) {
        verify.assertEq(new KeyPath.Immut());
        assertTrue(new KeyPath.Immut().isEmpty());
        assertEquals(0, new KeyPath.Immut().size());
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
    public void constructImmut() {
        String target = "my-brave-world.this-feature.enabled";
        String[] parts = new String[] {"my-brave-world", "this-feature", "enabled"};

        assertEquals(target, new KeyPath.Immut(parts).toString());
        assertArrayEquals(parts, new KeyPath.Immut(parts).intoParts());
        assertFalse(new KeyPath.Immut(parts).isEmpty());
        assertEquals(3, new KeyPath.Immut(parts).size());
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void rejectEmpty(KeyPathVerify verify) {
        assertThrows(IllegalArgumentException.class, () -> new KeyPath.Mut(""));
        assertThrows(IllegalArgumentException.class, () -> new KeyPath.Immut(""));
        {
            KeyPath.Mut keyPath = new KeyPath.Mut();
            assertThrows(IllegalArgumentException.class, () -> keyPath.addFront(""));
            verify.assertEq(keyPath);
            assertThrows(IllegalArgumentException.class, () -> keyPath.addBack(""));
            verify.assertEq(keyPath);
        }
        class SneakySequence implements CharSequence {
            private String delegate;

            @Override
            public int length() {
                return delegate.length();
            }

            @Override
            public char charAt(int index) {
                return delegate.charAt(index);
            }

            @Override
            public @NonNull CharSequence subSequence(int start, int end) {
                return delegate.subSequence(start, end);
            }

            @Override
            public @NonNull String toString() {
                return delegate;
            }
        }
        SneakySequence sneaky = new SneakySequence();
        sneaky.delegate = "hehe";
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront(sneaky);
        sneaky.delegate = "";
        assertThrows(IllegalArgumentException.class, keyPath::intoImmut);
        assertThrows(IllegalArgumentException.class, () -> new KeyPath.Immut(keyPath));
        KeyPath.Mut secondTry = new KeyPath.Mut();
        sneaky.delegate = "haha";
        secondTry.addBack(sneaky);
        sneaky.delegate = "";
        assertThrows(IllegalArgumentException.class, () -> new KeyPath.Immut(secondTry));
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void addFront(KeyPathVerify verify) {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("enabled");
        keyPath.addFront("this-feature");
        keyPath.addFront("my-brave-world");
        verify.assertEq(keyPath, "my-brave-world", "this-feature", "enabled");
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void addBack(KeyPathVerify verify) {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "my-brave-world", "this-feature", "enabled");
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void buildVariously1(KeyPathVerify verify) {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void buildVariously2(KeyPathVerify verify) {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void buildVariously3(KeyPathVerify verify) {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void buildVariously4(KeyPathVerify verify) {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void buildVariously5(KeyPathVerify verify) {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void buildVariously6(KeyPathVerify verify) {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath = new KeyPath.Mut(keyPath);
        keyPath.addBack("enabled");
        keyPath.addFront("section");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void buildVariously7(KeyPathVerify verify) {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addFront("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @ParameterizedTest
    @ArgumentsSource(KeyPathVerify.Provider.class)
    public void buildVariously8(KeyPathVerify verify) {
        KeyPath.Mut keyPath = new KeyPath.Mut();
        keyPath.addBack("my-brave-world");
        keyPath.addBack("this-feature");
        keyPath = keyPath.intoImmut().intoMut();
        keyPath.addFront("section");
        keyPath.addBack("enabled");
        verify.assertEq(keyPath, "section", "my-brave-world", "this-feature", "enabled");
    }

    @Test
    public void subSequences() {
        CharSequence keyPath = new KeyPath.Immut("ke", "paths").asCharSequence();
        assertEquals("", keyPath.subSequence(0, 0).toString());
        assertEquals("k", keyPath.subSequence(0, 1).toString());
        assertEquals("ke", keyPath.subSequence(0, 2).toString());
        assertEquals("ke.", keyPath.subSequence(0, 3).toString());
        assertEquals("ke.p", keyPath.subSequence(0, 4).toString());
        assertEquals("ke.pa", keyPath.subSequence(0, 5).toString());
        assertEquals("", keyPath.subSequence(1, 1).toString());
        assertEquals("e", keyPath.subSequence(1, 2).toString());
        assertEquals("e.", keyPath.subSequence(1, 3).toString());
        assertEquals("e.p", keyPath.subSequence(1, 4).toString());
        assertEquals("e.pa", keyPath.subSequence(1, 5).toString());
        assertEquals("", keyPath.subSequence(2, 2).toString());
        assertEquals(".", keyPath.subSequence(2, 3).toString());
        assertEquals(".p", keyPath.subSequence(2, 4).toString());
        assertEquals(".pa", keyPath.subSequence(2, 5).toString());
        assertEquals(".pat", keyPath.subSequence(2, 6).toString());
        assertEquals(".paths", keyPath.subSequence(2, 8).toString());
        assertEquals("", keyPath.subSequence(3, 3).toString());
        assertEquals("p", keyPath.subSequence(3, 4).toString());
        assertEquals("pa", keyPath.subSequence(3, 5).toString());
        assertEquals("pat", keyPath.subSequence(3, 6).toString());
        assertEquals("paths", keyPath.subSequence(3, 8).toString());
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
        assertEquals(original, original.intoImmut());
        assertEquals(copy, original.intoImmut());
    }

    @Test
    public void equalityEmpty() {
        assertEqualsBothWays(new KeyPath.Mut(), KeyPath.empty());
        assertEqualsBothWays(new KeyPath.Immut(), KeyPath.empty());
        assertEqualsBothWays(new KeyPath.Mut(), new KeyPath.Immut());
    }

    @AfterEach
    public void sharedEmptyNotModified() throws NoSuchFieldException, IllegalAccessException {
        Field field = KeyPath.Base.class.getDeclaredField("SHARED_EMPTY_PARTS");
        field.setAccessible(true);
        ArrayDeque<?> sharedEmptyParts = (ArrayDeque<?>) field.get(null);
        assertTrue(sharedEmptyParts.isEmpty());
    }
}
