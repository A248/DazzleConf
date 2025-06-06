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
import space.arim.dazzleconf2.engine.UpdateListener;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.engine.liaison.StringDefault;
import space.arim.dazzleconf2.migration.MigrateContext;
import space.arim.dazzleconf2.migration.MigrateSource;
import space.arim.dazzleconf2.migration.Migration;
import space.arim.dazzleconf2.migration.Transition;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MigrationTest {

    private final Backend backend;

    public MigrationTest(@Mock Backend backend) {
        this.backend = backend;
    }

    @BeforeEach
    public void setKeyMapper() {
        when(backend.recommendKeyMapper()).thenReturn(new DefaultKeyMapper());
    }

    public interface Destination {

        @StringDefault(value = "goodbye", ifMissing = "goodbye-default-if-missing")
        String hello();

        default char affirmative() {
            return 'y';
        }
    }

    @Test
    public void migrate(@Mock UpdateListener updateListener) {

        AtomicInteger runs = new AtomicInteger();
        Migration<String, Destination> migration = new Migration<>(
                new MigrateSource<String>() {
                    @Override
                    public @NonNull LoadResult<@NonNull String> load(@NonNull MigrateContext migrateContext) {
                        DataTree dataTree = migrateContext.mainBackend().read(any()).getOrThrow().data();
                        DataEntry oldHello = dataTree.get("old-hello");
                        assertNotNull(oldHello);
                        return LoadResult.of((String) oldHello.getValue());
                    }

                    @Override
                    public void onCompletion() {
                        runs.getAndIncrement();
                    }

                    @Override
                    public @NonNull MigrateSource<String> addFilter(@NonNull Filter<String> filter) {
                        throw new UnsupportedOperationException();
                    }
                },
                (previous, migrateContext) -> {
                    migrateContext.loadListener().updatedPath(new KeyPath.Immut("affirmative"), UpdateReason.MIGRATED);
                    return new Destination() {
                        @Override
                        public String hello() {
                            return previous;
                        }

                        @Override
                        public char affirmative() {
                            return 'W';
                        }
                    };
                }
        );
        Configuration<Destination> config = Configuration
                .defaultBuilder(Destination.class)
                .addMigration(migration)
                .build();
        DataTree.Mut sourceTree = new DataTree.Mut();
        sourceTree.set("version", new DataEntry("old"));
        sourceTree.set("old-hello", new DataEntry("old-goodbye"));
        when(backend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(sourceTree)));
        Destination migrated = config.configureWith(backend).getOrThrow();
        assertEquals("old-goodbye", migrated.hello());
        assertEquals('W', migrated.affirmative());
        assertEquals(1, runs.get());

        Destination again = config.configureWith(backend, updateListener).getOrThrow();
        assertEquals("old-goodbye", again.hello());
        assertEquals('W', again.affirmative());
        assertEquals(2, runs.get());
        verify(updateListener).migratedFrom(migration);
        verify(updateListener).updatedPath(new KeyPath.Immut("affirmative"), UpdateReason.MIGRATED);
        verifyNoMoreInteractions(updateListener);
    }

    @Test
    public void migrateNotApplicable(@Mock ErrorContext dummyError,
                                     @Mock Transition<String, Destination> dummyTransition,
                                     @Mock UpdateListener updateListener) {
        Migration<String, Destination> migration = new Migration<>(
                new MigrateSource<>() {
                    @Override
                    public @NonNull LoadResult<@NonNull String> load(@NonNull MigrateContext migrateContext) {
                        return LoadResult.failure(dummyError);
                    }

                    @Override
                    public void onCompletion() {
                        throw new AssertionError("Does not complete");
                    }

                    @Override
                    public @NonNull MigrateSource<String> addFilter(@NonNull Filter<String> filter) {
                        throw new UnsupportedOperationException();
                    }
                },
                dummyTransition
        );
        Configuration<Destination> config = Configuration
                .defaultBuilder(Destination.class)
                .addMigration(migration)
                .build();
        when(backend.read(any())).thenReturn(LoadResult.of(null));
        Destination defaultValues = config.configureWith(backend, updateListener).getOrThrow();
        assertEquals("goodbye", defaultValues.hello());
        assertEquals('y', defaultValues.affirmative());
        verify(updateListener).migrationSkip(migration, List.of(dummyError));
        verify(updateListener).loadedDefaults();
        verifyNoInteractions(dummyTransition);
    }

    @Test
    public void multipleApplicable(@Mock ErrorContext dummyError,
                                   @Mock Transition<String, Destination> dummyTransition,
                                   @Mock UpdateListener updateListener) {
        Migration<String, Destination> skipMigration = new Migration<>(
                new MigrateSource<>() {
                    @Override
                    public @NonNull LoadResult<@NonNull String> load(@NonNull MigrateContext migrateContext) {
                        return LoadResult.failure(dummyError);
                    }

                    @Override
                    public void onCompletion() {
                        throw new AssertionError("Does not complete");
                    }

                    @Override
                    public @NonNull MigrateSource<String> addFilter(@NonNull Filter<String> filter) {
                        throw new UnsupportedOperationException();
                    }
                },
                dummyTransition
        );
        Migration<String, Destination> firstApplicable = new Migration<>(
                new MigrateSource<String>() {
                    @Override
                    public @NonNull LoadResult<@NonNull String> load(@NonNull MigrateContext migrateContext) {
                        DataTree dataTree = migrateContext.mainBackend().read(any()).getOrThrow().data();
                        DataEntry oldHello =  dataTree.get("old-hello");
                        assertNotNull(oldHello);
                        return LoadResult.of(oldHello.getValue() + "-first");
                    }

                    @Override
                    public void onCompletion() {}

                    @Override
                    public @NonNull MigrateSource<String> addFilter(@NonNull Filter<String> filter) {
                        throw new UnsupportedOperationException();
                    }
                },
                ((Transition<String, String>) (previous, migrateContext) -> { return previous + "-chain"; }).chain(
                        (previous, migrateContext) -> new Destination() {
                            @Override
                            public String hello() {
                                return previous;
                            }

                            @Override
                            public char affirmative() {
                                return '1';
                            }
                        }
                )
        );
        Migration<String, Destination> secondApplicable = new Migration<>(
                new MigrateSource<String>() {
                    @Override
                    public @NonNull LoadResult<@NonNull String> load(@NonNull MigrateContext migrateContext) {
                        DataTree dataTree = migrateContext.mainBackend().read(any()).getOrThrow().data();
                        DataEntry oldHello =  dataTree.get("old-hello");
                        assertNotNull(oldHello);
                        return LoadResult.of(oldHello.getValue() + "-second");
                    }

                    @Override
                    public void onCompletion() {}

                    @Override
                    public @NonNull MigrateSource<String> addFilter(@NonNull Filter<String> filter) {
                        throw new UnsupportedOperationException();
                    }
                },
                (previous, migrateContext) -> new Destination() {
                    @Override
                    public String hello() {
                        return previous;
                    }

                    @Override
                    public char affirmative() {
                        return '2';
                    }
                }
        );
        Configuration<Destination> config = Configuration
                .defaultBuilder(Destination.class)
                .addMigration(skipMigration)
                .addMigrations(List.of(firstApplicable, secondApplicable))
                .build();
        assertEquals(List.of(skipMigration, firstApplicable, secondApplicable), config.getMigrations());

        DataTree.Mut sourceTree = new DataTree.Mut();
        sourceTree.set("version", new DataEntry("old"));
        sourceTree.set("old-hello", new DataEntry("old-goodbye"));
        when(backend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(sourceTree)));
        Destination migrated = config.configureWith(backend, updateListener).getOrThrow();
        assertEquals("old-goodbye-first-chain", migrated.hello());
        assertEquals('1', migrated.affirmative());

        // Check update listener
        verify(updateListener).migrationSkip(skipMigration, List.of(dummyError));
        verify(updateListener).migratedFrom(firstApplicable);
        verifyNoMoreInteractions(updateListener);

        // Check the data that was written back
        DataTree.Mut expectedWriteBack = new DataTree.Mut();
        expectedWriteBack.set("hello", new DataEntry("old-goodbye-first-chain"));
        expectedWriteBack.set("affirmative", new DataEntry('1'));
        verify(backend).write(argThat(new MatchDocumentData(expectedWriteBack)));
    }

}
