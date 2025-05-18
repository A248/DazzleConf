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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.*;
import space.arim.dazzleconf2.engine.CommentLocation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CachedBackendTest {

    @Test
    public void readOnce(@Mock Backend delegate) {
        DataTree.Immut data;
        {
            DataTree.Mut builder = new DataTree.Mut();
            builder.set("k1", new DataEntry("v1"));
            builder.set("k2", new DataEntry("v2"));
            builder.set("k3", new DataEntry("v3"));
            data = builder.intoImmut();
        }
        when(delegate.read()).thenAnswer((i) -> LoadResult.of(data));
        CachedBackend backend = new CachedBackend(delegate);
        assertEquals(data, backend.read().getOrThrow());
        assertEquals(data, backend.read().getOrThrow());
        verify(delegate, times(1)).read();
    }

    @Test
    public void refreshOnWrite(@Mock Backend delegate) {
        DataTree.Immut initialData;
        {
            DataTree.Mut builder = new DataTree.Mut();
            builder.set("k1", new DataEntry("v1"));
            builder.set("k2", new DataEntry("v2"));
            builder.set("k3", new DataEntry("v3"));
            initialData = builder.intoImmut();
        }
        DataTree.Immut empty = new DataTree.Immut();

        when(delegate.read()).thenAnswer((i) -> LoadResult.of(initialData));
        CachedBackend backend = new CachedBackend(delegate);
        assertEquals(initialData, backend.read().getOrThrow());
        assertEquals(initialData, backend.read().getOrThrow());
        backend.write(null, empty);
        assertEquals(empty, backend.read().getOrThrow());
        assertEquals(empty, backend.read().getOrThrow());

        verify(delegate, times(1)).read();
    }

    @Test
    public void writePassthrough(@Mock Backend delegate) {
        DataTree.Immut data;
        {
            DataTree.Mut builder = new DataTree.Mut();
            builder.set("k1", new DataEntry("v1"));
            builder.set("k2", new DataEntry("v2"));
            builder.set("k3", new DataEntry("v3"));
            data = builder.intoImmut();
        }
        CachedBackend backend = new CachedBackend(delegate);
        backend.write(null, data);
        backend.write(null, data);
        verify(delegate, times(2)).write(null, data);
    }

    @Test
    public void supportsComments(@Mock Backend delegate) {
        when(delegate.supportsComments(CommentLocation.BELOW)).thenReturn(true);
        CachedBackend backend = new CachedBackend(delegate);
        assertTrue(backend.supportsComments(CommentLocation.BELOW));
        assertFalse(backend.supportsComments(CommentLocation.INLINE));
    }

    @Test
    public void recommendKeyMapper(@Mock Backend delegate, @Mock KeyMapper keyMapper) {
        when(delegate.recommendKeyMapper()).thenReturn(keyMapper);
        assertSame(keyMapper, new CachedBackend(delegate).recommendKeyMapper());
    }
}
