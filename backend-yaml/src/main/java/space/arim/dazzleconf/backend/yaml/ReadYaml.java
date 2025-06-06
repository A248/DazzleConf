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

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.nodes.AnchorNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class ReadYaml {

    private final ErrorContext.Source errorSource;
    private final Yaml yaml;
    private final YamlBackend.CommentMarshall commentMarshall;

    // State

    private final ArrayDeque<String> pathStack = new ArrayDeque<>();

    // The current tree we're building
    private DataTree dataTreeInProgress;
    // The last key we saw - relevant while we're scanning comments/whitespace, and not yet added to the tree
    private Node lastSeenKey;
    // The last entry we saw - relevant for the same reasons, while not yet added to the tree
    private DataEntry lastSeenEntry;

    ReadYaml(ErrorContext.Source errorSource) {
        this.errorSource = errorSource;
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(true);
        loaderOptions.setAllowDuplicateKeys(false);
        yaml = new Yaml(loaderOptions);
        commentMarshall = new YamlBackend.CommentMarshall();
    }

    private int indentLevel() {
        return 2 * pathStack.size();
    }

    private LoadResult<DataTree> mappingNodeToTree(MappingNode node) {
        Object attempt = tryMappingNodeToDataTree(node);
        if (attempt instanceof ErrorContext) {
            return LoadResult.failure((ErrorContext) attempt);
        } else {
            return LoadResult.of((DataTree) attempt);
        }
    }

    // Returns the DataTree built, or ErrorContext on failure
    private Object tryMappingNodeToDataTree(MappingNode node) {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTreeInProgress = dataTree;
        for (NodeTuple nodeTuple : node.getValue()) {

        }
        return dataTree;
    }

    // Returns ErrorContext upon failure!
    private Object tryMappingNodeToDataTree(MappingNode node) {
        DataTree.Mut dataTree = new DataTree.Mut();
        Node nodeOnLastSeenLine = node;
        for (NodeTuple nodeTuple : node.getValue()) {
            Node keyNode = nodeTuple.getKeyNode();
            Node valueNode = nodeTuple.getValueNode();

            Object key = tryNodeToValue(keyNode, true);
            if (key instanceof ErrorContext) {
                return key;
            }
            // Fork the path for this value
            pathStack.addLast(key.toString());
            Object value;
            try {
                value = tryNodeToValue(valueNode, false);
                if (value instanceof ErrorContext) {
                    return value;
                }
            } finally {
                pathStack.pollLast();
            }
            DataEntry entry = new DataEntry(value)
                    .withComments(getComments(keyNode, valueNode))
                    // TODO: Make sure this reports the correct line number
                    .withLineNumber(valueNode.getStartMark().getLine());
            dataTree.set(key, entry);
        }
        return dataTree;
    }

    // Returns ErrorContext upon failure!
    private Object tryNodeToValue(Node node, boolean isKey) {
        if (node instanceof AnchorNode) {
            return tryNodeToValue(((AnchorNode) node).getRealNode(), isKey);
        }
        if (node instanceof ScalarNode) {
            // TODO: Figure out how non-string keys and values are treated and created
            return ((ScalarNode) node).getValue();
        }
        if (!isKey) {
            if (node instanceof MappingNode) {
                return tryMappingNodeToDataTree((MappingNode) node);
            }
            if (node instanceof SequenceNode) {
                List<Node> nodeList = ((SequenceNode) node).getValue();
                List<Object> elements = new ArrayList<>(nodeList.size());
                for (Node elemNode : nodeList) {
                    Object element = tryNodeToValue(elemNode, false);
                    if (element instanceof ErrorContext) {
                        return element;
                    }
                    elements.add(element);
                }
                return elements;
            }
        }
        // TODO: Write a test for the error message
        LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
        ErrorContext error = errorSource.buildError(libraryLang.wrongTypeForValue(
                node, isKey ? "key node" : "value node", node.getClass().getSimpleName()
        ));
        error.addDetail(ErrorContext.ENTRY_PATH, new KeyPath.Immut(pathStack.toArray(new String[0])));
        error.addDetail(ErrorContext.LINE_NUMBER, node.getStartMark().getLine());
        return error;
    }

    // TODO: Write tests for round-tripping comments
    // Please see the snake yaml javadocs to understand what we're doing
    // Inverse of setBlockComments and setInlineAndBelowComments

    private CommentData getComments(Node keyNode, Node valueNode) {

        CommentData commentData = CommentData.empty();
        // Block comments can be converted easily, 1:1
        {
            List<CommentLine> snakeBlockComments = keyNode.getBlockComments();
            if (snakeBlockComments != null) {
                List<String> commentsAbove = new ArrayList<>(snakeBlockComments.size());
                for (CommentLine snakeBlockComment : snakeBlockComments) {
                    commentsAbove.add(extractComment(snakeBlockComment));
                }
                commentData = commentData.setAt(CommentLocation.ABOVE, commentsAbove);
            }
        }
        List<CommentLine> snakeInlineCommentsList = valueNode.getInLineComments();
        if (snakeInlineCommentsList != null) {
            Iterator<CommentLine> snakeInlineComments = snakeInlineCommentsList.iterator();
            if (snakeInlineComments.hasNext()) {
                // Set single inline comment - YAML allows at most one, from our perpsective
                String commentInline = extractComment(snakeInlineComments.next());
                commentData = commentData.setAt(CommentLocation.INLINE, commentInline);

                // Add as many below comments by pulling them out of the list (YAML classifies them as "inline")
                if (snakeInlineComments.hasNext()) {
                    List<String> commentsBelow = new ArrayList<>(snakeInlineCommentsList.size() - 1);
                    do {
                        commentsBelow.add(extractComment(snakeInlineComments.next()));
                    } while (snakeInlineComments.hasNext());
                    commentData = commentData.setAt(CommentLocation.BELOW, commentsBelow);
                }
            }
        }
        return commentData;
    }

    private String extractComment(CommentLine commentLine) {
        String value = commentLine.getValue();
        return value.startsWith(" ") ? value.substring(1) : value;
    }
}
