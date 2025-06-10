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
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class EmptyDataTest {

    private final StringRoot stringRoot = new StringRoot("");

    @Test
    public void readEmpty(@Mock ErrorContext.Source errorSource) {
        AtomicInteger index = new AtomicInteger();
        for (String emptyDocument : List.of("", "    ", " \n ")) {
            stringRoot.writeString(emptyDocument);
            LoadResult<Backend.Document> loadResult = new YamlBackend(stringRoot).read(errorSource);
            int idx = index.getAndIncrement();
            assertEquals(LoadResult.of(null), loadResult, () -> "For source " + idx + ", got " + loadResult.getOrThrow().data());
            verifyNoInteractions(errorSource);
        }
    }

    @Test
    public void writeEmpty() {
        Backend backend = new YamlBackend(stringRoot);
        Backend.Document emptyDocument = new Backend.Document() {
            @Override
            public @NonNull CommentData comments() {
                return CommentData.empty();
            }

            @Override
            public @NonNull DataTree data() {
                return new DataTree.Immut();
            }
        };
        assertDoesNotThrow(() -> backend.write(emptyDocument));
    }
}
