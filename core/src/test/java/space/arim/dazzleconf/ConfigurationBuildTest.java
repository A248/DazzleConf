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
import space.arim.dazzleconf2.ConfigurationBuilder;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.backend.DefaultKeyMapper;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.reflect.DefaultReflectionService;
import space.arim.dazzleconf2.reflect.ReflectionService;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.lang.invoke.MethodHandles;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConfigurationBuildTest {

    public interface Config<A> {}

    private final TypeToken<Config<String>> configType = new TypeToken<>() {};

    @Test
    public void locale() {
        Locale locale = Locale.CHINA;
        assertSame(locale, Configuration.defaultBuilder(configType).locale(locale).build().getLocale());
    }

    @Test
    public void clearLiaisons() {
        assertFalse(Configuration.defaultBuilder(configType).build().getTypeLiaisons().isEmpty());
        assertTrue(Configuration.defaultBuilder(configType).clearTypeLiaisons().build().getTypeLiaisons().isEmpty());
    }

    @Test
    public void keyMapper() {
        assertNull(Configuration.defaultBuilder(configType).build().getKeyMapper());
        KeyMapper keyMapper = new DefaultKeyMapper();
        assertEquals(keyMapper, Configuration.defaultBuilder(configType).keyMapper(keyMapper).build().getKeyMapper());
    }

    @Test
    public void reflectionService(@Mock ReflectionService reflectionService) {
        DefaultReflectionService defaultReflectionService = new DefaultReflectionService();
        when(reflectionService.makeInstantiator(any())).thenReturn(defaultReflectionService.makeInstantiator(MethodHandles.lookup()));
        when(reflectionService.makeMethodMirror(any())).thenReturn(defaultReflectionService.makeMethodMirror(MethodHandles.lookup()));
        ConfigurationBuilder<?> builder = Configuration.defaultBuilder(configType).reflectionService(reflectionService);
        Configuration.Layout layout = assertDoesNotThrow(builder::build).getLayout();
        assertNotNull(layout.getInstantiator());
        assertNotNull(layout.getMethodMirror());
        verify(reflectionService).makeInstantiator(any());
        verify(reflectionService).makeMethodMirror(any());
    }

    @Test
    public void type() {
        assertSame(configType, Configuration.defaultBuilder(configType).build().getType());
        assertThrows(NullPointerException.class, () -> Configuration.defaultBuilder((Class<?>) null));
        assertThrows(NullPointerException.class, () -> Configuration.defaultBuilder((TypeToken<? extends Object>) null));
        assertThrows(DeveloperMistakeException.class, () -> Configuration.defaultBuilder(Config.class));
    }
}
