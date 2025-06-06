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

/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package space.arim.dazzleconf.backend.yaml;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.comments.CommentEventsCollector;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.events.CollectionEndEvent;
import org.snakeyaml.engine.v2.events.CollectionStartEvent;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.MappingStartEvent;
import org.snakeyaml.engine.v2.events.NodeEvent;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.SequenceStartEvent;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.SnakeCaseKeyMapper;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static space.arim.dazzleconf2.backend.Printable.preBuilt;

/**
 * An events reader that used {@link org.snakeyaml.engine.v2.composer.Composer} as an inspiration and a base. This
 * source file is covered by both licenses as a result.
 *
 */
final class ReadEvents {

    private final ReadYaml context;
    private final LoadSettings loadSettings;
    private final StandardConstructor standardConstructor;
    private final Parser parser;

    private final ScalarResolver scalarResolver;
    private final Map<Anchor, Object> anchors;
    private final Set<Object> recursiveNodes;
    private int nonScalarAliasesCount = 0;

    ReadEvents(ReadYaml context, LoadSettings loadSettings, StandardConstructor standardConstructor, Parser parser) {
        this.context = context;
        this.loadSettings = loadSettings;
        this.standardConstructor = standardConstructor;
        //assert (parser = new DebugParser(parser)) != null;
        this.parser = parser;

        scalarResolver = loadSettings.getSchema().getScalarResolver();
        anchors = new HashMap<>();
        recursiveNodes = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    LoadResult<@Nullable ReadYamlProduct> read() {
        ReadYamlProduct product = new ReadYamlProduct();
        try (WithScope commentScope = new WithScope(context.new Scope(new SetHeaderOrFooter(product)))) {
            return context.withHandleErrors(() -> {
                commentScope.read0(product);
                return product;
            });
        }
    }

    interface ComposeNode {
        void prepare(@Nullable Object value);
    }

    private final class WithScope implements AutoCloseable {

        private final ReadYaml.Scope scope;
        private final CommentEventsCollector blockCommentsCollector;
        private final CommentEventsCollector inlineCommentsCollector;

        WithScope(ReadYaml.Scope scope) {
            this.scope = scope;
            // Yes, we let blockCommentsCollector catch "IN_LINE" too. SnakeYaml-Engine can report block comments that
            // come after folded or literal multi-line strings as "IN_LINE" comments even if they're a new line
            blockCommentsCollector = new CommentEventsCollector(
                    parser, CommentType.BLANK_LINE, CommentType.BLOCK, CommentType.IN_LINE
            );
            inlineCommentsCollector = new CommentEventsCollector(parser, CommentType.IN_LINE);
        }

        @Override
        public void close() {
            assert blockCommentsCollector.consume().isEmpty() : "unconsumed block comments";
            assert inlineCommentsCollector.consume().isEmpty() : " unconsumed inline comments";
        }

        private void consumeEventId(Event.ID eventId) {
            if (!parser.checkEvent(eventId)) {
                // We can re-use SnakeCaseKeyMapper to build readable event names
                // For example, StreamStart -> stream-start -> stream start
                String displayName = new SnakeCaseKeyMapper().labelToKey(eventId.name()).toString();
                String message = "Expected " + displayName.replace('-', ' ');
                ErrorContext error = context.errorSource.buildError(preBuilt(message));
                throw context.throwError(error, null);
            }
            parser.next();
        }

        private void read0(ReadYamlProduct product) {
            // Drop the STREAM-START event.
            consumeEventId(Event.ID.StreamStart);
            // Compose the product if the stream is not empty
            if (parser.checkEvent(Event.ID.StreamEnd)) {
                // Empty document
                return;
            }
            // Collect inter-document start comments
            blockCommentsCollector.collectEvents();

            // Compose root node
            if (parser.checkEvent(Event.ID.DocumentStart)) {
                // Drop the DOCUMENT-START event
                parser.next();

                blockCommentsCollector.collectEvents();

                if (parser.checkEvent(Event.ID.SequenceStart) || parser.checkEvent(Event.ID.Scalar)) {
                    String message = context.getLibraryLang().yamlNotAMap();
                    ErrorContext error = context.errorSource.buildError(preBuilt(message));
                    throw context.throwError(error, null);
                }
                if (parser.checkEvent(Event.ID.MappingStart)) {
                    composeMap(null, product.dataTree, new SetHeaderOrFooter(product)).run();
                } else if (blockCommentsCollector.isEmpty()) {
                    // Another empty document
                    return;
                }
                blockCommentsCollector.collectEvents();

                // Drop the DOCUMENT-END event.
                consumeEventId(Event.ID.DocumentEnd);
            }
            scope.visitCommentSink(new SetHeaderOrFooter(product), blockCommentsCollector.consume());

            // Ensure that the stream contains no more documents.
            if (!parser.checkEvent(Event.ID.StreamEnd)) {
                Event event = parser.next();
                LibraryLang libraryLang = context.getLibraryLang();
                ErrorContext error = context.errorSource.buildError(preBuilt(libraryLang.failed()));
                error.addDetail(ErrorContext.BACKEND_MESSAGE, preBuilt("Expected a single document in the stream"));
                throw context.throwError(error, event.getStartMark().map(Mark::getLine).orElse(null));
            }
            // Drop the STREAM-END event.
            parser.next();
        }

        /**
         * Preconditions: next event is NodeEvent, and block comments are filled in collector. <br>
         * Postconditions: block comments are waiting (collector unfilled)
         *
         * @return true to rollback, happens only if node is a scalar, and the scalar is implicitly null
         */
        boolean composeNode(CommentSink commentSink, ComposeNode composeNode) {
            NodeEvent event = (NodeEvent) parser.peekEvent();
            @Nullable Anchor anchor = event.getAnchor().orElse(null);
            @Nullable Object value; // Only null for implicit nulls
            Runnable continuation;
            if (parser.checkEvent(Event.ID.Scalar)) {
                value = composeScalar((ScalarEvent) parser.next(), anchor, true);
                continuation = null;

            } else if (parser.checkEvent(Event.ID.SequenceStart)) {
                List<DataEntry> bucket = new ArrayList<>();
                value = bucket;
                continuation = composeSequence(anchor, bucket, commentSink);

            } else if (parser.checkEvent(Event.ID.MappingStart)) {
                DataTree.Mut bucket = new DataTree.Mut();
                value = bucket;
                continuation = composeMap(anchor, bucket, commentSink);

            } else {
                consumeEventId(Event.ID.Alias);
                Objects.requireNonNull(anchor, "anchor for alias event");
                if (!anchors.containsKey(anchor)) {
                    ErrorContext error = context.errorSource.buildError(preBuilt("Found undefined alias " + anchor));
                    throw context.throwError(error, event.getStartMark().map(Mark::getLine).orElse(null));
                }
                value = anchors.get(anchor);
                if (!DataTree.validateKey(value) && ++nonScalarAliasesCount > loadSettings.getMaxAliasesForCollections()) {
                    ErrorContext error = context.errorSource.buildError(preBuilt(
                            "Number of aliases for non-scalar nodes exceeds the specified max="
                                    + loadSettings.getMaxAliasesForCollections()));
                    throw context.throwError(error, event.getStartMark().map(Mark::getLine).orElse(null));
                }
                if (recursiveNodes.contains(value)) {
                    ErrorContext error = context.errorSource.buildError(preBuilt("Cannot use recursive anchors"));
                    throw context.throwError(error, event.getStartMark().map(Mark::getLine).orElse(null));
                }
                // We support comments here, even if snakeyaml's Composer originally does not
                inlineCommentsCollector.collectEvents();
                continuation = null;
            }
            composeNode.prepare(value);
            // Add inline comments: for example, this can pull comments beside map keys
            setInlineComments(commentSink, inlineCommentsCollector.consume());

            // Recurse for each entry
            if (continuation != null)
                continuation.run();

            return value == null;
        }

        private void registerAnchor(Anchor anchor, Object value) {
            Object prev = anchors.putIfAbsent(anchor, value);
            if (prev != null) {
                ErrorContext error = context.errorSource.buildError(preBuilt(
                        "Anchor " + anchor.getValue() + " already registered to " + prev
                ));
                throw context.throwError(error, null);
            }
        }

        /**
         * Postconditions: inline comments are filled in collector
         */
        private Object composeScalar(ScalarEvent ev, @Nullable Anchor anchor, boolean implicitNullIsNull) {
            Optional<String> tag = ev.getTag();
            boolean resolved = false;
            Tag nodeTag;
            if (!tag.isPresent() || tag.get().equals("!")) {
                nodeTag = scalarResolver.resolve(ev.getValue(), ev.getImplicit().canOmitTagInPlainScalar());
                resolved = true;
            } else {
                nodeTag = new Tag(tag.get());
            }
            // Construct from node
            Node node = new ScalarNode(nodeTag, resolved, ev.getValue(), ev.getScalarStyle(),
                    ev.getStartMark(), ev.getEndMark());
            Object value = standardConstructor.constructSingleDocument(Optional.of(node));
            // Handle nulls, see Backend javadoc
            if (value == null && (!implicitNullIsNull || ev.getValue().equals("null"))) {
                value = "null";
            }
            if (value != null && anchor != null) registerAnchor(anchor, value);
            // Only add inline comments if they're really inline - and not underneath flow/literal strings
            if (!(value instanceof String) || !ev.isLiteral() && !ev.isFolded()) {
                inlineCommentsCollector.collectEvents();
            }
            return value;
        }

        /**
         * Preconditions: next event is SequenceStartEvent, and block comments are filled in collector. <br>
         * Postconditions: block comments are waiting (collector unfilled)
         */
        private Runnable composeSequence(@Nullable Anchor anchor, List<DataEntry> bucket, CommentSink containerCommentSink) {
            class ComposeSequence implements ComposeCollection<SequenceStartEvent, List<DataEntry>> {

                private int index;

                @Override
                public Class<SequenceStartEvent> startEventType() {
                    return SequenceStartEvent.class;
                }

                @Override
                public Event.ID endEventId() {
                    return Event.ID.SequenceEnd;
                }

                @Override
                public Tag expectTag() {
                    return Tag.SEQ;
                }

                @Override
                public void composeEntry(List<DataEntry> bucket) {
                    int index = this.index++;
                    bucket.add(DUMMY_ENTRY); // Pre-fill up to the index

                    Event event = parser.peekEvent();
                    int indentLevel = context.getIndent(event);
                    Integer lineNumber = event.getStartMark().map(Mark::getLine).orElse(null);
                    ListEntry listEntry = new ListEntry(bucket, indentLevel, lineNumber, index);
                    /*
                     Comments before the list entry - we will delay this until we know the value
                     Why?

                     If the list element is a container, we don't consider the comments as belonging to it
                     Instead, we let the comments fall onto the first key/value or element within that container
                     Otherwise, in context of a list of lists, it's hard to tell whether the comments belong
                     E.g.
                        list-of-containers:
                         # Where
                         # do these
                         - - 'hello'
                         # comments

                         # Belong?
                         - key: 'value'
                     */
                    //scope.visitCommentSink(listEntry, blockCommentsCollector.consume());

                    // Recursion
                    context.keyPathStack.addLast("$");
                    try {
                        boolean rollback = composeNode(listEntry, value -> {
                            listEntry.value = value;

                            if (value instanceof DataTree || value instanceof List) {
                                // SEE ABOVE - we skip adding the list entry as a comment sink
                                listEntry.finish();
                            } else {
                                scope.visitCommentSink(listEntry, blockCommentsCollector.consume());
                            }
                        });
                        if (rollback) {
                            assert bucket.size() == index + 1;
                            bucket.remove(index);
                            this.index--;
                        }
                    } finally {
                        context.keyPathStack.pollLast();
                    }
                }
            };
            return composeCollection(new ComposeSequence(), anchor, bucket, containerCommentSink);
        }

        /**
         * Preconditions: next event is MappingStartEvent, and block comments are filled in collector. <br>
         * Postconditions: block comments are waiting (collector unfilled)
         */
        private Runnable composeMap(@Nullable Anchor anchor, DataTree.Mut bucket, CommentSink containerCommentSink) {
            class ComposeMap implements ComposeCollection<MappingStartEvent, DataTree.Mut> {

                @Override
                public Class<MappingStartEvent> startEventType() {
                    return MappingStartEvent.class;
                }

                @Override
                public Event.ID endEventId() {
                    return Event.ID.MappingEnd;
                }

                @Override
                public Tag expectTag() {
                    return Tag.MAP;
                }

                @Override
                public void composeEntry(DataTree.Mut bucket) {
                    if (!parser.checkEvent(Event.ID.Scalar)) {
                        throw context.throwError(context.errorSource.buildError(preBuilt(
                                "Using YAML lists or maps as keys is not supported"
                        )), null);
                    }
                    ScalarEvent scalarEvent = (ScalarEvent) parser.next();
                    Object key = composeScalar(scalarEvent, null, false);
                    MapEntry mapEntry = new MapEntry(
                            bucket, context.getIndent(scalarEvent),
                            scalarEvent.getStartMark().map(Mark::getLine).orElse(null), key
                    );
                    // Comments before the key/value pair
                    scope.visitCommentSink(mapEntry, blockCommentsCollector.consume());
                    // Comments after the key, but before the value
                    setInlineComments(mapEntry, inlineCommentsCollector.collectEvents().consume());
                    blockCommentsCollector.collectEvents();

                    // Recursion for the value
                    context.keyPathStack.addLast(key);
                    try {
                        composeNode(mapEntry, value -> mapEntry.value = value);
                    } finally {
                        context.keyPathStack.pollLast();
                    }
                }
            };
            return composeCollection(new ComposeMap(), anchor, bucket, containerCommentSink);
        }

        /**
         * Preconditions: next event is E, and block comments are filled in collector
         * Postconditions: block comments are waiting (collector unfilled)
         */
        private <E extends CollectionStartEvent, B> Runnable composeCollection(ComposeCollection<E, B> how,
                                                                               Anchor anchor, B bucket,
                                                                               CommentSink containerCommentSink) {
            E startEvent = how.startEventType().cast(parser.next());
            Optional<String> tag = startEvent.getTag();
            Tag nodeTag;
            Tag expectTag = how.expectTag();
            //boolean resolved = false;
            if (!tag.isPresent() || tag.get().equals("!")) {
                nodeTag = expectTag;
                //resolved = true;
            } else {
                nodeTag = new Tag(tag.get());
            }
            if (nodeTag != expectTag) {
                ErrorContext error = context.errorSource.buildError(preBuilt(
                        "Received explicit tag " + nodeTag + ", but expected " + expectTag
                ));
                throw context.throwError(error, startEvent.getStartMark().map(Mark::getLine).orElse(null));
            }
            return () -> {
                if (anchor != null) {
                    registerAnchor(anchor, bucket);
                }
                if (startEvent.isFlow()) {
                    Event endEvent;
                    // Handle comments inside the flow frame by creating a new scope
                    try (WithScope commentScope = new WithScope(context.new Scope(new FlowFrameInside()))) {
                        endEvent = commentScope.iterateToEnd(how, bucket);
                        commentScope.scope.visitCommentSink(new FlowFrameInside(), commentScope.blockCommentsCollector.consume());
                    }
                    int startFlowIndent = context.getIndent(startEvent);
                    int endFlowIndent = context.getIndent(endEvent);
                    // Comments on the flow frame itself
                    FlowFrameOutside flowFrameOutside = new FlowFrameOutside(
                            containerCommentSink, startFlowIndent, Math.min(startFlowIndent, endFlowIndent)
                    );
                    scope.visitCommentSink(flowFrameOutside, blockCommentsCollector.consume());
                    setInlineComments(containerCommentSink, inlineCommentsCollector.collectEvents().consume());
                } else {
                    this.iterateToEnd(how, bucket);
                }
            };
        }

        /**
         * Performs the iteration itself. <br>
         * Preconditions: a comment event, entry-related event, or collection end event may be next. <br>
         * Postconditions: all events processed, and block comments are waiting (collector unfilled)
         *
         * @param how how to fill each entry
         * @param bucket the bucket being filled
         * @return the collection end event (SequenceEndEvent or MappingEndEvent)
         * @param <E> unused
         * @param <B> the bucket type
         */
        private <E extends CollectionStartEvent, B> CollectionEndEvent iterateToEnd(ComposeCollection<E, B> how,
                                                                                    B bucket) {
            recursiveNodes.add(bucket);
            try {
                Event.ID endEventId = how.endEventId();
                while (!parser.checkEvent(endEventId)) {
                    blockCommentsCollector.collectEvents();
                    if (parser.checkEvent(endEventId)) {
                        break;
                    }
                    how.composeEntry(bucket);
                }
            } finally {
                recursiveNodes.remove(bucket);
            }
            // Returns the SequenceEndEvent / MappingEndEvent
            return (CollectionEndEvent) parser.next();
        }

        private void setInlineComments(CommentSink commentSink, List<CommentLine> commentLines) {
            if (commentLines == null || commentLines.isEmpty()) {
                return;
            }
            List<String> inlineComments = new ArrayList<>(commentLines.size());
            for (CommentLine commentLine : commentLines) {
                String commentValue = context.extractComment(commentLine, CommentType.IN_LINE);
                if (commentValue != null) {
                    inlineComments.add(commentValue);
                }
            }
            commentSink.setInlineComments(inlineComments);
        }
    }

    /// Used to help pre-fill lists
    private static final DataEntry DUMMY_ENTRY = new DataEntry(false);

    /// Used to parameterize reading collections
    interface ComposeCollection<E extends CollectionStartEvent, B> {
        Class<E> startEventType();

        Event.ID endEventId();

        Tag expectTag();

        /**
         * Preconditions: next event is appropriate for the entry, and block comments are filled in collector. <br>
         * Postconditions: block comments are waiting (collector unfilled)
         */
        void composeEntry(B bucket);
    }
}
