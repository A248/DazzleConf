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
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.*;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.ConfigureListener;
import space.arim.dazzleconf2.engine.UpdateReason;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfigureTest {

    private final Backend backend;

    public ConfigureTest(@Mock Backend backend) {
        this.backend = backend;
    }

    public interface Config {
        default String hello() {
            return "goodbye";
        }

        default char affirmative() {
            return 'y';
        }
    }

    @BeforeEach
    public void setup() {
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
    public void nullKeyMapper() {
        Configuration<Config> config = Configuration.defaultBuilder(Config.class).build();
        when(backend.recommendKeyMapper()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> config.configureWith(backend));
    }

    @Test
    public void erroredOut(@Mock ErrorContext errorContext) {
        Configuration<Config> config = Configuration.defaultBuilder(Config.class).build();
        when(backend.recommendKeyMapper()).thenReturn(new DefaultKeyMapper());
        when(backend.read(any())).thenReturn(LoadResult.failure(errorContext));
        LoadResult<Config> configureWith = config.configureWith(backend);
        assertTrue(configureWith.isFailure());
        assertEquals(List.of(errorContext), configureWith.getErrorContexts());
    }

    @Test
    public void loadDefaults(@Mock ConfigureListener configureListener) {
        Configuration<MigrationTest.Destination> config = Configuration
                .defaultBuilder(MigrationTest.Destination.class)
                .build();
        when(backend.recommendKeyMapper()).thenReturn(new DefaultKeyMapper());
        when(backend.read(any())).thenReturn(LoadResult.of(null));
        MigrationTest.Destination defaultValues = config.configureWith(backend, configureListener).getOrThrow();
        assertEquals("goodbye", defaultValues.hello());
        assertEquals('y', defaultValues.affirmative());

        // Check update listener
        verify(configureListener).wroteDefaults();
        verifyNoMoreInteractions(configureListener);

        // Check the data that was written back
        DataTree.Mut expectedWriteBack = new DataTree.Mut();
        expectedWriteBack.set("hello", new DataEntry("goodbye"));
        expectedWriteBack.set("affirmative", new DataEntry('y'));
        verify(backend).write(argThat(new MatchDocumentData(expectedWriteBack)));
    }

    @Test
    public void loadMissingValue(@Mock ConfigureListener configureListener) {
        Configuration<MigrationTest.Destination> config = Configuration
                .defaultBuilder(MigrationTest.Destination.class)
                .build();
        when(backend.recommendKeyMapper()).thenReturn(new DefaultKeyMapper());
        when(backend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(new DataTree.Immut())));
        MigrationTest.Destination withMissing = config.configureWith(backend, configureListener).getOrThrow();
        assertEquals("goodbye-default-if-missing", withMissing.hello());
        assertEquals('y', withMissing.affirmative());

        // Check update listener
        verify(configureListener).notifyUpdate(eq(new KeyPath.Mut("hello")), eq(UpdateReason.MISSING));
        verify(configureListener).notifyUpdate(eq(new KeyPath.Mut("affirmative")), eq(UpdateReason.MISSING));
        verifyNoMoreInteractions(configureListener);

        // Check the data that was written back
        DataTree.Mut expectedWriteBack = new DataTree.Mut();
        expectedWriteBack.set("hello", new DataEntry("goodbye-default-if-missing"));
        expectedWriteBack.set("affirmative", new DataEntry('y'));
        verify(backend).write(argThat(new MatchDocumentData(expectedWriteBack)));
    }

    @Test
    public void loadPartialMissingValue(@Mock ConfigureListener configureListener) {
        Configuration<MigrationTest.Destination> config = Configuration
                .defaultBuilder(MigrationTest.Destination.class)
                .build();
        when(backend.recommendKeyMapper()).thenReturn(new DefaultKeyMapper());
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("hello", new DataEntry("present"));
        when(backend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(dataTree)));
        MigrationTest.Destination withMissing = config.configureWith(backend, configureListener).getOrThrow();
        assertEquals("present", withMissing.hello());
        assertEquals('y', withMissing.affirmative());

        // Check update listener
        verify(configureListener).notifyUpdate(eq(new KeyPath.Mut("affirmative")), eq(UpdateReason.MISSING));
        verifyNoMoreInteractions(configureListener);

        // Check the data that was written back
        DataTree.Mut expectedWriteBack = new DataTree.Mut();
        expectedWriteBack.set("hello", new DataEntry("present"));
        expectedWriteBack.set("affirmative", new DataEntry('y'));
        verify(backend).write(argThat(new MatchDocumentData(expectedWriteBack)));
    }
}
