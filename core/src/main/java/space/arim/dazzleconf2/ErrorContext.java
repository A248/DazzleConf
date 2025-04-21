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

package space.arim.dazzleconf2;

import space.arim.dazzleconf.internal.util.ImmutableCollections;
import space.arim.dazzleconf2.translation.LibraryLangKey;
import space.arim.dazzleconf2.translation.LibraryLang;

import java.io.IOException;
import java.util.*;

/**
 * Context holder for errors in {@link LoadResult}. This is a decorator around a type safe map, used to provide
 * different error-related information and parts of context.
 *
 */
public interface ErrorContext {

    /**
     * Path of the configuration entry being scanned or operated upon
     */
    Key<List<String>, List<String>> ENTRY_PATH = new Key<>(
            LibraryLang::entryPath,
            (value) -> {
                return value == null ? ImmutableCollections.emptyList() : ImmutableCollections.listOf(value);
            },
            (lang, builder, value) -> {
                builder.append(String.join(".", value));
            }
    );

    /**
     * Line number in the source data
     */
    Key<Integer, OptionalInt> LINE_NUMBER = new Key<>(
            LibraryLang::line,
            value -> value == null ? OptionalInt.empty() : OptionalInt.of(value),
            (output, value) -> output.append(value.toString())
    );

    /**
     * Error message provided by the configuration format backend
     */
    Key<String, Optional<String>> BACKEND_MESSAGE = new Key<>(
            LibraryLang::backendMessage,
            Optional::ofNullable,
            Appendable::append
    );
    /**
     * A collection of errors causing this one
     */
    Key<List<ErrorContext>, List<ErrorContext>> CAUSES = new Key<>(
            LibraryLang::causalErrors,
            (value) -> {
                return value == null ? ImmutableCollections.emptyList() : ImmutableCollections.listOf(value);
            },
            (lang, builder, value) -> {
                int errorCount = value.size();
                int cap = Integer.min(4, errorCount);
                for (int n = 0; n < cap; n++) {
                    ErrorContext currentError = value.get(n);
                    builder.append("\n  ");
                    // 1. Add path
                    boolean pathOrLineNumber = false;
                    List<String> path = currentError.query(ENTRY_PATH);
                    if (!path.isEmpty()) {
                        builder.append(String.join(".", path));
                        pathOrLineNumber = true;
                    }
                    // 3. Line number
                    OptionalInt lineNumber = currentError.query(LINE_NUMBER);
                    if (lineNumber.isPresent()) {
                        if (pathOrLineNumber) builder.append(" ");
                        builder.append(lang.line());
                        builder.append(Integer.toString(lineNumber.getAsInt()));
                        pathOrLineNumber = true;
                    }
                    // 2. Error message
                    if (pathOrLineNumber) builder.append(':');
                    builder.append(currentError.mainMessage());
                }
                if (cap != errorCount) {
                    builder.append("+");
                    builder.append(Integer.toString(errorCount - cap));
                    builder.append(' ');
                    builder.append(lang.moreErrors());
                }
            }
    );

    /**
     * Gets the main message to display as the reason for this error. This won't include additional details; for that
     * see {@link #displayDetails}.
     *
     * @return the message
     */
    String mainMessage();

    /**
     * Gets the main message to display as the reason for this error. This won't include additional details; for that
     * see {@link #displayDetails}.
     * <p>
     * Same as {@link #mainMessage()} except that the message is sent to the output appendable
     *
     * @param output the string output
     * @throws IOException if the output threw this error, it is propagated
     */
    void mainMessage(Appendable output) throws IOException;

    /**
     * Gets the main message to display as the reason for this error. This won't include additional details; for that
     * see {@link #displayDetails}.
     * <p>
     * Same as {@link #mainMessage()} except that the message is sent to the output builder
     *
     * @param output the string output
     */
    void mainMessage(StringBuilder output);

    /**
     * Formats this error context and includes all information. This message is user displayable, and it is also
     * intended to be sufficiently informative to avert the need for stacktraces.
     *
     * @return a displayable message
     */
    String displayDetails();

    /**
     * Formats this error context and includes all information. This message is user displayable, and it is also
     * intended to be sufficiently informative to avert the need for stacktraces.
     * <p>
     * Same as {@link #displayDetails()} except that the message is sent to the output appendable
     *
     * @param output the string output
     * @throws IOException if the output threw this error, it is propagated
     */
    void displayDetails(Appendable output) throws IOException;

    /**
     * Formats this error context and includes all information. This message is user displayable, and it is also
     * intended to be sufficiently informative to avert the need for stacktraces.
     * <p>
     * Same as {@link #displayDetails()} except that the message is set to the output builder
     *
     * @param output the string output
     */
    void displayDetails(StringBuilder output);

    /**
     * Gets a piece of detail from this error context, if it is set
     * @param key the key
     * @return the context detail if set, an empty optional or collection otherwise
     * @param <V> the value put in for the key
     * @param <R> the value returned for the key
     */
    <V, R> R query(Key<V, R> key);

    /**
     * Adds a piece of detail to this error context, overriding previous values.
     * <p>
     * The passed context detail is copied and stored immutably in this error context. For example, lists are
     * made into immutable copies.
     *
     * @param key the key
     * @param context the context detail to set
     * @param <V> the value put in for the key
     * @param <R> the value returned for the key
     * @throws NullPointerException if the context detail is null
     */
    <V, R> void addDetail(Key<V, R> key, V context);

    /**
     * Clears the specified context for this error context
     *
     * @param key the key
     */
    void clearDetail(Key<?, ?> key);

    /**
     * Copies all context details from this error context into another one
     *
     * @param target the target error context
     */
    void copyDetailsInto(ErrorContext target);

    /**
     * Gets all the keys set on this error context. Implementations of this method are encouraged to provide
     * stable, deterministic sorting of the returned set.
     *
     * @return all the keys, may or may not be immutable
     */
    Set<Key<?, ?>> allKeys();

    /**
     * A marker for context keys
     * @param <V> the value type
     * @param <R> the return type
     */
    final class Key<V, R> {

        final LibraryLangKey langKey;
        final MapReturnValue<V, R> mapReturnValue;
        final FormatData<V> formatData;

        Key(LibraryLangKey langKey, MapReturnValue<V, R> mapReturnValue, FormatData<V> formatData) {
            this.langKey = langKey;
            this.mapReturnValue = mapReturnValue;
            this.formatData = formatData;
        }

        Key(LibraryLangKey langKey, MapReturnValue<V, R> mapReturnValue, FormatDataLangLess<V> formatData) {
            this.langKey = langKey;
            this.mapReturnValue = mapReturnValue;
            this.formatData = FormatData.langLess(formatData);
        }

        interface MapReturnValue<V, R> {
            R map(V value);
        }
        interface FormatData<V> {
            void format(LibraryLang libraryLang, Appendable output, V value) throws IOException;

            static <V> FormatData<V> langLess(FormatDataLangLess<V> format) {
                return (lang, output, value) -> format.format(output, value);
            }
        }
        interface FormatDataLangLess<V> {
            void format(Appendable output, V value) throws IOException;
        }
    }
}
