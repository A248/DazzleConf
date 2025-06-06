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

package space.arim.dazzleconf.backend.yaml;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.backend.StringRoot;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class NonStringKeysTest {

    @Test
    public void integerKeysSupported(@Mock ErrorContext.Source errorSource) {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set(1, new DataEntry("hello"));
        dataTree.set("option", new DataEntry(false));
        DataTree.Mut subTree = new DataTree.Mut();
        dataTree.set(-5, new DataEntry(subTree));
        subTree.set("hi", new DataEntry("yes"));
        subTree.set(1, new DataEntry("no"));

        StringRoot stringRoot = new StringRoot("");
        new YamlBackend(stringRoot).write(new Backend.Document() {
            @Override
            public @NonNull CommentData comments() {
                return CommentData.empty();
            }

            @Override
            public @NonNull DataTree data() {
                return dataTree.intoImmut();
            }
        });
        assertEquals("""
                1: hello
                option: false
                -5:
                  hi: yes
                  1: no""", stringRoot.readString().trim());

        DataTree reloaded = new YamlBackend(stringRoot).read(errorSource).getOrThrow().data();
        assertEquals(dataTree, reloaded);
    }

    @Test
    public void butListsAsKeysAreNot(@Mock ErrorContext.Source errorSource, @Mock ErrorContext dummyError) {
        lenient().when(errorSource.getLocale()).thenReturn(Locale.ENGLISH);
        lenient().when(errorSource.buildError(any())).thenReturn(dummyError);
        lenient().when(errorSource.throwError((CharSequence) any())).thenReturn(LoadResult.failure(dummyError));
        lenient().when(errorSource.throwError((Printable) any())).thenReturn(LoadResult.failure(dummyError));
        // Taken from StackOverflow
        String source = """
                mapping:
                  c_id:
                    [pak, gb]: '4711'
                    [pak, ch]: '4712'
                    [pak]: '4713'
                """;
        assertDoesNotThrow(() -> new Load(LoadSettings.builder().build()).loadFromString(source));
        LoadResult<Backend.Document> loadResult = new YamlBackend(new StringRoot(source)).read(errorSource);
        assertFalse(loadResult.isSuccess());
        assertEquals(LoadResult.failure(dummyError), loadResult);
    }
}
