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

import java.io.IOException;
import java.util.*;

final class LoadError implements ErrorContext {

    private final CharSequence message;
    private final LibraryLang libraryLang;
    private final Map<Key<?, ?>, Object> contexts = new LinkedHashMap<>();

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

    @Override
    public String mainMessage() {
        return message.toString();
    }

    @Override
    public void mainMessage(Appendable output) throws IOException {
        output.append(message);
    }

    @Override
    public void mainMessage(StringBuilder output) {
        output.append(message);
    }

    @Override
    public String displayDetails() {
        StringBuilder builder = new StringBuilder();
        displayDetails(builder);
        return builder.toString();
    }

    @Override
    public void displayDetails(Appendable output) throws IOException {
        for (Key<?, ?> key : allKeys()) {
            formatKeyData(output, key);
            output.append('\n');
        }
    }

    @Override
    public void displayDetails(StringBuilder output) {
        try {
            displayDetails((Appendable) output);
        } catch (IOException e) {
            throw new AssertionError("StringBuilder does not throw IOException", e);
        }
    }

    private <V, R> void formatKeyData(Appendable output, Key<V, R> key) throws IOException {
        output.append(key.langKey.getMessage(libraryLang));
        output.append(": ");
        key.formatData.format(libraryLang, output, rawQuery(key));
    }

    private <V> V rawQuery(Key<V, ?> key) {
        @SuppressWarnings("unchecked")
        V value = (V) contexts.get(key);
        return value;
    }

    @Override
    public <V, R> R query(Key<V, R> key) {
        return key.mapReturnValue.map(rawQuery(key));
    }

    @Override
    public <V, R> void addDetail(Key<V, R> key, V context) {
        Objects.requireNonNull(context, "Cannot add null context");
        contexts.put(key, context);
    }

    @Override
    public void clearDetail(Key<?, ?> key) {
        contexts.remove(key);
    }

    @Override
    public void copyDetailsInto(ErrorContext target) {
        for (Key<?, ?> key :  contexts.keySet()) {
            copyContextInto(key, target);
        }
    }

    private <V> void copyContextInto(Key<V, ?> key, ErrorContext target) {
        target.addDetail(key, rawQuery(key));
    }

    @Override
    public Set<Key<?, ?>> allKeys() {
        return contexts.keySet();
    }
}
