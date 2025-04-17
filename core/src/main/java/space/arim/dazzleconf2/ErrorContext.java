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

import java.io.IOException;
import java.util.*;

/**
 * Context holder for errors in {@link LoadResult}
 *
 */
public interface ErrorContext {

    /**
     * Path of the configuration entry being scanned or operated upon
     */
    Key<Optional<String>> ENTRY_PATH = new Key<>("Entry path");

    /**
     * Line number in the source data
     */
    Key<OptionalInt> LINE_NUMBER = new Key<>("Line number");

    /**
     * Error message provided by the configuration format backend
     */
    Key<Optional<String>> BACKEND_MESSAGE = new Key<>("Backend message");

    /**
     * IO error, usually thrown by the configuration format backend
     */
    Key<List<IOException>> IO_ERROR = new Key<>("IO error");

    /**
     * Other errors that might happen as part of a "more than one" operation, but are secondary to this error.
     * <p>
     * For example, when a configuration cannot be loaded and none of the migrations can be loaded either, a result
     * is returned where the reasons for the migrations' failure to load become the auxiliary errors.
     */
    Key<List<ErrorContext>> AUXILIARIES = new Key<>("Auxiliary errors");

    /**
     * Gets the main message to display as the reason for this error
     * @return the message
     */
    String message();

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

        private final String id;

        Key(String id) {
            this.id = Objects.requireNonNull(id);
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
