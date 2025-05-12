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
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.internals.LibraryLangKey;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

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
     * The locale to format messages in.
     *
     * @return the locale
     */
    @NonNull Locale getLocale();

    /**
     * Gets the main message to display as the reason for this error. This won't include additional details; for that
     * see {@link #displayDetails}.
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
    @NonNull Printable displayDetails();

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
     * Gets all the keys set on this error context. Implementations of this method are encouraged to provide
     * stable, deterministic sorting of the returned set.
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
}
