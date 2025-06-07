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

import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.nodes.CollectionNode;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

final class WriteYaml {

    private final StandardRepresenter standardRepresenter;

    WriteYaml(StandardRepresenter standardRepresenter) {
        this.standardRepresenter = standardRepresenter;
    }

    Node dataTreeToNode(DataTree dataTree) {
        List<NodeTuple> keyValuePairs = new ArrayList<>(dataTree.size());
        dataTree.forEach((key, entry) -> {
            Node keyNode = standardRepresenter.represent(key);
            Node valueNode = entryToNode(entry);

            setAllComments(keyNode, valueNode, entry.getComments());
            keyValuePairs.add(new NodeTuple(keyNode, valueNode));
        });
        return new MappingNode(Tag.MAP, keyValuePairs, FlowStyle.AUTO);
    }

    private Node entryToNode(DataEntry entry) {
        Object value = entry.getValue();
        if (value instanceof DataTree) {
            return dataTreeToNode((DataTree) value);
        }
        return standardRepresenter.represent(value);
    }

    private static void setAllComments(Node keyNode, Node valueNode, CommentData commentData) {
        {
            List<String> commentsAbove = commentData.getAt(CommentLocation.ABOVE);
            if (!commentsAbove.isEmpty()) {
                setAndMapBlockComments(
                        keyNode, commentsAbove, commentsAbove.size(), Node::setBlockComments
                );
            }
        }
        List<String> commentsInline = commentData.getAt(CommentLocation.INLINE);
        List<String> commentsBelow = commentData.getAt(CommentLocation.BELOW);
        if (commentsInline.isEmpty() && commentsBelow.isEmpty()) {
            // Fast path
            return;
        }
        if (commentsInline.size() > 1) {
            throw YamlBackend.doesNotSupport("more than one inline comment");
        }
        // If the entry is a list or a map, we place inline and below comments on the key
        Node whereToPlace = valueNode instanceof CollectionNode ? keyNode : valueNode;
        if (!commentsInline.isEmpty()) {
            whereToPlace.setInLineComments(Collections.singletonList(new CommentLine(
                    Optional.empty(), Optional.empty(), " " + commentsInline.get(0), CommentType.IN_LINE
            )));
        }
        if (!commentsBelow.isEmpty()) {
            setAndMapBlockComments(whereToPlace, commentsBelow, 1 + commentsBelow.size(), Node::setEndComments);
            // Add a blank line: This line helps differentiate us from later comments (e.g. ABOVE on other entries)
            whereToPlace.getEndComments().add(new CommentLine(
                    Optional.empty(), Optional.empty(), "", CommentType.BLANK_LINE
            ));
        }
    }

    private static void setAndMapBlockComments(Node node, List<String> comments, int sizeHint,
                                               BiConsumer<Node, List<CommentLine>> setter) {
        List<CommentLine> snakeBlockComments = new ArrayList<>(sizeHint);
        for (String comment : comments) {
            snakeBlockComments.add(new CommentLine(
                    Optional.empty(), Optional.empty(), " " + comment, CommentType.BLOCK
            ));
        }
        setter.accept(node, snakeBlockComments);
    }

}
