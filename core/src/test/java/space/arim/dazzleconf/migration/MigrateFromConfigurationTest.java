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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.*;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DefaultKeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.engine.liaison.StringLiaison;
import space.arim.dazzleconf2.migration.MigrateContext;
import space.arim.dazzleconf2.migration.MigrateFromConfiguration;
import space.arim.dazzleconf2.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MigrateFromConfigurationTest {

    private Configuration<Config> configuration;
    private final MigrateContext migrateContext;
    private final Backend mainBackend;
    private final LoadListener loadListener;

    public MigrateFromConfigurationTest(@Mock MigrateContext migrateContext, @Mock Backend mainBackend,
                                        @Mock LoadListener loadListener) {
        this.migrateContext = migrateContext;
        this.mainBackend = mainBackend;
        this.loadListener = loadListener;
    }

    public interface Config {

        default String source() {
            return "fallback";
        }
    }

    @BeforeEach
    public void setup(@Mock ErrorContext.Source errorSource, @Mock ErrorContext dummyError) {
        configuration = new ConfigurationBuilder<>(new TypeToken<Config>() {})
                .addTypeLiaisons(new StringLiaison())
                .build();
        lenient().when(mainBackend.recommendKeyMapper()).thenReturn(new DefaultKeyMapper());
        lenient().when(migrateContext.loadListener()).thenReturn(loadListener);
        lenient().when(migrateContext.errorSource()).thenReturn(errorSource);
        lenient().when(errorSource.throwError((CharSequence) any())).thenReturn(LoadResult.failure(dummyError));
    }

    @Test
    public void nullTreeYieldsError() {
        when(mainBackend.read(any())).thenReturn(LoadResult.of(null));
        when(migrateContext.mainBackend()).thenReturn(mainBackend);
        LoadResult<Config> loadResult = new MigrateFromConfiguration<>(configuration).load(migrateContext);
        assertFalse(loadResult.isSuccess());
    }

    @Test
    public void failedTreeYieldsError(@Mock ErrorContext errorContext) {
        when(mainBackend.read(any())).thenReturn(LoadResult.failure(errorContext));
        when(migrateContext.mainBackend()).thenReturn(mainBackend);
        LoadResult<Config> loadResult = new MigrateFromConfiguration<>(configuration).load(migrateContext);
        assertFalse(loadResult.isSuccess());
        assertEquals(errorContext, loadResult.getErrorContexts().getFirst());
    }

    @Test
    public void successYieldsValue() {
        DataTree.Mut backendTree = new DataTree.Mut();
        backendTree.set("source", new DataEntry("yay"));
        when(mainBackend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(backendTree)));
        when(migrateContext.mainBackend()).thenReturn(mainBackend);
        LoadResult<Config> loadResult = new MigrateFromConfiguration<>(configuration).load(migrateContext);
        assertTrue(loadResult.isSuccess());
        assertEquals("yay", loadResult.getOrThrow().source());
    }

    @Test
    public void listenToUpdates() {
        DataTree.Mut backendTree = new DataTree.Mut();
        backendTree.set("other", new DataEntry("no!"));
        when(mainBackend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(backendTree)));
        when(migrateContext.mainBackend()).thenReturn(mainBackend);
        LoadResult<Config> loadResult = new MigrateFromConfiguration<>(configuration).load(migrateContext);
        assertTrue(loadResult.isSuccess());
        assertEquals("fallback", loadResult.getOrThrow().source());
        verify(loadListener).updatedPath(new KeyPath.Mut("source"), UpdateReason.MIGRATED);
    }

    @Test
    public void filterNotUsable() {
        DataTree.Mut backendTree = new DataTree.Mut();
        backendTree.set("other", new DataEntry("no!"));
        when(mainBackend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(backendTree)));
        when(migrateContext.mainBackend()).thenReturn(mainBackend);
        // Add a filter with exactly the value that is going to happen
        LoadResult<Config> loadResult = new MigrateFromConfiguration<>(configuration)
                .addFilter(config -> !config.source().equals("fallback"))
                .load(migrateContext);
        assertFalse(loadResult.isSuccess());
        verifyNoInteractions(loadListener);
    }

    @Test
    public void filterStillUsable() {
        DataTree.Mut backendTree = new DataTree.Mut();
        backendTree.set("source", new DataEntry("yay"));
        when(mainBackend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(backendTree)));
        when(migrateContext.mainBackend()).thenReturn(mainBackend);
        LoadResult<Config> loadResult = new MigrateFromConfiguration<>(configuration)
                .addFilter(config -> !config.source().equals("fallback"))
                .load(migrateContext);
        assertTrue(loadResult.isSuccess());
        assertEquals("yay", loadResult.getOrThrow().source());
    }
}
