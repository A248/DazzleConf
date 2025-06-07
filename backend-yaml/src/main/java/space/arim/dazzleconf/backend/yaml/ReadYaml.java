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

    private static class Product implements Backend.Document {
        private final DataTree.Mut dataTree = new DataTree.Mut();
        private List<String> header;
        private List<String> footer;

        @Override
        public @NonNull CommentData comments() {
            CommentData commentData = CommentData.empty();
            if (header != null) {
                commentData = commentData.setAt(CommentLocation.ABOVE, header);
            }
            if (footer != null) {
                commentData = commentData.setAt(CommentLocation.BELOW, footer);
            }
            return commentData;
        }

        @Override
        public @NonNull DataTree data() {
            return dataTree;
        }
    }

    // Entry-level and visiting state

    private final ArrayDeque<Object> keyPathStack = new ArrayDeque<>(); // Debug
    private final List<CommentSink> visibleSinksAbove = new ArrayList<>();
    private final List<CommentLine> commentBuffer = new ArrayList<>();

    // Error handling

    private ErrorContext thrownError;
    private static final IllegalStateException THROW_SIGNAL_ERROR;
    static {
        THROW_SIGNAL_ERROR = new IllegalStateException();
        THROW_SIGNAL_ERROR.setStackTrace(new StackTraceElement[0]);
    }

    private IllegalStateException throwError(ErrorContext error, Integer lineNumber) {
        String[] keyPathString = new String[keyPathStack.size()];
        int n = 0;
        for (Object keyPathElement : keyPathStack) {
            keyPathString[n++] = keyPathElement.toString();
        }
        error.addDetail(ErrorContext.ENTRY_PATH, new KeyPath.Immut(keyPathString));
        if (lineNumber != null) {
            error.addDetail(ErrorContext.LINE_NUMBER, lineNumber);
        }
        this.thrownError = error;
        throw THROW_SIGNAL_ERROR;
    }

    // Kick-starts the whole operation
    LoadResult<Backend.@NonNull Document> runForMapping(MappingNode mappingNode) {
        Product product = new Product();
        visibleSinksAbove.add(new SetHeaderOrFooter(product));
        try {
            visitMap(mappingNode, product.dataTree);
            visitCommentSink(new SetHeaderOrFooter(product));
            return LoadResult.of(product);
        } catch (IllegalStateException checkForError) {
            if (checkForError == THROW_SIGNAL_ERROR) {
                return LoadResult.failure(thrownError);
            }
            throw checkForError; // Shouldn't happen
        }
    }

    interface CommentSink {
        int indentLevel();

        void attachAttractedComments(boolean comingFromBelow, List<String> comments);

        void finish();
    }

    private void divideBufferAmongAboveAndBelow(CommentSink newSinkBelow) {
        // Goal: divide the comments cleanly, dividing whenever we encounter blank lines
        // Group clusters of comments based on their average indent, and find where the cross-over happens

        // We will score every cluster based on average of indentation amount
        // Instead of performing division and having to handle decimals, we'll scale values upwrd

        // Track the values we've gathered so far. As we go, we use subList to break it apart
        List<String> gathered = new ArrayList<>(commentBuffer.size());

        class Cluster {
            int startIndex;
            int indentScore;

            List<String> finish(int endIndex) {
                return gathered.subList(startIndex, endIndex);
            }

            void sendWhere(List<String> contents, CommentSink target) {
                boolean sendingDownward = newSinkBelow == target;
                target.attachAttractedComments(!sendingDownward, contents);
            }

            CommentSink computeWhereToSend(List<String> contents, boolean prioritizeUpward) {
                int clusterLength = contents.size();
                int indentScoreBelow = newSinkBelow.indentLevel() * clusterLength;
                // Fast path
                if (indentScoreBelow == indentScore) {
                    return newSinkBelow;
                }
                int closestScore = indentScoreBelow;
                CommentSink closestSink = newSinkBelow;
                // If any of the values here are less than indentScoreBelow, ignore them
                int[] indentScoresAbove = new int[visibleSinksAbove.size()];
                for (int n = visibleSinksAbove.size() - 1; n >= 0; n--) {
                    CommentSink currentSinkAbove = visibleSinksAbove.get(n);
                    int indentScoreAbove = currentSinkAbove.indentLevel() * clusterLength;
                    if (indentScoreAbove < indentScoreBelow) {
                        // We stop computing here, because it's not possible to comment below keys
                        break; // No reason to keep computing, as this number will only decrease
                    }
                    boolean closerScore = prioritizeUpward ?
                            isFirstCloser(indentScoreAbove, closestScore, indentScore) :
                            !isFirstCloser(closestScore, indentScoreAbove, indentScore);
                    if (closerScore) {
                        closestScore = indentScoreAbove;
                        closestSink = currentSinkAbove;
                    }
                }
                return closestSink;
            }
        }

        Cluster currentCluster = null;
        // If we've never seen any blank lines, and we're on the first cluster, send it upward upon a tie
        boolean sendAboveIfTied = true;
        boolean sendAllDownward = false;
        {
            for (CommentLine commentLine : commentBuffer) {
                String commentValue = extractComment(commentLine, CommentType.BLOCK);
                if (commentValue != null) {
                    if (currentCluster == null) {
                        currentCluster = new Cluster();
                    }
                    currentCluster.indentScore += getIndent(commentLine);
                    gathered.add(commentValue);
                    continue;
                }
                sendAboveIfTied = false;
                if (currentCluster == null) {
                    // Just browsing opening blank lines...
                    continue;
                }
                List<String> clusterContents = currentCluster.finish(gathered.size());
                CommentSink whereToSend;
                if (sendAllDownward) {
                    whereToSend = newSinkBelow;
                } else {
                    whereToSend = currentCluster.computeWhereToSend(clusterContents, false);
                    sendAllDownward = whereToSend == newSinkBelow;
                }
                currentCluster.sendWhere(clusterContents, whereToSend);
            }
            if (currentCluster != null) {
                List<String> clusterContents = currentCluster.finish(gathered.size());
                CommentSink whereToSend;
                if (sendAllDownward) {
                    whereToSend = newSinkBelow;
                } else {
                    whereToSend = currentCluster.computeWhereToSend(clusterContents, sendAboveIfTied);
                }
                currentCluster.sendWhere(clusterContents, whereToSend);
            }
        }
        /*
        // If there are no blank lines, or when the blank line count ties, we give preference based on 'preferAbove'
        int divideAt = -1, blankLineRecord = 0;
        int currentBlankLineCount = 0;
        int indentSum = 0; // If we never found a blank line, use this to calculate an average indent later

        for (CommentLine commentLine : commentBuffer) {
            String commentValue = extractComment(commentLine, CommentType.BLOCK);
            if (commentValue == null) {
                // Found a blank line!
                currentBlankLineCount++;
                continue;
            }
            if (currentBlankLineCount != 0) {
                boolean brokeRecord = preferAbove ?
                        currentBlankLineCount >= blankLineRecord : currentBlankLineCount > blankLineRecord;
                if (brokeRecord) {
                    blankLineRecord = currentBlankLineCount;
                    divideAt = gathered.size();
                }
                currentBlankLineCount = 0;
            }
            gathered.add(commentValue);
            indentSum += getIndent(commentLine);
            System.out.println("Indentation for comment is " + getIndent(commentLine));
        }
        if (divideAt == -1) {
            // No blank lines separate the comments. So, switch to an alternative algorithm using the average indent
            int indentScoreAbove = sinkAbove.indentLevel();
            int indentScoreBelow = sinkBelow.indentLevel();
            if (isFirstCloser(indentScoreAbove, indentScoreBelow, indentSum)) {
                sinkAbove.attachAttractedComments(true, gathered);
            } else {
                sinkBelow.attachAttractedComments(false, gathered);
            }
        } else {
            // Divide up all the comments we gathered, as promised
            sinkAbove.attachAttractedComments(true, gathered.subList(0, divideAt));
            sinkBelow.attachAttractedComments(false, gathered.subList(divideAt, gathered.size()));
        }
        */

        /*
        List<String> giveToAboveSpecifically = null; // Null if we never found a blank line
        List<String> giveToBelowSpecifically = null; // Null if we never found a blank line

        for (CommentLine commentLine : commentBuffer) {
            String commentValue = extractComment(commentLine, CommentType.BLOCK);
            if (commentValue == null) {
                // Found a blank line!
                if (divideAt == -1) {
                    // Transition states: start dividing between centerAbove and centerBelow
                    giveToAboveSpecifically = gathered;
                    giveToBelowSpecifically = new ArrayList<>();
                }
            } else if (giveToBelowSpecifically != null) {
                giveToBelowSpecifically.add(commentValue);
            } else {
                gathered.add(commentValue);
                indentSum += getCommentStart(commentLine);
            }
        }
        if (giveToBelowSpecifically == null) {
            // Instead of performing division and having to handle decimals, we just scale the indent levels upward
            int scaleTo = commentBuffer.length;
            int indentScoreAbove = sinkAbove.indentLevel() * scaleTo;
            int indentScoreBelow = sinkBelow.indentLevel() * scaleTo;
            if (isFirstCloser(indentScoreAbove, indentScoreBelow, indentSum)) {
                sinkAbove.attachAttractedComments(true, gathered);
            } else {
                sinkBelow.attachAttractedComments(false, gathered);
            }
        } else {
            sinkAbove.attachAttractedComments(true, giveToAboveSpecifically);
            sinkBelow.attachAttractedComments(false, giveToBelowSpecifically);
        }*/
    }

    // If there is a tie, gives priority to the higher-ranked competitor
    // If there is a tie and the competitors are ranked the same, gives priority to the first argument
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
        return competitor1 >= competitor2;
    }

    private static final class SetHeaderOrFooter implements CommentSink {

        private final Product product;

        private SetHeaderOrFooter(Product product) {
            this.product = product;
        }

        @Override
        public int indentLevel() {
            return 0;
        }

        @Override
        public void attachAttractedComments(boolean comingFromBelow, List<String> comments) {
            if (comingFromBelow) {
                product.header = comments;
            } else {
                product.footer = comments;
            }
        }

        @Override
        public void finish() {}
    }

    private static final class MapEntry implements CommentSink {

        private final DataTree.Mut container;
        private final int indentLevel;
        private final Object key;
        private final Object value;
        private CommentData commentData = CommentData.empty();

        private MapEntry(DataTree.Mut container, int indentLevel, Object key, Object value) {
            this.container = container;
            this.indentLevel = indentLevel;
            this.key = key;
            this.value = value;
            System.out.println("Using indent level " + indentLevel + " for key " + key);
        }

        @Override
        public int indentLevel() {
            return indentLevel;
        }

        @Override
        public void attachAttractedComments(boolean comingFromBelow, List<String> comments) {
            if (comingFromBelow) {
                commentData = commentData.setAt(CommentLocation.BELOW, comments);
            } else {
                commentData = commentData.setAt(CommentLocation.ABOVE, comments);
            }
        }

        @Override
        public void finish() {
            container.set(key, new DataEntry(value).withComments(commentData));
        }
    }

    private static final class BlackHoleCommentSink implements CommentSink {

        private final int indentLevel;

        private BlackHoleCommentSink(int indentLevel) {
            this.indentLevel = indentLevel;
        }

        @Override
        public int indentLevel() {
            return indentLevel;
        }

        @Override
        public void attachAttractedComments(boolean comingFromBelow, List<String> comments) {}

        @Override
        public void finish() {}
    }

    private void visitMap(MappingNode mappingNode, DataTree.Mut container) {
        visitBlockComments(mappingNode.getBlockComments());
        for (NodeTuple nodeTuple : mappingNode.getValue()) {
            Node keyNode = nodeTuple.getKeyNode();
            Node valueNode = nodeTuple.getValueNode();

            visitMapEntry(keyNode, valueNode, container);
        }
        visitBlockComments(mappingNode.getEndComments());
    }

    private void visitMapEntry(Node keyNode, Node valueNode, DataTree.Mut container) {
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
        // Prepare also for next round, but don't do it yet. Postpone continuations until later
        List<Consumer<ReadYaml>> continuations = new ArrayList<>();
        value = visitEntryContent(valueNode, keyNode, addInlineCommentsFrom, continuations);
        // Make new entry
        MapEntry mapEntry = new MapEntry(container, getIndent(keyNode), getKeyValue(keyNode), value);
        mapEntry.commentData = mapEntry.commentData.setAt(CommentLocation.INLINE, inlineComments);
        // Collect comments between this entry and the last one
        visitBlockComments(keyNode.getBlockComments());
        visitCommentSink(mapEntry);
        visitBlockComments(keyNode.getEndComments());
        // GO
        // Either return here, or recurse to find more values
        if (!continuations.isEmpty()) {
            keyPathStack.addLast(mapEntry.key);
            try {
                for (Consumer<ReadYaml> continuation : continuations) {
                    continuation.accept(this);
                }
            } finally {
                keyPathStack.pollLast();
            }
        }
    }

    private void visitCommentSink(CommentSink newCommentSink) {
        // Check previous comment sinks, and compare
        // Look at all comments, and have the last and current entry compete over them
        divideBufferAmongAboveAndBelow(newCommentSink);
        // Clear the buffer, so we can start looking for more
        commentBuffer.clear();
        // Prepare to see the next item
        // Push the current sink in front of any previous ones, according to what should be visible and comment-worthy
        // E.g., consider:
        //
        // section:
        //   mykey: 'hi'
        //   enabled: true
        // other-option: "replaces all the way to section"
        //
        // my-key should not obscure section. But enabled will replace mykey, and other-option replaces both of them
        for (int idx = visibleSinksAbove.size() - 1; idx >= 0; idx--) {
            CommentSink sinkAbove = visibleSinksAbove.get(idx);
            if (sinkAbove.indentLevel() >= newCommentSink.indentLevel()) {
                // The new sink will obscure it - so remove this sink
                sinkAbove.finish();
            }
        }
        visibleSinksAbove.add(newCommentSink);
    }

    private void visitBlockComments(List<CommentLine> snakeComments) {
        if (snakeComments != null) {
            this.commentBuffer.addAll(snakeComments);
        }
    }

    // Throws error if a map or sequence was used as a key
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
        throw throwError(errorSource.buildError(libraryLang.wrongTypeForValue(
                keyNode, "key type", keyNode.getClass().getSimpleName()
        )), keyNode.getStartMark().map(Mark::getLine).orElse(null));
    }

    private @NonNull Object visitEntryContent(Node valueNode, Node keyNodeIfNeeded,
                                              Consumer<Node> addInlineCommentsFrom,
                                              List<Consumer<ReadYaml>> continueVisiting) {
        if (valueNode instanceof AnchorNode) {
            return visitEntryContent(
                    ((AnchorNode) valueNode).getRealNode(), keyNodeIfNeeded, addInlineCommentsFrom, continueVisiting
            );
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
                // For scalar nodes: non-tree list items do not store comments in our library's API
                // For nested lists: the requirement of flow style FLOW will prevent attaching comments
                if (!(elemNode instanceof MappingNode)) {
                    CommentSink noCommentsHere = new BlackHoleCommentSink(getIndent(elemNode));
                    continueVisiting.add(read -> read.visitCommentSink(noCommentsHere));
                }
                elements.add(visitEntryContent(elemNode, null, (ignore) -> {}, continueVisiting));
            }
            continueVisiting.add(read -> read.visitBlockComments(valueNode.getEndComments()));
            return elements;
        }
        throw new IllegalStateException("Unknown Node subclass: " + valueNode);
    }

    /**
     * Gets the value out of a comment line, or returns {@code null} if we're looking at a blank line.
     * <p>
     * If the line is not blank but the comment type does not match {@code expectIfNotBlank}, throws an error.
     *
     * @param snakeComment the source {@code CommentLine} from SnakeYaml-Engine
     * @param expectIfNotBlank the comment type we're looking for
     * @return the value of the comment if it matched the type, or {@code null} if it's a blank line
     */
    private @Nullable String extractComment(CommentLine snakeComment, CommentType expectIfNotBlank) {
        CommentType engineCommentType = snakeComment.getCommentType();
        if (engineCommentType == CommentType.BLANK_LINE) {
            return null;
        }
        if (engineCommentType != expectIfNotBlank) {
            throw throwError(errorSource.buildError(preBuilt(
                    "Unexpected comment type " + engineCommentType + "; expected " + expectIfNotBlank
            )), snakeComment.getStartMark().map(Mark::getLine).orElse(null));
        }
        String value = snakeComment.getValue();
        return value.startsWith(" ") ? value.substring(1) : value;
    }

    private int getIndent(Node node) {
        Mark startMark = node.getStartMark().orElse(null);
        if (startMark == null) {
            throw throwError(errorSource.buildError(preBuilt("Start mark not present on node " + node)), null);
        }
        return startMark.getColumn();
    }

    private int getIndent(CommentLine commentLine) {
        Mark commentStartMark = commentLine.getStartMark().orElse(null);
        if (commentStartMark == null) {
            throw throwError(errorSource.buildError(preBuilt(
                    "Start mark not present on comment " + commentLine
            )), null);
        }
        return commentStartMark.getColumn();
    }
}
