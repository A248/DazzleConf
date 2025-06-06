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

package space.arim.dazzleconf.backend.hocon;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.engine.Comments;
import space.arim.dazzleconf2.engine.liaison.SubSection;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class WriteHoconTest {

    public interface Config {

        @Comments({"Comment using Hocon", "Another line of comments"})
        default String someOption() {
            return "option-value";
        }

        @SubSection ConfigSection sectionOne();

        @Comments("Section header")
        interface ConfigSection {

            default boolean someFlag() {
                return false;
            }

        }

    }

    @Test
    public void writeDefaults() {
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
        DataTree.Mut dataTree = new DataTree.Mut();
        configuration.writeTo(configuration.loadDefaults(), dataTree);
        HoconBackend backend = new HoconBackend(new StringRoot(""));
        assertDoesNotThrow(() -> backend.write(Backend.Document.simple(dataTree)));
    }

    @Test
    public void writeComments() {
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class).build();
        DataTree.Mut dataTree = new DataTree.Mut();
        configuration.writeTo(configuration.loadDefaults(), dataTree);
        StringRoot stringRoot = new StringRoot("");
        HoconBackend backend = new HoconBackend(stringRoot);
        assertDoesNotThrow(() -> backend.write(Backend.Document.simple(dataTree)));

        String content = stringRoot.readString();
        List<String> contentLines = content.lines().toList();
        /*
        This test is made for comments and not order. So let's accept that either entry in Config may come first
         */
        List<String> entry1 = List.of(
                "# Comment using Hocon",
                "# Another line of comments",
                "some-option=option-value");
        List<String> entry2 = List.of(
                "# Section header",
                "section-one {",
                "    some-flag=false",
                "}");
        if (!contentLines.equals(combineLines(entry1, entry2))
                && contentLines.equals(combineLines(entry2, entry1))) {
            fail("Received unexpected content:\n" + content);
        }
    }

    private static List<String> combineLines(List<String> str1, List<String> str2) {
        List<String> combined = new ArrayList<>(str1.size() + str2.size());
        combined.addAll(str1);
        combined.addAll(str2);
        combined.add(""); // empty line at the end
        return List.copyOf(combined);
    }

    @Test
    public void writeOrdered() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("zeroth-option", new DataEntry(false));
        dataTree.set("first-option", new DataEntry(true));
        dataTree.set("second-option", new DataEntry(false));
        StringRoot stringRoot = new StringRoot("");
        HoconBackend backend = new HoconBackend(stringRoot);
        assertDoesNotThrow(() -> backend.write(Backend.Document.simple(dataTree)));

        assertEquals("""
                zeroth-option=false
                first-option=true
                second-option=false""", stringRoot.readString().trim());
    }

    @Test
    public void writeOrderedComments() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("zeroth-option", new DataEntry(false).withComments(CommentLocation.ABOVE, List.of("Comments on zeroth")));
        dataTree.set("first-option", new DataEntry(true).withComments(CommentLocation.ABOVE, List.of("Comments on first")));
        dataTree.set("second-option", new DataEntry(false).withComments(CommentLocation.ABOVE, List.of("Comments on second")));
        StringRoot stringRoot = new StringRoot("");
        HoconBackend backend = new HoconBackend(stringRoot);
        assertDoesNotThrow(() -> backend.write(Backend.Document.simple(dataTree)));

        assertEquals("""
                # Comments on zeroth
                zeroth-option=false
                # Comments on first
                first-option=true
                # Comments on second
                second-option=false""", stringRoot.readString().trim());
    }
}
