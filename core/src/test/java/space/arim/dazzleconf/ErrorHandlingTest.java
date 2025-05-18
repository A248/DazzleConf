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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.ErrorPrint;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ErrorHandlingTest {

    private final Backend backend;
    private final ErrorPrint errorPrint;

    public ErrorHandlingTest(@Mock Backend backend, @Mock ErrorPrint errorPrint) {
        this.backend = backend;
        this.errorPrint = errorPrint;
    }

    @Test
    public void success() {
        Configuration<HelloWorld> config = Configuration
                .defaultBuilder(HelloWorld.class)
                .build();
        assertEquals("hi", config.loadDefaults().hello());

        DataTree.Mut sourceTree = new DataTree.Mut();
        sourceTree.set("hello", new DataEntry("goodbye"));
        when(backend.read()).thenAnswer((i) -> LoadResult.of((DataStreamable) sourceTree));
        when(backend.recommendKeyMapper()).thenReturn(new DefaultKeyMapper());

        assertEquals("goodbye", config.configureOrFallback(backend, errorPrint).hello());
        verifyNoInteractions(errorPrint);
    }

    public interface HelloWorld {

        default String hello() {
            return "hi";
        }
    }

}
