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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.ErrorPrint;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DefaultKeyMapper;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.ConfigureListener;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ErrorHandlingTest {

    private final Backend backend;
    private final ErrorPrint errorPrint;

    public ErrorHandlingTest(@Mock Backend backend, @Mock ErrorPrint errorPrint) {
        this.backend = backend;
        this.errorPrint = errorPrint;
    }

    public interface Config {

        default int integral() {
            return -1;
        }

    }

    @BeforeEach
    public void setup() {
        when(backend.recommendKeyMapper()).thenReturn(new DefaultKeyMapper());
        lenient().when(backend.meta()).thenReturn(new Backend.Meta() {
            @Override
            public boolean supportsComments(boolean documentLevel, boolean reading, @NonNull CommentLocation location) {
                return true;
            }

            @Override
            public boolean preservesOrder(boolean reading) {
                return true;
            }

            @Override
            public boolean writesFloatAsDouble() {
                return false;
            }

            @Override
            public boolean allKeysAreStrings() {
                return false;
            }
        });
    }

    @Test
    public void noErrors() {
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("integral", new DataEntry(1));
        when(backend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(dataTree)));
        Config loaded = configuration.configureOrFallback(backend, errorPrint);
        verifyNoInteractions(errorPrint);
        assertEquals(1, loaded.integral());
    }

    @Test
    public void noErrors(@Mock ConfigureListener configureListener) {
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("integral", new DataEntry(1));
        when(backend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(dataTree)));
        Config loaded = configuration.configureOrFallback(backend, configureListener, errorPrint);
        verifyNoInteractions(errorPrint);
        verifyNoInteractions(configureListener);
        assertEquals(1, loaded.integral());
    }

    @Test
    public void errorFromBackend(@Mock ErrorContext errorContext) {
        Configuration<Config> config = Configuration
                .defaultBuilder(Config.class)
                .build();
        when(backend.read(any())).thenReturn(LoadResult.failure(errorContext));

        assertEquals(-1, config.configureOrFallback(backend, errorPrint).integral());
        verify(errorPrint).onError(List.of(errorContext));
    }

    @Test
    public void badValue() {
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("integral", new DataEntry("not an integer"));
        when(backend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(dataTree)));
        Config loaded = configuration.configureOrFallback(backend, errorPrint);
        verify(errorPrint).onError(argThat(list -> !list.isEmpty()));
        assertEquals(-1, loaded.integral(), "fallback to default value");
    }

    @Test
    public void badValue(@Mock ConfigureListener configureListener) {
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("integral", new DataEntry("not an integer"));
        when(backend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(dataTree)));
        Config loaded = configuration.configureOrFallback(backend, configureListener, errorPrint);
        verify(errorPrint).onError(argThat(list -> !list.isEmpty()));
        verifyNoInteractions(configureListener);
        assertEquals(-1, loaded.integral(), "fallback to default value");
    }
}
