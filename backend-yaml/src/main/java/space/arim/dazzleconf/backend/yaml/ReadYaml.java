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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.Node;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

final class ReadYaml {

    final ErrorContext.Source errorSource;
    final ArrayDeque<Object> keyPathStack = new ArrayDeque<>(); // Debug

    // Error handling
    private ErrorContext thrownError;
    private static final IllegalStateException THROW_SIGNAL_ERROR  = new IllegalStateException();

    ReadYaml(ErrorContext.Source errorSource) {
        this.errorSource = errorSource;
    }

    LibraryLang getLibraryLang() {
        return LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
    }

    <R> LoadResult<R> withHandleErrors(Supplier<R> action) {
        try {
            return LoadResult.of(action.get());
        } catch (IllegalStateException checkForError) {
            if (checkForError == THROW_SIGNAL_ERROR) {
                return LoadResult.failure(thrownError);
            }
            throw checkForError; // Shouldn't happen
        }
    }

    IllegalStateException throwError(ErrorContext error, Integer lineNumber) {
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

    final class Scope {

        private final List<CommentSink> visibleSinksAbove = new ArrayList<>();

        Scope(CommentSink initialSink) {
            visibleSinksAbove.add(initialSink);
        }

        void visitCommentSink(CommentSink currentSink, List<CommentLine> blockComments) {
            // Check previous comment sinks, and compare
            // Look at all comments, and have the last and current entry compete over them
            divideAmongAboveAndBelow(currentSink, blockComments);
            // Prepare for the next round
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
                if (sinkAbove.indentLevel() >= currentSink.obscureAfterIndentLevel()) {
                    // The new sink will obscure it - so remove this sink
                    sinkAbove.finish();
                    visibleSinksAbove.remove(idx);
                }
            }
            visibleSinksAbove.add(currentSink);
        }

        private void divideAmongAboveAndBelow(CommentSink newSinkBelow, List<CommentLine> blockComments) {
            // Goal: divide the comments cleanly, dividing whenever we encounter blank lines
            // Group clusters of comments based on their average indent, and find where the cross-over happens

            // We will score every cluster based on average of indentation amount
            // Instead of performing division and having to handle decimals, we'll scale values upwrd

            // Track the values we've gathered so far. As we go, we use subList to break it apart
            List<String> gathered = new ArrayList<>(blockComments.size());

            class Cluster {
                int startIndex;
                int indentScore;

                List<String> finish(int endIndex) {
                    return gathered.subList(startIndex, endIndex);
                }

                void sendWhere(List<String> contents, CommentSink target) {
                    boolean sendingDownward = newSinkBelow == target;
                    target.setBlockComments(!sendingDownward, contents);
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
                        int currentScoreAbove = currentSinkAbove.indentLevel() * clusterLength;
                        if (currentScoreAbove < indentScoreBelow) {
                            break; // No reason to keep computing, as this number will only decrease
                        }
                        if (isFirstCloser(currentScoreAbove, closestScore, indentScore)) {
                            closestSink = currentSinkAbove;
                            closestScore = currentScoreAbove;
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
                for (CommentLine commentLine : blockComments) {
                    String commentValue = extractComment(commentLine, null);
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

    /**
     * Gets the value out of a comment line, or returns {@code null} if we're looking at a blank line.
     * <p>
     * If the line is not blank, {@code expectIfNotBlank} is not null, and the comment type does not match
     * {@code expectIfNotBlank}, throws an exception.
     *
     * @param snakeComment the source {@code CommentLine} from SnakeYaml-Engine
     * @param expectIfNotBlank the comment type we're looking for, or null to accept anything but blank
     * @return the value of the comment if it matched the type, or {@code null} if it's a blank line
     */
    @Nullable String extractComment(CommentLine snakeComment, @Nullable CommentType expectIfNotBlank) {
        CommentType engineCommentType = snakeComment.getCommentType();
        if (engineCommentType == CommentType.BLANK_LINE) {
            return null;
        }
        if (expectIfNotBlank != null && engineCommentType != expectIfNotBlank) {
            throw new IllegalStateException(
                    "Unexpected comment type " + engineCommentType + "; expected " + expectIfNotBlank + " at " +
                            snakeComment.getStartMark().map(Mark::getLine).orElse(null)
            );
        }
        String value = snakeComment.getValue();
        return value.startsWith(" ") ? value.substring(1) : value;
    }

    int getIndent(Event event) {
        return getIndentFrom(event, Event::getStartMark);
    }

    int getIndent(Node node) {
        return getIndentFrom(node, Node::getStartMark);
    }

    int getIndent(CommentLine comment) {
        return getIndentFrom(comment, CommentLine::getStartMark);
    }

    private <T, F extends Function<T, Optional<Mark>>> int getIndentFrom(T obj, F getStartMark) {
        Mark startMark = getStartMark.apply(obj).orElse(null);
        if (startMark == null) {
            throw new IllegalStateException("Start mark not present on " + obj);
        }
        return startMark.getColumn();
    }
}
