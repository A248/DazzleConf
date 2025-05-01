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
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.io.IOException;
import java.util.*;

final class LoadError implements ErrorContext, LibraryLang.Accessor {

    private final CharSequence message;
    private final LibraryLang libraryLang;
    private final Map<Key<?>, Object> contexts = new LinkedHashMap<>();

    LoadError(CharSequence message, LibraryLang libraryLang) {
        this.message = Objects.requireNonNull(message, "message");
        this.libraryLang = Objects.requireNonNull(libraryLang, "libraryLang");
    }

    @Override
    public @NonNull LibraryLang getLibraryLang() {
        return libraryLang;
    }

    @Override
    public @NonNull Locale getLocale() {
        return libraryLang.getLocale();
    }

    @Override
    public @NonNull Printable mainMessage() {
        return Printable.preBuilt(message);
    }

    @Override
    public @NonNull Printable displayDetails() {
        return new Printable() {
            @Override
            public @NonNull String printString() {
                StringBuilder output = new StringBuilder();
                printTo(output);
                return output.toString();
            }

            @Override
            public void printTo(@NonNull Appendable output) throws IOException {
                for (Key<?> key : allKeys()) {
                    formatKeyData(output, key);
                    output.append('\n');
                }
            }

            @Override
            public void printTo(@NonNull StringBuilder output) {
                try {
                    printTo((Appendable) output);
                } catch (IOException e) {
                    throw new AssertionError("StringBuilder does not throw IOException", e);
                }
            }

            @Override
            public String toString() {
                return printString();
            }
        };
    }

    private <V> void formatKeyData(Appendable output, Key<V> key) throws IOException {
        output.append(key.langKey.getMessage(libraryLang));
        output.append(": ");
        key.formatData.format(output, query(key));
    }

    @Override
    public <V> V query(@NonNull Key<V> key) {
        Objects.requireNonNull(key, "key");
        @SuppressWarnings("unchecked")
        V value = (V) contexts.get(key);
        return value;
    }

    @Override
    public <V> void addDetail(@NonNull Key<V> key, @NonNull V context) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(context, "context");
        contexts.put(key, context);
    }

    @Override
    public void clearDetail(@NonNull Key<?> key) {
        contexts.remove(key);
    }

    @Override
    public void copyDetailsInto(@NonNull ErrorContext target) {
        for (Key<?> key :  contexts.keySet()) {
            copyContextInto(key, target);
        }
    }

    private <V> void copyContextInto(Key<V> key, ErrorContext target) {
        V val = query(key);
        assert val != null;
        target.addDetail(key, val);
    }

    @Override
    public @NonNull Set<@NonNull Key<?>> allKeys() {
        return contexts.keySet();
    }

}
