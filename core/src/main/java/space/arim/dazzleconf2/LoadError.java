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

import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.translation.LibraryLang;

import java.util.*;

final class LoadError implements ErrorContext {

    private final CharSequence message;
    private final LibraryLang libraryLang;
    private final Map<Key<?>, Object> contexts = new LinkedHashMap<>();

    LoadError(CharSequence message, LibraryLang libraryLang) {
        this.message = Objects.requireNonNull(message, "message");
        this.libraryLang = Objects.requireNonNull(libraryLang, "libraryLang");
    }

    static <R> LoadResult<R> wrongTypeForValue(LibraryLang libraryLang, Object value, Class<?> expectedType) {
        return LoadResult.failure(new LoadError(libraryLang.wrongTypeForValue(
                value, displayCanonicalType(expectedType, null), displayCanonicalType(value.getClass(), value)
        ), libraryLang));
    }

    private static String displayCanonicalType(Class<?> type, Object typeAssist) {
        if (type.equals(String.class)) {
            return "text/string";
        }
        if (type.equals(Byte.class)) {
            return "small integer";
        }
        if (type.equals(Short.class) || type.equals(Integer.class) || type.equals(Long.class)) {
            return "integer";
        }
        if (type.equals(Character.class)) {
            return "character";
        }
        if (type.equals(Float.class) || type.equals(Double.class)) {
            return "decimal";
        }
        if (typeAssist instanceof List || type.equals(List.class)) {
            return "list";
        }
        if (typeAssist instanceof DataTree || type.equals(DataTree.class)) {
            return "configuration section";
        }
        throw new IllegalArgumentException("Not a canonical type " + type);
    }

    void addContext(Key<?> key, Object value) {
        contexts.put(key, value);
    }

    @Override
    public String mainMessage() {
        return message.toString();
    }

    @Override
    public String display() {
        StringBuilder builder = new StringBuilder();
        builder.append(libraryLang.errorIntro());
        builder.append('\n');
        builder.append(message);
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
