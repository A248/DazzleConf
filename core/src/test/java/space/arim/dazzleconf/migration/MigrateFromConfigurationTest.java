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
import space.arim.dazzleconf.Configuration;
import space.arim.dazzleconf.ConfigurationBuilder;
import space.arim.dazzleconf.ErrorContext;
import space.arim.dazzleconf.LoadResult;
import space.arim.dazzleconf.backend.Backend;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.DefaultKeyMapper;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.UpdateReason;
import space.arim.dazzleconf.engine.liaison.StringLiaison;
import space.arim.dazzleconf.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MigrateFromConfigurationTest {

    private Configuration<Config> configuration;
    private final MigrateContext migrateContext;
    private final Backend mainBackend;

    public MigrateFromConfigurationTest(@Mock MigrateContext migrateContext, @Mock Backend mainBackend) {
        this.migrateContext = migrateContext;
        this.mainBackend = mainBackend;
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
        backendTree.put("source", new DataEntry("yay"));
        when(mainBackend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(backendTree)));
        when(migrateContext.mainBackend()).thenReturn(mainBackend);
        LoadResult<Config> loadResult = new MigrateFromConfiguration<>(configuration).load(migrateContext);
        assertTrue(loadResult.isSuccess());
        assertEquals("yay", loadResult.getOrThrow().source());
    }

    @Test
    public void listenToUpdates() {
        DataTree.Mut backendTree = new DataTree.Mut();
        backendTree.put("other", new DataEntry("no!"));
        when(mainBackend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(backendTree)));
        when(migrateContext.mainBackend()).thenReturn(mainBackend);
        LoadResult<Config> loadResult = new MigrateFromConfiguration<>(configuration).load(migrateContext);
        assertTrue(loadResult.isSuccess());
        assertEquals("fallback", loadResult.getOrThrow().source());
        verify(migrateContext).notifyUpdate(new KeyPath.Mut("source"), UpdateReason.MIGRATED);
    }

    @Test
    public void filterNotUsable() {
        DataTree.Mut backendTree = new DataTree.Mut();
        backendTree.put("other", new DataEntry("no!"));
        when(mainBackend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(backendTree)));
        when(migrateContext.mainBackend()).thenReturn(mainBackend);
        // Add a filter with exactly the value that is going to happen
        LoadResult<Config> loadResult = new MigrateFromConfiguration<>(configuration)
                .addFilter(config -> !config.source().equals("fallback"))
                .load(migrateContext);
        assertFalse(loadResult.isSuccess());
        verify(migrateContext, never()).notifyUpdate(any(), any());
    }

    @Test
    public void filterStillUsable() {
        DataTree.Mut backendTree = new DataTree.Mut();
        backendTree.put("source", new DataEntry("yay"));
        when(mainBackend.read(any())).thenReturn(LoadResult.of(Backend.Document.simple(backendTree)));
        when(migrateContext.mainBackend()).thenReturn(mainBackend);
        LoadResult<Config> loadResult = new MigrateFromConfiguration<>(configuration)
                .addFilter(config -> !config.source().equals("fallback"))
                .load(migrateContext);
        assertTrue(loadResult.isSuccess());
        assertEquals("yay", loadResult.getOrThrow().source());
    }
}
