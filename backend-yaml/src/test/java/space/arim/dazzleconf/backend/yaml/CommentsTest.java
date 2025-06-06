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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class CommentsTest {

    private final StringRoot stringRoot = new StringRoot("");
    private final YamlBackend yamlBackend = new YamlBackend(stringRoot);

    @Test
    @Disabled
    public void writeSimpleHeader() {
        CommentData header = CommentData.empty().setAt(CommentLocation.ABOVE, "Hello");
        yamlBackend.write(new Backend.Document() {
            @Override
            public @NonNull CommentData comments() {
                return header;
            }

            @Override
            public @NonNull DataTree data() {
                DataTree.Mut dataTree = new DataTree.Mut();
                dataTree.set("option", new DataEntry("value"));
                return dataTree;
            }
        });
        assertEquals("# Hello\noption: value", stringRoot.readString().trim());
    }

    @Test
    @Disabled
    public void writeAdvancedHeader() {
        CommentData header = CommentData.empty()
                .setAt(CommentLocation.ABOVE, "Hello", "There", "World")
                .setAt(CommentLocation.INLINE, "INLINE ME WHERE?")
                .setAt(CommentLocation.BELOW, "Below");
        yamlBackend.write(new Backend.Document() {
            @Override
            public @NonNull CommentData comments() {
                return header;
            }

            @Override
            public @NonNull DataTree data() {
                DataTree.Mut dataTree = new DataTree.Mut();
                dataTree.set("option", new DataEntry("value"));
                return dataTree;
            }
        });
        assertEquals("# Hello\n# There\n# World\noption: value\n# Below", stringRoot.readString().trim());
    }

    @Test
    @Disabled
    public void roundTripHeader(@Mock ErrorContext.Source errorSource) {
        CommentData header = CommentData.empty()
                .setAt(CommentLocation.ABOVE, "Hello", "There", "World")
                .setAt(CommentLocation.BELOW, "Below");
        yamlBackend.write(new Backend.Document() {
            @Override
            public @NonNull CommentData comments() {
                return header;
            }

            @Override
            public @NonNull DataTree data() {
                DataTree.Mut dataTree = new DataTree.Mut();
                dataTree.set("option", new DataEntry("value"));
                return dataTree;
            }
        });
        assertEquals("# Hello\n# There\n# World\noption: value\n# Below", stringRoot.readString().trim());
        assertEquals(
                LoadResult.of(header),
                yamlBackend.read(errorSource).map(document -> document != null ? document.comments() : null),
                "Actual document: " + stringRoot.readString().trim()
        );
    }

    @Test
    public void writeEntryComments() {
        CommentData commentsOnEntry = CommentData.empty()
                .setAt(CommentLocation.ABOVE, "Watching you")
                .setAt(CommentLocation.INLINE, "From")
                .setAt(CommentLocation.BELOW, "Below!", "Haha");
        yamlBackend.write(new Backend.Document() {
            @Override
            public @NonNull CommentData comments() {
                return CommentData.empty();
            }

            @Override
            public @NonNull DataTree data() {
                DataTree.Mut dataTree = new DataTree.Mut();
                DataTree.Mut subTree = new DataTree.Mut();
                dataTree.set("sub", new DataEntry(subTree).withComments(CommentLocation.ABOVE, List.of("Test header")));
                dataTree.set("another", new DataEntry(false));
                subTree.set("plain", new DataEntry(1));
                subTree.set("commented", new DataEntry("hello").withComments(commentsOnEntry));
                return dataTree;
            }
        });
        assertEquals("""
                sub:
                  plain: 1
                  # Watching you
                  commented: hello # From
                  # Below!
                  # Haha
                another: false""", stringRoot.readString().trim());
    }

    @Test
    public void readEntryComments(@Mock ErrorContext.Source errorSource) {
        stringRoot.writeString("""
                sub:
                  plain: 1
                  # Watching you
                  commented: hello # From
                  # Below!
                  # Haha
                another: false""");
        CommentData commentsOnEntry;
        {
            Backend.Document document = yamlBackend.read(errorSource).getOrThrow();
            assertNotNull(document);
            DataTree dataTree = document.data();
            DataEntry subEntry = dataTree.get("sub");
            assertNotNull(subEntry);
            DataTree subTree = (DataTree) subEntry.getValue();
            DataEntry commented = subTree.get("commented");
            assertNotNull(commented);
            commentsOnEntry = commented.getComments();
        }
        assertEquals(
                CommentData.empty()
                        .setAt(CommentLocation.ABOVE, "Watching you")
                        .setAt(CommentLocation.INLINE, "From")
                        .setAt(CommentLocation.BELOW, "Below!", "Haha"),
                commentsOnEntry
        );
    }

    @Test
    public void writeDistinguishSubSectionFromEntryComments() {
        CommentData commentsOnEntry = CommentData.empty()
                .setAt(CommentLocation.ABOVE, "Watching you")
                .setAt(CommentLocation.INLINE, "From")
                .setAt(CommentLocation.BELOW, "Below!", "Haha");
        CommentData commentsOnSection = CommentData.empty()
                .setAt(CommentLocation.ABOVE, "On top", "Another on top")
                //.setAt(CommentLocation.INLINE, "Beside: where will it go?")
                .setAt(CommentLocation.BELOW, "Below!");
        yamlBackend.write(new Backend.Document() {
            @Override
            public @NonNull CommentData comments() {
                return CommentData.empty();
            }

            @Override
            public @NonNull DataTree data() {
                DataTree.Mut dataTree = new DataTree.Mut();
                DataTree.Mut subTree = new DataTree.Mut();
                DataTree.Mut subSubTree = new DataTree.Mut();
                dataTree.set("sub", new DataEntry(subTree));
                subTree.set("plain", new DataEntry(1));
                subTree.set("commented", new DataEntry("hello").withComments(commentsOnEntry));
                subTree.set("section", new DataEntry(subSubTree).withComments(commentsOnSection));
                subSubTree.set("dummy", new DataEntry("will we snag the comments?"));
                subSubTree.set("mischievous", new DataEntry(true));
                return dataTree;
            }
        });
        assertEquals("""
                sub:
                  plain: 1
                  # Watching you
                  commented: hello # From
                  # Below!
                  # Haha
                  # On top
                  # Another on top
                  section:
                    dummy: will we snag the comments?
                    mischievous: true
                
                  # Below!""", stringRoot.readString().trim());
    }

    @Test
    public void readDistinguishSubSectionFromEntryComments(@Mock ErrorContext.Source errorSource) {
        stringRoot.writeString("""
                sub:
                  plain: 1
                  # Watching you
                  commented: hello # From
                  # Below!
                  # Haha""");
        CommentData commentsOnEntry;
        CommentData commentsOnSection;
        {
            Backend.Document document = yamlBackend.read(errorSource).getOrThrow();
            assertNotNull(document);
            DataTree dataTree = document.data();
            DataEntry subEntry = dataTree.get("sub");
            assertNotNull(subEntry);
            DataTree subTree = (DataTree) subEntry.getValue();
            DataEntry commented = subTree.get("commented");
            assertNotNull(commented);
            commentsOnEntry = commented.getComments();
            DataEntry subSubEntry = subTree.get("section");
            assertNotNull(subSubEntry);
            commentsOnSection = subSubEntry.getComments();
        }
        assertEquals(
                CommentData.empty()
                        .setAt(CommentLocation.ABOVE, "Watching you")
                        .setAt(CommentLocation.INLINE, "From")
                        .setAt(CommentLocation.BELOW, "Below!", "Haha"),
                commentsOnEntry
        );
    }
}
