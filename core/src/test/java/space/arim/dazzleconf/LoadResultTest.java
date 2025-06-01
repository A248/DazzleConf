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

package space.arim.dazzleconf;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Printable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class LoadResultTest {

    @Test
    public void success() {
        LoadResult<String> loadResult = LoadResult.of("datum");
        assertTrue(loadResult.isSuccess());
        assertFalse(loadResult.isFailure());
        assertEquals("datum", loadResult.getValue());
        assertEquals("datum", loadResult.getOrThrow());
        assertEquals(List.of(), loadResult.getErrorContexts());
        assertTrue(loadResult.toString().contains("datum"));

        LoadResult<Integer> mapped = loadResult.map(String::length);
        assertTrue(mapped.isSuccess());
        assertEquals(5, mapped.getOrThrow());
        LoadResult<Integer> flatMapped = loadResult.flatMap((s) -> LoadResult.of(s.length() + 1));
        assertTrue(flatMapped.isSuccess());
        assertEquals(6, flatMapped.getOrThrow());
        LoadResult<Integer> flatMappedError = loadResult.flatMap((s) -> LoadResult.failure(List.of()));
        assertFalse(flatMappedError.isSuccess());
    }

    @Test
    public void successNullable() {
        LoadResult<String> loadResult = LoadResult.of(null);
        assertTrue(loadResult.isSuccess());
        assertFalse(loadResult.isFailure());
        assertNull(loadResult.getValue());
        assertNull(loadResult.getOrThrow());
        assertEquals(List.of(), loadResult.getErrorContexts());
        assertTrue(loadResult.toString().contains("null"));

        LoadResult<Boolean> mapped = loadResult.map(Objects::isNull);
        assertTrue(mapped.isSuccess());
        assertTrue(mapped.getOrThrow());

    }

    @Test
    public void failure(@Mock ErrorContext dummyErrorOne, @Mock ErrorContext dummyErrorTwo) {
        lenient().when(dummyErrorOne.mainMessage()).thenReturn(Printable.preBuilt("failed for xyz"));
        lenient().when(dummyErrorOne.toString()).thenReturn("failed for xyz");

        LoadResult<String> loadResult = LoadResult.failure(dummyErrorOne, dummyErrorTwo);
        assertFalse(loadResult.isSuccess());
        assertTrue(loadResult.isFailure());
        assertNull(loadResult.getValue());
        assertThrows(NoSuchElementException.class, loadResult::getOrThrow);
        assertEquals(List.of(dummyErrorOne, dummyErrorTwo), loadResult.getErrorContexts());
        assertTrue(loadResult.toString().contains(dummyErrorOne.toString()));

        LoadResult<Integer> mapped = loadResult.map(String::length);
        assertFalse(mapped.isSuccess());
        LoadResult<Integer> flatMapped = loadResult.flatMap((s) -> LoadResult.of(s.length() + 1));
        assertFalse(flatMapped.isSuccess());
        LoadResult<Integer> flatMappedError = loadResult.flatMap((s) -> LoadResult.failure(List.of()));
        assertEquals(List.of(dummyErrorOne, dummyErrorTwo), flatMappedError.getErrorContexts());
    }

    @Test
    public void equality() {
        EqualsVerifier.forClass(LoadResult.class).verify();
    }
}
