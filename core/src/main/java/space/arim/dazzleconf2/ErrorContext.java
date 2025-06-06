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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.internals.lang.LibraryLangKey;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Set;

/**
 * Context holder for errors in {@link LoadResult}. This is a decorator around a type safe map, used to provide
 * different error-related information and parts of context.
 *
 */
public interface ErrorContext {

    /**
     * Path of the configuration entry being scanned or operated upon
     */
    Key<KeyPath> ENTRY_PATH = new Key<>(LibraryLang::location, (output, value) -> value.printTo(output));

    /**
     * Line number in the source data
     */
    Key<Integer> LINE_NUMBER = new Key<>(LibraryLang::line, (output, value) -> output.append(value.toString()));

    /**
     * Error message provided by the configuration format backend
     */
    Key<Printable> BACKEND_MESSAGE = new Key<>(LibraryLang::backendMessage, (output, value) -> value.printTo(output));

    /**
     * A URL pointing to an online syntax linter.
     * <p>
     * If the backend wishes, it can provide this URL, to an online syntax linter, to help the user check what's wrong.
     */
    Key<URL> SYNTAX_LINTER = new Key<>(LibraryLang::syntaxLinter, (output, value) -> output.append(value.toExternalForm()));

    /**
     * The locale to format messages in.
     *
     * @return the locale
     */
    @NonNull Locale getLocale();

    /**
     * Gets the main message to display as the reason for this error. This won't include additional details; for that
     * see {@link #display}.
     *
     * @return the message
     */
    @NonNull Printable mainMessage();

    /**
     * Formats this error context and includes all information. This message is user displayable, and it is also
     * intended to be sufficiently informative to avert the need for stacktraces.
     *
     * @return a displayable message
     */
    @NonNull Printable display();

    /**
     * Gets a piece of detail from this error context, if it is set
     * @param key the key
     * @return the context detail if set, or null if not present
     * @param <V> the value put in for the key
     */
    <V> @Nullable V query(@NonNull Key<V> key);

    /**
     * Adds a piece of detail to this error context, overriding previous values.
     * <p>
     * The passed context detail is copied and stored immutably in this error context. For example, lists are
     * made into immutable copies.
     *
     * @param key the key
     * @param context the context detail to set
     * @param <V> the value put in for the key
     * @throws NullPointerException if the key or context detail is null
     */
    <V> void addDetail(@NonNull Key<V> key, @NonNull V context);

    /**
     * Clears the specified context for this error context
     *
     * @param key the key
     */
    void clearDetail(@NonNull Key<?> key);

    /**
     * Copies all context details from this error context into another one
     *
     * @param target the target error context
     */
    void copyDetailsInto(@NonNull ErrorContext target);

    /**
     * Gets all the keys set on this error context.
     * <p>
     * Implementations of this method are encouraged to provide stable, deterministic sorting of the returned set.
     * Mutability is not defined, and callers should not try to modify it.
     *
     * @return all the keys, may or may not be immutable
     */
    @NonNull Set<@NonNull Key<?>> allKeys();

    /**
     * A marker for context keys
     * @param <V> the value type
     */
    final class Key<V> {

        final LibraryLangKey langKey;
        final FormatData<V> formatData;

        Key(LibraryLangKey langKey, FormatData<V> formatData) {
            this.langKey = langKey;
            this.formatData = formatData;
        }

        interface FormatData<V> {
            void format(Appendable output, V value) throws IOException;
        }
    }

    /**
     * A source of error contexts.
     * <p>
     * This class might be implemented in different places, and therefore provide different context keys in
     * returned {@code ErrorContext}s.
     *
     */
    interface Source {

        /**
         * The locale to format error messages in.
         *
         * @return the locale
         */
        @NonNull Locale getLocale();

        /**
         * Builds an error context based on the implementation.
         * <p>
         * This function takes a {@code Printable} for flexibility. Using {@link Printable#preBuilt(CharSequence)} will
         * suffice in many cases.
         *
         * @param message the main error messge
         * @return an error context
         */
        @NonNull ErrorContext buildError(@NonNull Printable message);

        /**
         * Builds an error context, wraps it in a <code>LoadResult</code> and returns it.
         * <p>
         * This function does not actually throw anything. It is named as such so that your code can look like this:
         * <pre>
         *     {@code
         *         return operable.throwError("failure");
         *     }
         * </pre>
         *
         * @param message the main error message
         * @return an error result
         * @param <R> the type of the result value (can be anything since the result will be an error)
         */
        <R> @NonNull LoadResult<R> throwError(@NonNull CharSequence message);

        /**
         * Builds an error context, wraps it in a <code>LoadResult</code> and returns it.
         * <p>
         * This function does not actually throw anything. It is named as such to communicate the logical termination of
         * a block of code.
         *
         * @param message the main error message
         * @return an error result
         * @param <R> the type of the result value (can be anything since the result will be an error)
         */
        <R> @NonNull LoadResult<R> throwError(@NonNull Printable message);

    }
}
