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

import java.util.*;

/**
 * Context holder for errors in {@link LoadResult}
 *
 */
public interface ErrorContext {

    /**
     * Path of the configuration entry being scanned or operated upon
     */
    Key<List<String>> ENTRY_PATH = new Key<>(
            LibraryLang::entryPath,
            (value) -> {
                if (value == null) {
                    return ImmutableCollections.emptyList();
                } else {
                    @SuppressWarnings("unchecked")
                    List<String> casted = (List<String>) value;
                    return casted;
                }
            },
            (builder, value) -> {
                builder.append(String.join(".", value));
            }
    );

    /**
     * Line number in the source data
     */
    Key<OptionalInt> LINE_NUMBER = new Key<>(
            LibraryLang::lineNumber,
            value -> value == null ? OptionalInt.empty() : OptionalInt.of((Integer) value),
            (builder, lineNumber) -> {
                lineNumber.ifPresentOrElse(builder::append, () -> builder.append("<none>"));
            }
    );

    /**
     * Error message provided by the configuration format backend
     */
    Key<Optional<String>> BACKEND_MESSAGE = new Key<>(
            LibraryLang::backendMessage,
            value -> Optional.of((String) value),
            (builder, backendMsg) -> {
                backendMsg.ifPresentOrElse(builder::append, () -> builder.append("<none>"));
            }
    );

    /**
     * Gets the main message to display as the reason for this error. This won't include additional context; for that
     * see {@link #display}.
     *
     * @return the message
     */
    String mainMessage();

    /**
     * Formats this error context and includes all information. This message is user displayable, and it is also
     * intended to be sufficiently informative to avert the need for stacktraces.
     *
     * @return a displayable
     */
    String display();

    /**
     * Gets a piece of context from this error context, if it is set
     * @param key the key
     * @return the context detail if set, an empty optional or collection otherwise
     * @param <V> the value returned for the key
     */
    <V> V query(Key<V> key);

    /**
     * Gets all the keys set on this error context. Implementations of this method are encouraged to provide
     * stable, deterministic sorting of the returned set.
     *
     * @return all the keys, may or may not be immutable
     */
    Set<Key<?>> allKeys();

    /**
     * A marker for context keys
     * @param <V> the value type
     */
    final class Key<V> {

        final LibraryLangKey langKey;
        final MapReturnValue<V> mapReturnValue;
        final FormatData<V> formatData;

        Key(LibraryLangKey langKey, MapReturnValue<V> mapReturnValue, FormatData<V> formatData) {
            this.langKey = langKey;
            this.mapReturnValue = mapReturnValue;
            this.formatData = formatData;
        }

        interface MapReturnValue<V> {
            V map(Object value);
        }
        interface FormatData<V> {
            void format(StringBuilder builder, V value);
        }
    }
}
