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
import org.snakeyaml.engine.v2.nodes.CollectionNode;
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
    private static final IllegalStateException THROW_SIGNAL_ERROR  = new IllegalStateException();

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
    LoadResult<Backend.@Nullable Document> runForMapping(MappingNode mappingNode) {
        Product product = new Product();
        visibleSinksAbove.add(new SetHeaderOrFooter(product));
        try {
            visitMap(mappingNode, product.dataTree);
            visitCommentSink(new SetHeaderOrFooter(product));

        } catch (IllegalStateException checkForError) {
            if (checkForError == THROW_SIGNAL_ERROR) {
                return LoadResult.failure(thrownError);
            }
            throw checkForError; // Shouldn't happen
        }
        // Finish!
        if (product.dataTree.isEmpty() && product.header == null && product.footer == null) {
            // Totally empty document... let's not take this seriously
            return LoadResult.of(null);
        }
        return LoadResult.of(product);
    }

    interface CommentSink {
        int indentLevel();

        void attachAttractedComments(boolean comingFromBelow, List<String> comments);

        default void setInlineComments(List<String> inlineComments) {}

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

            CommentSink computeWhereToSend(List<String> contents) {
                int clusterLength = contents.size();
                int indentScoreBelow = newSinkBelow.indentLevel() * clusterLength;
                // Fast path
                if (indentScoreBelow == indentScore) {
                    return newSinkBelow;
                }
                int closestScore = indentScoreBelow;
                CommentSink closestSink = newSinkBelow;
                for (int n = visibleSinksAbove.size() - 1; n >= 0; n--) {
                    CommentSink currentSinkAbove = visibleSinksAbove.get(n);
                    int indentScoreAbove = currentSinkAbove.indentLevel() * clusterLength;
                    if (indentScoreAbove < indentScoreBelow) {
                        break; // No reason to keep computing, as this number will only decrease
                    }
                    if (isFirstCloser(indentScoreAbove, closestScore, indentScore)) {
                        closestScore = indentScoreAbove;
                        closestSink = currentSinkAbove;
                    }
                }
                return closestSink;
            }
        }

        Cluster currentCluster = null;
        // If the first cluster has no blank lines before it but blank lines after it, send it upward
        boolean sendFirstUpward = true;
        boolean sendAllDownward = false;
        {
            for (CommentLine commentLine : commentBuffer) {
                String commentValue = extractComment(commentLine, CommentType.BLOCK);
                if (commentValue != null) {
                    if (currentCluster == null) {
                        currentCluster = new Cluster();
                        currentCluster.startIndex = gathered.size();
                    }
                    currentCluster.indentScore += getIndent(commentLine);
                    gathered.add(commentValue);
                    continue;
                }
                if (currentCluster != null) {
                    List<String> clusterContents = currentCluster.finish(gathered.size());
                    CommentSink whereToSend;
                    if (sendFirstUpward){
                        whereToSend = visibleSinksAbove.get(visibleSinksAbove.size() - 1);
                    } else if (sendAllDownward) {
                        whereToSend = newSinkBelow;
                    } else {
                        whereToSend = currentCluster.computeWhereToSend(clusterContents);
                        sendAllDownward = whereToSend == newSinkBelow;
                    }
                    currentCluster.sendWhere(clusterContents, whereToSend);
                    currentCluster = null;
                }
                sendFirstUpward = false;
            }
            if (currentCluster != null) {
                List<String> clusterContents = currentCluster.finish(gathered.size());
                CommentSink whereToSend;
                if (sendAllDownward) {
                    whereToSend = newSinkBelow;
                } else {
                    whereToSend = currentCluster.computeWhereToSend(clusterContents);
                }
                currentCluster.sendWhere(clusterContents, whereToSend);
            }
        }
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

    private static abstract class ContainerEntry<B> implements CommentSink {

        final B bucket;
        private final int indentLevel;
        CommentData commentData = CommentData.empty();

        private ContainerEntry(B bucket, int indentLevel) {
            this.bucket = bucket;
            this.indentLevel = indentLevel;
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
        public void setInlineComments(List<String> inlineComments) {
            commentData = commentData.setAt(CommentLocation.INLINE, inlineComments);
        }
    }

    private static final class MapEntry extends ContainerEntry<DataTree.Mut> {

        private final Object key;
        private final Object value;

        private MapEntry(DataTree.Mut bucket, int indentLevel, Object key, Object value) {
            super(bucket, indentLevel);
            this.key = key;
            this.value = value;
        }

        @Override
        public void finish() {
            bucket.set(key, new DataEntry(value).withComments(commentData));
        }
    }

    private static final class ScalarListEntry extends ContainerEntry<List<DataEntry>> {

        private final int index;
        private final Object value;

        private ScalarListEntry(List<DataEntry> container, int indentLevel, int index, Object value) {
            super(container, indentLevel);
            this.index = index;
            this.value = value;
        }

        @Override
        public void finish() {
            bucket.set(index, new DataEntry(value).withComments(commentData));
        }
    }

    private void visitMap(MappingNode mappingNode, DataTree.Mut bucket) {
        visitBlockComments(mappingNode.getBlockComments());
        for (NodeTuple nodeTuple : mappingNode.getValue()) {
            Node keyNode = nodeTuple.getKeyNode();
            Node valueNode = nodeTuple.getValueNode();

            visitMapEntry(keyNode, valueNode, bucket);
        }
        visitBlockComments(mappingNode.getEndComments());
    }

    private void visitMapEntry(Node keyNode, Node valueNode, DataTree.Mut bucket) {
        // FIRST
        // Gather the value as a mutable container. This is kind of like look-ahead, but object-based
        InlineCommentStore inlineCommentStore = new InlineCommentStore();
        Continuation continuation = new Continuation();
        Object value = visitValue(valueNode, keyNode, inlineCommentStore, continuation);
        // Make new entry
        MapEntry mapEntry = new MapEntry(bucket, getIndent(keyNode), getKeyValue(keyNode), value);
        inlineCommentStore.moveAccumulatedTo(mapEntry);
        // Collect comments between this entry and the last one
        visitBlockComments(keyNode.getBlockComments());
        visitCommentSink(mapEntry);
        visitBlockComments(keyNode.getEndComments());
        // GO
        // Either return here, or recurse to find more values
        if (continuation.runner != null) {
            keyPathStack.addLast(mapEntry.key);
            try {
                continuation.runner.accept(this);
            } finally {
                keyPathStack.pollLast();
            }
        }
    }

    // Used to help pre-fill lists
    private static final DataEntry DUMMY_ENTRY = new DataEntry(false);

    private void visitList(SequenceNode sequenceNode, List<DataEntry> bucket) {
        visitBlockComments(sequenceNode.getBlockComments());
        List<Node> elemNodes = sequenceNode.getValue();
        // We'll pre-fill the bucket up to the size desired - then add elements at their indexes later
        for (int n = 0; n < elemNodes.size(); n++) {
            bucket.add(DUMMY_ENTRY); // Pre-fill up to index n
            visitListElement(elemNodes.get(n), n, bucket);
        }
        visitBlockComments(sequenceNode.getEndComments());
    }

    private void visitListElement(Node elemNode, int index, List<DataEntry> bucket) {
        // Following the same procedure - almost - from visitMapEntry.
        InlineCommentStore inlineCommentStore = new InlineCommentStore();
        Continuation continuation = new Continuation();
        Object value = visitValue(elemNode, null, inlineCommentStore, continuation);

        // Start collecting comments
        visitBlockComments(elemNode.getBlockComments());
        if (continuation.runner == null) {
            // Only make an entry if we're looking at a scalar
            // This ensures that comments gather on map entries and scalar list elements, not the containers themselves
            ScalarListEntry scalarListEntry = new ScalarListEntry(bucket, getIndent(elemNode), index, value);
            inlineCommentStore.moveAccumulatedTo(scalarListEntry);
            visitCommentSink(scalarListEntry);
        } else {
            // `value` is itself a container (DataTree or List), so add it right away
            bucket.set(index, new DataEntry(value));
            // Run recursion here
            keyPathStack.addLast("$"); // Marker for an anonymous list entry
            try {
                continuation.runner.accept(this);
            } finally {
                keyPathStack.pollLast();
            }
        }
        visitBlockComments(elemNode.getEndComments());
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
                visibleSinksAbove.remove(idx);
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
        assert keyNode instanceof CollectionNode;
        throw throwError(errorSource.buildError(preBuilt(
                "Using YAML lists or maps as keys is not supported"
        )), keyNode.getStartMark().map(Mark::getLine).orElse(null));
    }

    private static final class Continuation {
        private Consumer<ReadYaml> runner;
    }

    private @NonNull Object visitValue(Node valueNode, @Nullable Node keyNodeIfFromMap,
                                       InlineCommentStore inlineCommentStore,
                                       Continuation continuation) {
        if (valueNode instanceof AnchorNode) {
            return visitValue(
                    ((AnchorNode) valueNode).getRealNode(), keyNodeIfFromMap, inlineCommentStore, continuation
            );
        }
        if (valueNode instanceof ScalarNode) {
            inlineCommentStore.storeFromNode(valueNode);
            Object scalarValue = standardConstructor.constructSingleDocument(Optional.of(valueNode));
            return scalarValue == null ? "null" : scalarValue;
        }
        if (valueNode instanceof MappingNode) {
            inlineCommentStore.storeFromNode(keyNodeIfFromMap);
            DataTree.Mut dataTree = new DataTree.Mut();
            continuation.runner = read -> read.visitMap((MappingNode) valueNode, dataTree);
            return dataTree;
        }
        if (valueNode instanceof SequenceNode) {
            inlineCommentStore.storeFromNode(keyNodeIfFromMap);
            SequenceNode sequenceNode = (SequenceNode) valueNode;
            List<DataEntry> list = new ArrayList<>(sequenceNode.getValue().size());
            continuation.runner = read -> read.visitList((SequenceNode) valueNode, list);
            return list;
        }
        throw new IllegalStateException("Unknown Node subclass: " + valueNode);
    }

    private final class InlineCommentStore {

        private List<String> inlineComments;

        void storeFromNode(@Nullable Node node) {
            List<CommentLine> snakeInlineComments;
            if (node == null || (snakeInlineComments = node.getInLineComments()) == null) {
                return;
            }
            for (CommentLine commentLine : snakeInlineComments) {
                String commentValue = extractComment(commentLine, CommentType.IN_LINE);
                if (commentValue != null) {
                    if (inlineComments == null) {
                        inlineComments = new ArrayList<>();
                    }
                    inlineComments.add(commentValue);
                }
            }
        }

        private void moveAccumulatedTo(CommentSink commentSink) {
            if (inlineComments != null) {
                commentSink.setInlineComments(inlineComments);
            }
        }
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
