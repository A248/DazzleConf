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
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static space.arim.dazzleconf2.backend.Printable.preBuilt;

final class ReadNodes {

    private final ReadYaml context;
    private final StandardConstructor standardConstructor;

    ReadNodes(ReadYaml context, StandardConstructor standardConstructor) {
        this.context = context;
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

    private final List<CommentLine> commentBuffer = new ArrayList<>();

    // Kick-starts the whole operation
    LoadResult<ReadYamlProduct> runForMapping(MappingNode mappingNode) {
        ReadYamlProduct product = new ReadYamlProduct();
        return context.withHandleErrors(() -> {
            WithScope withScope = new WithScope(context.new Scope(new SetHeaderOrFooter(product)));
            withScope.visitMap(mappingNode, product.dataTree);
            withScope.scope.visitCommentSink(new SetHeaderOrFooter(product), commentBuffer);
            return product;
        });
    }

    private final class WithScope {

        private final ReadYaml.Scope scope;

        private WithScope(ReadYaml.Scope scope) {
            this.scope = scope;
        }

        void visitMap(MappingNode mappingNode, DataTree.Mut bucket) {
            visitBlockComments(mappingNode.getBlockComments());
            for (NodeTuple nodeTuple : mappingNode.getValue()) {
                Node keyNode = nodeTuple.getKeyNode();
                Node valueNode = nodeTuple.getValueNode();

                visitMapEntry(keyNode, valueNode, bucket);
            }
            visitBlockComments(mappingNode.getEndComments());
        }

        void visitMapEntry(Node keyNode, Node valueNode, DataTree.Mut bucket) {
            // FIRST
            // Gather the value as a mutable container. This is kind of like look-ahead, but object-based
            InlineCommentStore inlineCommentStore = new InlineCommentStore();
            Continuation continuation = new Continuation();
            Object value = visitValue(valueNode, keyNode, inlineCommentStore, continuation);
            // Make new entry
            MapEntry mapEntry = new MapEntry(
                    bucket, context.getIndent(keyNode), keyNode.getStartMark().map(Mark::getLine).orElse(null),
                    getKeyValue(keyNode)
            );
            mapEntry.value = value;
            inlineCommentStore.moveAccumulatedTo(mapEntry);
            // Collect comments between this entry and the last one
            visitBlockComments(keyNode.getBlockComments());
            scope.visitCommentSink(mapEntry, commentBuffer);
            commentBuffer.clear();
            visitBlockComments(keyNode.getEndComments());
            // GO
            // Either return here, or recurse to find more values
            if (continuation.runner != null) {
                context.keyPathStack.addLast(mapEntry.key);
                try {
                    continuation.runner.accept(this);
                } finally {
                    context.keyPathStack.pollLast();
                }
            }
        }

        void visitList(SequenceNode sequenceNode, List<DataEntry> bucket) {
            visitBlockComments(sequenceNode.getBlockComments());
            List<Node> elemNodes = sequenceNode.getValue();
            // We'll pre-fill the bucket up to the size desired - then add elements at their indexes later
            for (int n = 0; n < elemNodes.size(); n++) {
                bucket.add(DUMMY_ENTRY); // Pre-fill up to the index
                visitListElement(elemNodes.get(n), n, bucket);
            }
            visitBlockComments(sequenceNode.getEndComments());
        }

        void visitListElement(Node elemNode, int index, List<DataEntry> bucket) {
            // Following the same procedure - almost - from visitMapEntry.
            InlineCommentStore inlineCommentStore = new InlineCommentStore();
            Continuation continuation = new Continuation();
            Object value = visitValue(elemNode, null, inlineCommentStore, continuation);

            // Start collecting comments
            visitBlockComments(elemNode.getBlockComments());
            if (continuation.runner == null) {
                // Make an entry if we're looking at a scalar
                // This lets comments gather on scalar list elements
                ListEntry listEntry = new ListEntry(
                        bucket, context.getIndent(elemNode), elemNode.getStartMark().map(Mark::getLine).orElse(null),
                        index
                );
                listEntry.value = value;
                inlineCommentStore.moveAccumulatedTo(listEntry);
                scope.visitCommentSink(listEntry, commentBuffer);
                commentBuffer.clear();
            }
            if (continuation.runner != null) {
                // `value` is itself a container (DataTree or List), so add it right away
                bucket.set(index, new DataEntry(value));
                // Run recursion here
                context.keyPathStack.addLast("$"); // Marker for an anonymous list entry
                try {
                    continuation.runner.accept(this);
                } finally {
                    context.keyPathStack.pollLast();
                }
            }
            visitBlockComments(elemNode.getEndComments());
        }
    }
    // Used to help pre-fill lists
    private static final DataEntry DUMMY_ENTRY = new DataEntry(false);

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
        throw context.throwError(context.errorSource.buildError(preBuilt(
                "Using YAML lists or maps as keys is not supported"
        )), keyNode.getStartMark().map(Mark::getLine).orElse(null));
    }

    private static final class Continuation {
        Consumer<ReadNodes.WithScope> runner;
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
            Object scalarValue = standardConstructor.constructSingleDocument(Optional.of(valueNode));
            // Handle multi-line strings placing inline comments on the key
            if (keyNodeIfFromMap != null) {
                inlineCommentStore.gatherFromNode(keyNodeIfFromMap);
            }
            inlineCommentStore.gatherFromNode(valueNode);
            return scalarValue == null ? "null" : scalarValue;
        }
        if (valueNode instanceof MappingNode) {
            MappingNode mappingNode = (MappingNode) valueNode;

            inlineCommentStore.gatherFromNode(keyNodeIfFromMap);
            DataTree.Mut dataTree = new DataTree.Mut();
            continuation.runner = read -> read.visitMap(mappingNode, dataTree);
            return dataTree;
        }
        if (valueNode instanceof SequenceNode) {
            SequenceNode sequenceNode = (SequenceNode) valueNode;

            inlineCommentStore.gatherFromNode(keyNodeIfFromMap);
            List<DataEntry> list = new ArrayList<>(sequenceNode.getValue().size());
            continuation.runner = read -> read.visitList(sequenceNode, list);
            return list;
        }
        throw new IllegalStateException("Unknown Node subclass: " + valueNode);
    }

    private final class InlineCommentStore {

        private List<String> inlineComments;

        void gatherFromNode(@Nullable Node node) {
            List<CommentLine> commentLines;
            if (node == null || (commentLines = node.getInLineComments()) == null) {
                return;
            }
            for (CommentLine commentLine : commentLines) {
                String commentValue = context.extractComment(commentLine, CommentType.IN_LINE);
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

}
