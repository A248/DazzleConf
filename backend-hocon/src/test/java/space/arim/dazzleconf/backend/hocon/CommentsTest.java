/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf.TestingErrorSource;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommentsTest {

    private final StringRoot stringRoot = new StringRoot("");
    private final ErrorContext.Source errorSource = new TestingErrorSource().makeErrorSource();

    private static CommentData commentAbove(String...comments) {
        return CommentData.empty().setAt(CommentLocation.ABOVE, List.of(comments));
    }

    @Test
    public void readComments() {
        HoconBackend backend = new HoconBackend.Builder()
                .commentMode(HoconCommentMode.ROUND_TRIP_OMIT_HEADER)
                .build(stringRoot);
        stringRoot.writeString("""
                # Comments on zeroth
                zeroth-option = false
                # Comments on first
                first-option = true
                # Comments on second
                second-option = false""");
        Backend.Document document = backend.read(errorSource).getOrThrow();
        assertNotNull(document);
        DataTree dataTree = document.data();
        DataEntry zeroth = dataTree.get("zeroth-option");
        DataEntry first = dataTree.get("first-option");
        DataEntry second = dataTree.get("second-option");
        assertNotNull(zeroth);
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(commentAbove("Comments on zeroth"), zeroth.getComments());
        assertEquals(commentAbove("Comments on first"), first.getComments());
        assertEquals(commentAbove("Comments on second"), second.getComments());
    }
}
