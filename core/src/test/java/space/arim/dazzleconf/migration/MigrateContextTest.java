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

package space.arim.dazzleconf.migration;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.ErrorFactory;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.migration.MigrateContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
public class MigrateContextTest {

    @Test
    public void withLoadListener(@Mock Backend backend, @Mock LoadListener originalListener, @Mock LoadListener newListener) {
        class OriginalCtx extends ErrorFactory implements MigrateContext {

            @Override
            public @NonNull Backend mainBackend() {
                return backend;
            }

            @Override
            public @NonNull LoadListener loadListener() {
                return originalListener;
            }
        }
        MigrateContext originalCtx = new OriginalCtx();
        MigrateContext newCtx = originalCtx.withLoadListener(newListener);
        assertSame(newListener, newCtx.loadListener());
        assertSame(backend, newCtx.mainBackend());
        assertNotNull(newCtx.buildError(Printable.preBuilt("1")));
        assertNotNull(newCtx.throwError(Printable.preBuilt("2")));
        assertNotNull(newCtx.throwError("3"));
    }
}
