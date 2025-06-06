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

import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.Node;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

final class CommentMarshall {

    private final int startLine;

    CommentMarshall(int startLine) {
        this.startLine = startLine;
    }

    // Please see the snake yaml javadocs to understand what we're doing

    private List<String> getComments1to1(List<CommentLine> commentLines) {

    }

    // Block comments, for example, can be converted easily,1:1

    private void setComments1to1(Node node, List<String> comments, BiConsumer<Node, List<CommentLine>> setter) {
        List<CommentLine> snakeBlockComments = new ArrayList<>(comments.size());
        for (String comment : comments) {
            snakeBlockComments.add(new CommentLine(null, null, " " + comment, CommentType.BLOCK));
        }
        setter.accept(node, snakeBlockComments);
    }

    private void setInlineAndBelowCommentsMerged(Node node, CommentData commentData) {
        // SnakeYAML (following YAML spec) considers comments below the current entry as "inline"
        // So, we need to combine our inline/below comments to build YAML's inline comments list

        List<String> commentsInline = commentData.getAt(CommentLocation.INLINE);
        List<String> commentsBelow = commentData.getAt(CommentLocation.BELOW);

        List<CommentLine> snakeInlineComments = new ArrayList<>(1 + commentsBelow.size());
        switch (commentsInline.size()) {
            case 0:
                if (commentsBelow.isEmpty()) {
                    return; // All finished!
                }
                // Fill in an empty inline comment, so we can add comments below
                snakeInlineComments.add(new CommentLine(null, null, " ", CommentType.BLANK_LINE));
                break;
            case 1:
                String inline = commentsInline.get(0);
                snakeInlineComments.add(new CommentLine(null, null, " " + inline, CommentType.IN_LINE));
                break;
            default:
                throw YamlBackend.doesNotSupport("more than one inline comment");
        }
        for (String below : commentsBelow) {
            snakeInlineComments.add(new CommentLine(null, null, " " + below, CommentType.BLOCK));
        }
        node.setInLineComments(snakeInlineComments);
    }
}
