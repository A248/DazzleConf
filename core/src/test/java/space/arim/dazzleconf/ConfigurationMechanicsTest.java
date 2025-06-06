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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.ConfigurationDefinition;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.*;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.engine.Comments;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.UpdateReason;
import space.arim.dazzleconf2.engine.liaison.SubSection;

import java.lang.reflect.AccessFlag;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Covers:
// 1. Key mapping and labels
// 2. Error counts
// 3. Interfaces without defaults, and missing value errors
// 4. Key paths in error messages, accounting for sections
// 5. Enum parsing, big and small, and error messages
// 6. Top-level comments, accounting for inheritance
// 7. Entry-level comments, including edits and round-trip
@ExtendWith(MockitoExtension.class)
public class ConfigurationMechanicsTest {

    private Configuration<Config> configuration;
    private final LoadListener loadListener;
    private final KeyMapper keyMapper = new SnakeCaseKeyMapper();

    public ConfigurationMechanicsTest(@Mock LoadListener loadListener) {
        this.loadListener = loadListener;
    }

    @Comments("Inherited comment should not be passed down")
    public interface Parent {

        String inherited();
    }

    @Comments("Comment header expected")
    public interface Config extends Parent {

        @Comments("Game control key")
        char keyPress();

        List<List<List<String>>> matrix();

        Set<Float> floats();

        StandardCopyOption smallEnum();

        @SubSection Section subSection();

        @Comments(value = "Comments placed on subsection declaration", location = CommentLocation.INLINE)
        interface Section {

            boolean enabled();

            @Comments("What to send players")
            String message();

            AccessFlag bigEnum();

        }
    }

    @BeforeEach
    public void setup() {
        configuration = Configuration.defaultBuilder(Config.class).build();
    }

    @Test
    public void getTopLevelComments() {
        assertEquals(
                CommentData.empty().setAt(CommentLocation.ABOVE, "Comment header expected"),
                configuration.getLayout().getComments()
        );
    }

    @Test
    public void getLabels() {
        assertEquals(
                Set.of("inherited", "keyPress", "matrix", "floats", "smallEnum", "subSection"),
                new HashSet<>(configuration.getLayout().getLabels())
        );
    }

    @Test
    public void getLabelsAsStream() {
        assertEquals(
                Set.of("inherited", "keyPress", "matrix", "floats", "smallEnum", "subSection"),
                configuration.getLayout().getLabelsAsStream().collect(Collectors.toSet())
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void readKeyMapped(boolean updatableValues) {
        String inherited = "inheritance";
        char keyPress = 'k';
        List<List<List<String>>> matrix = List.of(
                List.of(List.of("beside")), List.of(List.of("hello", "there"), List.of("crazy"))
        );
        Set<Float> floats = Set.of(1.4f);
        StandardCopyOption smallEnum = StandardCopyOption.COPY_ATTRIBUTES;
        boolean enabled = true;
        String message = "msg";
        AccessFlag bigEnum = AccessFlag.MANDATED;

        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("inherited", new DataEntry(inherited));
        dataTree.set("key-press", new DataEntry(updatableValues ? String.valueOf(keyPress) : keyPress));
        dataTree.set("matrix", new DataEntry(matrix));
        dataTree.set("floats", new DataEntry(new ArrayList<>(floats)));
        dataTree.set("small-enum", new DataEntry(smallEnum.name()));
        DataTree.Mut subTree = new DataTree.Mut();
        dataTree.set("sub-section", new DataEntry(subTree));
        subTree.set("enabled", new DataEntry(updatableValues ? String.valueOf(enabled) : enabled));
        subTree.set("message", new DataEntry(message));
        subTree.set("big-enum", new DataEntry(bigEnum.name()));

        LoadResult<Config> loadResult = configuration.readFrom(dataTree, new ConfigurationDefinition.ReadOptions() {
            @Override
            public @NonNull LoadListener loadListener() {
                return loadListener;
            }

            @Override
            public @NonNull KeyMapper keyMapper() {
                return keyMapper;
            }
        });
        assertTrue(loadResult.isSuccess());
        if (updatableValues) {
            verify(loadListener).updatedPath(new KeyPath.Immut("sub-section", "enabled"), UpdateReason.UPDATED);
            verifyNoMoreInteractions(loadListener);
        } else {
            verifyNoInteractions(loadListener);
        }
        Config loaded = loadResult.getOrThrow();

        assertEquals(inherited, loaded.inherited());
        assertEquals(keyPress, loaded.keyPress());
        assertEquals(matrix, loaded.matrix());
        assertEquals(floats, loaded.floats());
        assertEquals(smallEnum, loaded.smallEnum());
        Config.Section section = loaded.subSection();
        assertEquals(enabled, section.enabled());
        assertEquals(message, section.message());
        assertEquals(bigEnum, section.bigEnum());
    }

    @Test
    public void writeKeyMapped() {
        String inherited = "inheritance";
        char keyPress = 'k';
        List<List<List<String>>> matrix = List.of(
                List.of(List.of("beside")), List.of(List.of("hello", "there"), List.of("crazy"))
        );
        Set<Float> floats = Set.of(1.4f);
        StandardCopyOption smallEnum = StandardCopyOption.COPY_ATTRIBUTES;
        boolean enabled = true;
        String message = "msg";
        AccessFlag bigEnum = AccessFlag.MANDATED;

        Config config = new Config() {
            @Override
            public String inherited() {
                return inherited;
            }

            @Override
            public char keyPress() {
                return keyPress;
            }

            @Override
            public List<List<List<String>>> matrix() {
                return matrix;
            }

            @Override
            public Set<Float> floats() {
                return floats;
            }

            @Override
            public StandardCopyOption smallEnum() {
                return smallEnum;
            }

            @Override
            public @SubSection Section subSection() {
                return new Section() {
                    @Override
                    public boolean enabled() {
                        return enabled;
                    }

                    @Override
                    public String message() {
                        return message;
                    }

                    @Override
                    public AccessFlag bigEnum() {
                        return bigEnum;
                    }
                };
            }

        };
        DataTree.Mut dataTree = new DataTree.Mut();
        configuration.writeTo(config, dataTree, () -> keyMapper);

        assertEquals(new DataEntry(inherited), dataTree.get("inherited"));
        {
            DataEntry keyPressEntry = dataTree.get("key-press");
            assertNotNull(keyPressEntry);
            assertEquals(keyPress, keyPressEntry.getValue());
            assertEquals(CommentData.empty().setAt(CommentLocation.ABOVE, "Game control key"), keyPressEntry.getComments());
        }
        assertEquals(new DataEntry(matrix), dataTree.get("matrix"));
        assertNotNull(dataTree.get("floats"));
        assertEquals(new DataEntry(smallEnum.name()), dataTree.get("small-enum"));

        DataEntry subEntry = dataTree.get("sub-section");
        assertNotNull(subEntry);
        assertEquals(
                CommentData.empty().setAt(CommentLocation.INLINE, "Comments placed on subsection declaration"),
                subEntry.getComments()
        );
        DataTree.Mut subTree = assertInstanceOf(DataTree.class, subEntry.getValue()).intoMut();
        assertEquals(new DataEntry(enabled), subTree.get("enabled"));
        {
            DataEntry messageEntry = subTree.get("message");
            assertNotNull(messageEntry);
            assertEquals(message, messageEntry.getValue());
            assertEquals(CommentData.empty().setAt(CommentLocation.ABOVE, "What to send players"), messageEntry.getComments());
        }
        assertEquals(new DataEntry(message), subTree.get("message"));
        assertEquals(new DataEntry(bigEnum.name()), subTree.get("big-enum"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void readWithUpdateKeyMapped(boolean updatableValues) {
        String inherited = "inheritance";
        char keyPress = 'k';
        List<List<List<String>>> matrix = List.of(
                List.of(List.of("beside")), List.of(List.of("hello", "there"), List.of("crazy"))
        );
        Set<Float> floats = Set.of(1.4f);
        StandardCopyOption smallEnum = StandardCopyOption.COPY_ATTRIBUTES;
        boolean enabled = true;
        String message = "msg";
        AccessFlag bigEnum = AccessFlag.MANDATED;

        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("inherited", new DataEntry(inherited));
        dataTree.set("key-press", new DataEntry(updatableValues ? String.valueOf(keyPress) : keyPress)
                .withComments(CommentLocation.ABOVE, List.of("Game control key (edited)")));
        dataTree.set("matrix", new DataEntry(matrix));
        dataTree.set("floats", new DataEntry(new ArrayList<>(floats)));
        dataTree.set("small-enum", new DataEntry(smallEnum.name()));
        DataTree.Mut subTree = new DataTree.Mut();
        dataTree.set("sub-section", new DataEntry(subTree)
                .withComments(CommentLocation.INLINE, List.of("Comments placed on subsection declaration")));
        subTree.set("enabled", new DataEntry(updatableValues ? String.valueOf(enabled) : enabled));
        subTree.set("message", new DataEntry(message));
        subTree.set("big-enum", new DataEntry(bigEnum.name()));

        LoadResult<Config> loadResult = configuration.readWithUpdate(dataTree, new ConfigurationDefinition.ReadOptions() {
            @Override
            public @NonNull LoadListener loadListener() {
                return loadListener;
            }

            @Override
            public @NonNull KeyMapper keyMapper() {
                return keyMapper;
            }
        });
        assertTrue(loadResult.isSuccess());
        if (updatableValues) {
            verify(loadListener).updatedPath(new KeyPath.Mut("sub-section", "enabled"), UpdateReason.UPDATED);
            verifyNoMoreInteractions(loadListener);
        } else {
            verifyNoInteractions(loadListener);
        }

        assertEquals(new DataEntry(inherited), dataTree.get("inherited"));
        assertEquals(new DataEntry(keyPress), dataTree.get("key-press"));
        {
            DataEntry keyPressEntry = dataTree.get("key-press");
            assertNotNull(keyPressEntry);
            assertEquals(new DataEntry(keyPress), keyPressEntry);
            assertEquals(CommentData.empty().setAt(CommentLocation.ABOVE, "Game control key (edited)"), keyPressEntry.getComments());
        }
        assertEquals(new DataEntry(matrix), dataTree.get("matrix"));
        assertNotNull(dataTree.get("floats"));
        assertEquals(new DataEntry(smallEnum.name()), dataTree.get("small-enum"));

        DataEntry subEntry = dataTree.get("sub-section");
        assertNotNull(subEntry);
        assertEquals(CommentData.empty().setAt(CommentLocation.INLINE, "Comments placed on subsection declaration"), subEntry.getComments());
        subTree = assertInstanceOf(DataTree.class, subEntry.getValue()).intoMut();
        assertEquals(new DataEntry(enabled), subTree.get("enabled"));
        {
            DataEntry messageEntry = subTree.get("message");
            assertNotNull(messageEntry);
            assertEquals(message, messageEntry.getValue());
            // Comments are not re-added if they are removed
            assertEquals(CommentData.empty(), messageEntry.getComments());
        }
        assertEquals(new DataEntry(bigEnum.name()), subTree.get("big-enum"));
    }

}
