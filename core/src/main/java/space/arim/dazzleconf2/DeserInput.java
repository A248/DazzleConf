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
import space.arim.dazzleconf2.engine.KeyMapper;
import space.arim.dazzleconf2.engine.KeyPath;
import space.arim.dazzleconf2.engine.LoadListener;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.translation.LibraryLang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class DeserInput implements DeserializeInput {

    private final Object object;
    private final Source source;
    private final Context context;

    DeserInput(Object object, Source source, Context context) {
        this.object = object;
        this.source = source;
        this.context = context;
    }

    static final class Source {
        private final String[] pathStart;
        private final String pathLast;
        private final DataTree.Entry entry;

        Source(String[] pathStart, String pathLast, DataTree.Entry entry) {
            this.pathStart = pathStart;
            this.pathLast = pathLast;
            this.entry = entry;
        }
    }

    static final class Context {
        private final LibraryLang libraryLang;
        private final LoadListener loadListener;
        private final KeyMapper keyMapper;

        Context(LibraryLang libraryLang, LoadListener loadListener, KeyMapper keyMapper) {
            this.libraryLang = libraryLang;
            this.loadListener = loadListener;
            this.keyMapper = keyMapper;
        }
    }

    @Override
    public Object object() {
        return object;
    }

    @Override
    public KeyPath absoluteKeyPath() {
        KeyPath absolutePath = new KeyPath(source.pathStart);
        absolutePath.addBack(source.pathLast);
        return absolutePath;
    }

    @Override
    public KeyMapper keyMapper() {
        return context.keyMapper;
    }

    @Override
    public LoadResult<DataTree> requireDataTree() {
        Object object = object();
        if (object instanceof DataTree) {
            return LoadResult.of((DataTree) object);
        }
        return LoadError.wrongTypeForValue(context.libraryLang, object, DataTree.class);
    }

    @Override
    public void flagUpdate(KeyPath keyPath) {
        if (keyPath == null) {
            keyPath = new KeyPath();
        }
        keyPath.addFront(source.pathLast);
        context.loadListener.updatedMissingPath(keyPath);
    }

    @Override
    public DeserializeInput makeChild(Object value) {
        return new DeserInput(value, source, context);
    }

    @Override
    public ErrorContext buildError(String message) {
        LoadError loadError = new LoadError(message, context.libraryLang);
        // Add entry path
        List<String> fullPath = new ArrayList<>(source.pathStart.length + 1);
        fullPath.addAll(Arrays.asList(source.pathStart));
        fullPath.add(source.pathLast);
        loadError.addDetail(ErrorContext.ENTRY_PATH, fullPath);
        // Add line number
        source.entry.lineNumber().ifPresent(lineNumber -> {
            loadError.addDetail(ErrorContext.LINE_NUMBER, lineNumber);
        });
        return loadError;
    }

    @Override
    public <R> LoadResult<R> throwError(String message) {
        return LoadResult.failure(buildError(message));
    }
}
