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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.AnchorNode;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static space.arim.dazzleconf2.backend.Printable.preBuilt;

final class ReadYaml {

    private final ErrorContext.Source errorSource;
    private final StandardConstructor standardConstructor;

    ReadYaml(ErrorContext.Source errorSource, StandardConstructor standardConstructor) {
        this.errorSource = errorSource;
        this.standardConstructor = standardConstructor;
    }

    // This class implements a scanner for YAML, that correctly pairs comments with the entry which declares them
    // For example
    //
    // mysection:
    //  # Would be detected as belonging to 'mysection'
    //    # Would be detected as belonging to 'entry'
    //   entry: true # also belongs to 'entry'
    //   # belongs to entry since it's closer than the alternative
    // version: 1
    //  # belongs to 'version'
    // # belongs to the footer of the entire document

    // Document-level state

    private Product product;

    private static class Product implements Backend.Document {
        private final DataTree.Mut dataTree = new DataTree.Mut();
        private final List<String> header = new ArrayList<>();
        private final List<String> footer = new ArrayList<>();

        @Override
        public @NonNull CommentData comments() {
            return CommentData.empty()
                    .setAt(CommentLocation.ABOVE, header)
                    .setAt(CommentLocation.BELOW, footer);
        }

        @Override
        public @NonNull DataTree data() {
            return dataTree;
        }
    }

    // Entry-level and visiting state

    private final ArrayDeque<Object> keyPathStack = new ArrayDeque<>();

    private Entry lastEntry;
    private final List<CommentLine> commentBuffer = new ArrayList<>();

    // Error handling

    private ErrorContext thrownError;
    private static final IllegalStateException THROW_SIGNAL_ERROR = new IllegalStateException();

    private IllegalStateException throwError(ErrorContext error) {
        String[] keyPathString = new String[keyPathStack.size()];
        int n = 0;
        for (Object keyPathElement : keyPathStack) {
            keyPathString[n++] = keyPathElement.toString();
        }
        error.addDetail(ErrorContext.ENTRY_PATH, new KeyPath.Immut(keyPathString));
        this.thrownError = error;
        throw THROW_SIGNAL_ERROR;
    }

    // Kick-starts the whole operation
    LoadResult<Backend.@NonNull Document> runForMapping(MappingNode mappingNode) {
        Product product = new Product();
        this.product = product;
        try {
            visitMap(mappingNode, product.dataTree);
            if (this.lastEntry != null) {
                this.lastEntry.competeWithHeaderOrFooterForBuffer(product.footer, CommentLocation.BELOW);
                this.lastEntry.finish();
            } else {
                // Empty - add everything to header
                for (CommentLine commentLine : commentBuffer) {
                    String commentValue = extractComment(commentLine, CommentType.BLOCK);
                    if (commentValue != null) {
                        product.header.add(commentValue);
                    }
                }
            }
            return LoadResult.of(product);
        } catch (IllegalStateException checkForError) {
            if (checkForError == THROW_SIGNAL_ERROR) {
                return LoadResult.failure(thrownError);
            }
            throw checkForError; // Shouldn't happen
        }
    }

    private final class Entry {

        private final DataTree.Mut container;
        private final int indentLevel;
        private final Object key;
        private final Object value;
        private CommentData commentData = CommentData.empty();

        private Entry(DataTree.Mut container, int indentLevel, Object key, Object value) {
            this.container = container;
            this.indentLevel = indentLevel;
            this.key = key;
            this.value = value;
        }

        void competeWithHeaderOrFooterForBuffer(List<String> headerOrFooter, CommentLocation locationIfOurs) {
            List<String> commentsForUs = new ArrayList<>();
            for (CommentLine commentLine : commentBuffer) {
                String commentValue = extractComment(commentLine, CommentType.BLOCK);
                if (commentValue != null) {
                    if (getCommentStart(commentLine) == 0) {
                        headerOrFooter.add(commentValue);
                    } else {
                        commentsForUs.add(commentValue);
                    }
                }
            }
            commentData = commentData.setAt(locationIfOurs, commentsForUs);
        }

        void competeWithNewEntryForBuffer(Entry newEntry) {
            List<String> belowCommentsForUs = new ArrayList<>();
            List<String> aboveCommentsForThem = new ArrayList<>();

            for (CommentLine commentLine : commentBuffer) {
                String commentValue = extractComment(commentLine, CommentType.BLOCK);
                if (commentValue != null) {
                    if (isFirstCloser(indentLevel, newEntry.indentLevel, getCommentStart(commentLine))) {
                        belowCommentsForUs.add(commentValue);
                    } else {
                        aboveCommentsForThem.add(commentValue);
                    }
                }
            }
            commentData = commentData.setAt(CommentLocation.BELOW, belowCommentsForUs);
            newEntry.commentData = newEntry.commentData.setAt(CommentLocation.ABOVE, aboveCommentsForThem);
        }

        void finish() {
            container.set(key, new DataEntry(value).withComments(commentData));
        }
    }

    private void visitMap(MappingNode mappingNode, DataTree.Mut container) {
        visitBlockComments(mappingNode.getBlockComments());
        for (NodeTuple nodeTuple : mappingNode.getValue()) {
            Node keyNode = nodeTuple.getKeyNode();
            Node valueNode = nodeTuple.getValueNode();

            visitEntry(keyNode, valueNode, container);
        }
        visitBlockComments(mappingNode.getEndComments());
    }

    private void visitEntry(Node keyNode, Node valueNode, DataTree.Mut container) {
        // FIRST
        // Gather the value as a mutable container. This is kind of like look-ahead, but object-based
        Object value;
        // We'll add inline comments from either keyNode and valueNode, depending on what the value is
        List<String> inlineComments = new ArrayList<>();
        Consumer<Node> addInlineCommentsFrom = (chosenNode) -> {
            List<CommentLine> snakeInlineComments = chosenNode.getInLineComments();
            if (snakeInlineComments != null) {
                for (CommentLine commentLine : snakeInlineComments) {
                    String commentValue = extractComment(commentLine, CommentType.IN_LINE);
                    if (commentValue != null) {
                        inlineComments.add(commentValue);
                    }
                }
            }
        };
        // Prepare also for next round, but don't do it yet. Postpone continuations of the algorithm until later
        List<Consumer<ReadYaml>> continuations = new ArrayList<>();
        value = visitEntryContent(valueNode, keyNode, addInlineCommentsFrom, continuations);
        // Make new entry
        Entry thisEntry;
        {
            Mark keyStartMark = keyNode.getStartMark().orElse(null);
            if (keyStartMark == null) {
                throw throwError(errorSource.buildError(preBuilt("Start mark not present on key " + keyNode)));
            }
            thisEntry = new Entry(container, keyStartMark.getColumn(), getKeyValue(keyNode), value);
            thisEntry.commentData = thisEntry.commentData.setAt(CommentLocation.INLINE, inlineComments);
        }
        // Check last item and compare
        Entry lastEntry = this.lastEntry;
        if (lastEntry == null) {
            // No prior items: that means we're the first item
            // So, either add comments to first item, or add to header
            thisEntry.competeWithHeaderOrFooterForBuffer(product.header, CommentLocation.ABOVE);
        } else {
            // We just finished traversing in between items, and arrived at another one
            // So, look at all comments, and have the last and current item compete over them
            lastEntry.competeWithNewEntryForBuffer(thisEntry);
            lastEntry.finish();
        }
        // NEXT ROUND
        // Clear the buffer, so we can start looking for more
        commentBuffer.clear();
        // Prepare to see the next item
        this.lastEntry = thisEntry;
        // GO
        // Either return here, or recurse to find more values
        if (!continuations.isEmpty()) {
            // Looks like we're recursing first
            keyPathStack.addLast(thisEntry.key);
            try {
                for (Consumer<ReadYaml> continuation : continuations) {
                    continuation.accept(this);
                }
            } finally {
                keyPathStack.pollLast();
            }
        }

    }

    // If there is a tie, gives priority to the higher-ranked competitor
    // If there is a tie and the competitors are ranked the same, gives priority to the later argument
    private static boolean isFirstCloser(int competitor1, int competitor2, int competeOver) {
        int distanceTo1 = Math.abs(competitor1 - competeOver);
        int distanceTo2 = Math.abs(competitor2 - competeOver);
        if (distanceTo1 < distanceTo2) {
            return true;
        }
        if (distanceTo2 < distanceTo1) {
            return false;
        }
        // Tie! Need to break the tie
        return competitor1 > competitor2;
    }

    private void visitBlockComments(List<CommentLine> snakeComments) {
        if (snakeComments != null) {
            this.commentBuffer.addAll(snakeComments);
        }
    }

    private int getCommentStart(CommentLine commentLine) {
        Mark commentStartMark = commentLine.getStartMark().orElse(null);
        if (commentStartMark == null) {
            throw throwError(errorSource.buildError(preBuilt("Start mark not present on comment " + commentLine)));
        }
        return commentStartMark.getColumn() - 2;
    }

    // Produces ErrorContext if a map or sequence was used as a key
    private Object getKeyValue(Node keyNode) {
        if (keyNode instanceof AnchorNode) {
            return getKeyValue(((AnchorNode) keyNode).getRealNode());
        }
        if (keyNode instanceof ScalarNode) {
            Object scalarKey = standardConstructor.constructSingleDocument(Optional.of(keyNode));
            return scalarKey == null ? "null" : scalarKey;
        }
        // TODO: Write a test for the error message
        LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
        ErrorContext error = errorSource.buildError(libraryLang.wrongTypeForValue(
                keyNode, "key type", keyNode.getClass().getSimpleName()
        ));
        keyNode.getStartMark().ifPresent(mark -> {
            error.addDetail(ErrorContext.LINE_NUMBER, mark.getLine());
        });
        throw throwError(error);
    }

    private @NonNull Object visitEntryContent(Node valueNode, Node keyNodeIfNeeded,
                                     Consumer<Node> addInlineCommentsFrom,
                                     List<Consumer<ReadYaml>> continueVisiting) {
        if (valueNode instanceof AnchorNode) {
            return visitEntryContent(((AnchorNode) valueNode).getRealNode(), keyNodeIfNeeded, addInlineCommentsFrom, continueVisiting);
        }
        if (valueNode instanceof ScalarNode) {
            addInlineCommentsFrom.accept(valueNode);
            Object scalarValue = standardConstructor.constructSingleDocument(Optional.of(valueNode));
            return scalarValue == null ? "null" : scalarValue;
        }
        if (valueNode instanceof MappingNode) {
            addInlineCommentsFrom.accept(keyNodeIfNeeded);
            DataTree.Mut dataTree = new DataTree.Mut();
            continueVisiting.add(read -> read.visitMap((MappingNode) valueNode, dataTree));
            return dataTree;
        }
        if (valueNode instanceof SequenceNode) {
            addInlineCommentsFrom.accept(keyNodeIfNeeded);

            List<Node> nodeList = ((SequenceNode) valueNode).getValue();
            List<Object> elements = new ArrayList<>(nodeList.size());

            continueVisiting.add(read -> read.visitBlockComments(valueNode.getBlockComments()));
            for (Node elemNode : nodeList) {
                elements.add(visitEntryContent(elemNode, null, (ignore) -> {}, continueVisiting));
            }
            continueVisiting.add(read -> read.visitBlockComments(valueNode.getEndComments()));
            return elements;
        }
        throw new IllegalStateException("Unknown Node subclass: " + valueNode);
    }

    private @Nullable String extractComment(CommentLine engineComment, CommentType expectIfNotBlank) {
        CommentType engineCommentType = engineComment.getCommentType();
        if (engineCommentType == CommentType.BLANK_LINE) {
            return null;
        }
        if (engineCommentType != expectIfNotBlank) {
            throw throwError(errorSource.buildError(preBuilt(
                    "Unexpected comment type " + engineCommentType + "; expected " + expectIfNotBlank
            )));
        }
        String value = engineComment.getValue();
        return value.startsWith(" ") ? value.substring(1) : value;
    }
}
