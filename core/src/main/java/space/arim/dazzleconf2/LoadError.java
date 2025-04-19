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

import space.arim.dazzleconf2.translation.LibraryLangKey;
import space.arim.dazzleconf2.translation.LibraryLang;

import java.util.*;

final class LoadError implements ErrorContext {

    private final LibraryLangKey message;
    private final LibraryLang libraryLang;
    private final Map<Key<?>, Object> contexts = new LinkedHashMap<>();

    LoadError(LibraryLangKey message, LibraryLang libraryLang) {
        this.message = Objects.requireNonNull(message, "message");
        this.libraryLang = Objects.requireNonNull(libraryLang, "libraryLang");
    }

    void addContext(Key<?> key, Object value) {
        contexts.put(key, value);
    }

    @Override
    public String mainMessage() {
        return message.getMessage(libraryLang);
    }

    @Override
    public String display() {
        StringBuilder builder = new StringBuilder();
        builder.append(libraryLang.errorIntro());
        builder.append('\n');
        builder.append(mainMessage());
        builder.append('\n');
        builder.append(libraryLang.errorContext());
        builder.append('\n');
        for (Key<?> key : allKeys()) {
            formatKeyData(builder, key);
            builder.append('\n');
        }
        return builder.toString();
    }

    private <V> void formatKeyData(StringBuilder builder, Key<V> key) {
        builder.append(key.langKey.getMessage(libraryLang));
        builder.append(": ");
        key.formatData.format(builder, query(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V query(Key<V> key) {
        return (V) contexts.get(key);
    }

    @Override
    public Set<Key<?>> allKeys() {
        return contexts.keySet();
    }
}
