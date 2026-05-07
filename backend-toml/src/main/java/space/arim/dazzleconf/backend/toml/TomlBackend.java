/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

package space.arim.dazzleconf.backend.toml;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.JTomlServiceImpl;
import io.github.wasabithumb.jtoml.comment.Comment;
import io.github.wasabithumb.jtoml.comment.CommentPosition;
import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import io.github.wasabithumb.jtoml.except.parse.TomlLocalParseException;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataList;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.backend.ReadableRoot;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.backend.KebabCaseKeyMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static space.arim.dazzleconf2.backend.Printable.preBuilt;

/**
 * A backend for TOML.
 * <p>
 * <b>Comments</b>
 * <p>
 * This backend supports writing comments. It can support reading comments, but at the cost of disabling below and
 * inline comments, as well as the document header and footer.
 * <p>
 * The challenge is that the backing TOML library does not provide enough information to reconstitute the correct
 * location of previously written comments. The TOML library exposes an unordered document tree, and it sets comments
 * on values within that tree. This means we must trust in the TOML library to read comments where it thinks they are
 * located, which in practice is above the entry.
 * <p>
 * Ergo, round-trip preservation of TOML comments is only possible for comments above each entry. Users of this backend
 * can choose whether to write all comments (always) or to read and write only the comments above each entry. The
 * default is {@link TomlCommentMode#WRITE_ALWAYS}, which can be changed by using the {@link TomlBackend.Builder}.
 * <p>
 * <b>Types</b>
 * <p>
 * Although TOML inherently supports date and time types, using them is incompatible with the library model. If a date
 * or time type is encountered, an error will be returned. Therefore, users writing TOML must quote strings that seem
 * to follow a time or date format.
 * <p>
 * TOML does not have a concept of null, so no special handling is needed for nulls.
 */
public final class TomlBackend implements Backend {

    private final ReadableRoot dataRoot;
    private final URL syntaxLinter;
    private final TomlCommentMode commentMode;

    private final JToml jToml;

    private TomlBackend(ReadableRoot dataRoot, Builder builder) {
        this.dataRoot = Objects.requireNonNull(dataRoot);
        this.syntaxLinter = syntaxLinterOrDefault(builder);
        this.commentMode = builder.commentMode;

        JTomlOptions jTomlOptions = JTomlOptions.builder()
                .set(JTomlOption.READ_COMMENTS, commentMode == TomlCommentMode.ROUND_TRIP_ABOVE_ONLY)
                .set(JTomlOption.WRITE_EMPTY_TABLES, true)
                .set(JTomlOption.WRITE_COMMENTS, true)
                .build();
        // Skip service loading and construct the provider directly
        jToml = new JTomlServiceImpl().createInstance(jTomlOptions);
    }

    /**
     * Creates from a readable data root. For example, to load from a file:
     * <pre>
     *     {@code
     *         Backend tomlBackend = new TomlBackend(new PathRoot(Path.of("config.toml")));
     *         Configuration<MyConfig> configuration = Configuration.defaultBuilder(MyConfig.class).build();
     *         LoadResult<MyConfig> loaded = configuration.configureWith(tomlBackend);
     *     }
     * </pre>
     *
     * @param dataRoot the data root from which to read and write
     */
    public TomlBackend(@NonNull ReadableRoot dataRoot) {
        this(dataRoot, new Builder());
    }

    private static URL syntaxLinterOrDefault(Builder builder) {
        URL syntaxLinter = builder.syntaxLinter;
        return syntaxLinter == null ? defaultSyntaxLinter() : syntaxLinter;
    }

    private static URL defaultSyntaxLinter() {
        try {
            return new URI("https", "toolbox.helpch.at", "/validators/toml", null).toURL();
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * Builder for a TOML backend. Allows setting additional options.
     *
     */
    public static final class Builder {

        private URL syntaxLinter;
        private TomlCommentMode commentMode = TomlCommentMode.WRITE_ALWAYS;

        /**
         * Creates the builder
         */
        public Builder() {}

        /**
         * Sets the syntax linter used by the backend.
         * <p>
         * <b>This is an experimental method.</b> See the {@code @API} annotation.
         * <p>
         * The argument URL may be provided to users in the form of error messages. It should point to a live website
         * where end users can paste and validate their configuration file's syntax.
         *
         * @param syntaxLinter the syntax linter
         * @return this builder
         */
        @API(status = API.Status.EXPERIMENTAL)
        public @NonNull Builder syntaxLinter(@NonNull URL syntaxLinter) {
            this.syntaxLinter = Objects.requireNonNull(syntaxLinter, "syntaxLinter");
            return this;
        }

        /**
         * Sets the comment mode used by the backend.
         * <p>
         * See the variants of the enum for more information. Defaults to {@link TomlCommentMode#WRITE_ALWAYS}
         *
         * @param commentMode the comment mode
         * @return this builder
         */
        public @NonNull Builder commentMode(@NonNull TomlCommentMode commentMode) {
            this.commentMode = Objects.requireNonNull(commentMode, "commentMode");
            return this;
        }

        /**
         * Builds into a backend. This can be called as many times as needed.
         *
         * @param dataRoot the data root from which to read and write
         * @return the backend
         */
        public @NonNull TomlBackend build(@NonNull ReadableRoot dataRoot) {
            return new TomlBackend(dataRoot, this);
        }
    }

    @Override
    public @NonNull LoadResult<@Nullable Document> read(ErrorContext.@NonNull Source errorSource) {
        TomlDocument tomlDocument;
        try {
            if (!dataRoot.dataExists()) {
                return LoadResult.of(null);
            }
            tomlDocument = dataRoot.openReader(jToml::read);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (TomlIOException ioEx) {
            throw new UncheckedIOException(ioEx.getCause());
        } catch (TomlLocalParseException parseEx) {
            LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
            ErrorContext error = errorSource.buildError(preBuilt(libraryLang.syntax()));
            error.addDetail(ErrorContext.BACKEND_MESSAGE, preBuilt(parseEx.getMessage()));
            error.addDetail(ErrorContext.SYNTAX_LINTER, syntaxLinter);
            error.addDetail(ErrorContext.LINE_NUMBER, parseEx.getLineNumber());
            return LoadResult.failure(error);
        } catch (TomlException otherEx) {
            LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
            ErrorContext error = errorSource.buildError(preBuilt(libraryLang.otherReason()));
            error.addDetail(ErrorContext.BACKEND_MESSAGE, preBuilt(otherEx.getMessage()));
            return LoadResult.failure(error);
        }
        ReadToml readToml = new ReadToml(errorSource);
        DataTree.Mut data = new DataTree.Mut();
        readToml.readTomlTree(tomlDocument, data);
        if (readToml.error != null) {
            return LoadResult.failure(readToml.error);
        }
        return LoadResult.of(new Document() {
            @Override
            public @NonNull CommentData comments() {
                return CommentData.empty();
            }

            @Override
            public @NonNull DataTree data() {
                return data;
            }
        });
    }

    private final class ReadToml {

        private final ArrayDeque<String> keyPathStack = new ArrayDeque<>();
        private final ErrorContext.Source errorSource;

        private ErrorContext error;

        private ReadToml(ErrorContext.Source errorSource) {
            this.errorSource = errorSource;
        }

        private void readTomlTree(TomlTable tomlTable, DataTree.Mut dataTree) {
            for (TomlKey tomlKey : tomlTable.keys(false)) {
                Iterator<String> keyIter = tomlKey.iterator();
                String key = keyIter.next();
                if (keyIter.hasNext()) throw new IllegalStateException("TomlTable#keys should each have length of 1");

                TomlValue tomlValue = tomlTable.get(tomlKey);
                assert tomlValue != null;
                keyPathStack.addLast(key);
                try {
                    DataEntry dataEntry = entryFromToml(tomlValue);
                    if (dataEntry == null) {
                        return;
                    }
                    dataTree.put(key, dataEntry);
                } finally {
                    keyPathStack.pollLast();
                }
            }
        }

        private void readTomlList(TomlArray tomlArray, DataList.Mut dataList) {
            keyPathStack.addLast("$");
            try {
                for (TomlValue tomlElem : tomlArray) {
                    DataEntry dataEntry = entryFromToml(tomlElem);
                    if (dataEntry == null) {
                        return;
                    }
                    dataList.add(dataEntry);
                }
            } finally {
                keyPathStack.pollLast();
            }
        }

        /// Returns null on error
        private @Nullable DataEntry entryFromToml(TomlValue tomlValue) {
            Object value;
            if (tomlValue.isTable()) {
                DataTree.Mut dataTree = new DataTree.Mut();
                readTomlTree(tomlValue.asTable(), dataTree);
                value = dataTree;

            } else if (tomlValue.isArray()) {
                TomlArray tomlArray = tomlValue.asArray();
                DataList.Mut dataList = new DataList.Mut(tomlArray.size());
                readTomlList(tomlArray, dataList);
                value = dataList;

            } else {
                value = tomlValue.asPrimitive().value();
                if (!DataEntry.validateValue(value)) {
                    LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
                    error = errorSource.buildError(preBuilt(libraryLang.tomlDateType()));
                    error.addDetail(ErrorContext.ENTRY_PATH, new KeyPath.Immut(keyPathStack.toArray(new String[0])));
                }
            }
            if (error != null) {
                return null;
            }
            DataEntry entry = new DataEntry(value);
            if (commentMode == TomlCommentMode.ROUND_TRIP_ABOVE_ONLY) {
                List<Comment> tomlComments = tomlValue.comments().get(CommentPosition.PRE);
                List<String> comments = new ArrayList<>(tomlComments.size());
                for (Comment tomlComment : tomlComments) {
                    comments.add(tomlComment.content());
                }
                entry = entry.withComments(CommentLocation.ABOVE, comments);
            }
            return entry;
        }
    }

    @Override
    public void write(@NonNull Document document) {
        TomlTable tomlTable = dataTreeToToml(document.data());
        if (commentMode != TomlCommentMode.ROUND_TRIP_ABOVE_ONLY) {
            setComments(tomlTable, document.comments());
        }
        try {
            dataRoot.openWriter(writer -> {
                jToml.write(writer, tomlTable);
                return null;
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private TomlTable dataTreeToToml(DataTree dataTree) {
        TomlTable tomlTable = TomlTable.create();
        dataTree.forEach((key, entry) -> {
            tomlTable.put(TomlKey.literal(key.toString()), entryToToml(entry));
        });
        return tomlTable;
    }

    private TomlArray dataListToToml(DataList dataList) {
        TomlArray tomlArray = TomlArray.create(dataList.size());
        dataList.forEach(entry -> tomlArray.add(entryToToml(entry)));
        return tomlArray;
    }

    private TomlValue entryToToml(DataEntry entry) {
        Object value = entry.getValue();
        TomlValue tomlValue;
        if (value instanceof DataTree) {
            tomlValue = dataTreeToToml((DataTree) value);
        }  else if (value instanceof DataList) {
            tomlValue = dataListToToml((DataList) value);
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            tomlValue = TomlPrimitive.of(((Number) value).intValue());
        } else if (value instanceof Long) {
            tomlValue = TomlPrimitive.of((Long) value);
        } else if (value instanceof Float) {
            tomlValue = TomlPrimitive.of((Float) value);
        } else if (value instanceof Double) {
            tomlValue = TomlPrimitive.of((Double) value);
        } else if (value instanceof Boolean) {
            tomlValue = TomlPrimitive.of((Boolean) value);
        } else if (value instanceof Character) {
            tomlValue = TomlPrimitive.of(((Character) value).toString());
        } else if (value instanceof String) {
            tomlValue = TomlPrimitive.of((String) value);
        } else {
            throw new IllegalStateException("Unknown type in data entry " + value);
        }
        setComments(tomlValue, entry.getComments());
        return tomlValue;
    }

    private void setComments(TomlValue tomlValue, CommentData commentData) {
        Comments tomlComments = tomlValue.comments();
        for (CommentPosition tomlPosition : CommentPosition.values()) {
            CommentLocation location = commentLocationFrom(tomlPosition);
            List<String> commentsHere = commentData.getAt(location);
            if (location == CommentLocation.INLINE && commentsHere.size() > 1) {
                throw new DeveloperMistakeException("Can add at most one inline comment on a toml value");
            }
            for (String commentHere : commentsHere) {
                tomlComments.add(tomlPosition, commentHere);
            }
        }
    }

    private static CommentLocation commentLocationFrom(CommentPosition position) {
        switch (position) {
            case PRE: return CommentLocation.ABOVE;
            case INLINE: return CommentLocation.INLINE;
            case POST: return CommentLocation.BELOW;
            default: throw new IncompatibleClassChangeError(
                    "Unknown" + ' ' +  CommentPosition.class.getSimpleName()+ ' ' + position);
        }
    }

    @Override
    public @NonNull KeyMapper recommendKeyMapper() {
        return new KebabCaseKeyMapper();
    }

    @Override
    public @NonNull Meta meta() {
        return new Meta() {
            @Override
            public boolean supportsComments(boolean documentLevel, boolean reading, @NonNull CommentLocation location) {
                switch (commentMode) {
                    case WRITE_ALWAYS:
                        return !reading;
                    case ROUND_TRIP_ABOVE_ONLY:
                        return !documentLevel && location == CommentLocation.ABOVE;
                    default:
                        throw new IncompatibleClassChangeError("Unknown comment mode " + commentMode);
                }
            }

            @Override
            public boolean preservesOrder(boolean reading) {
                return false;
            }

            @Override
            public boolean writesFloatAsDouble() {
                return true;
            }

            @Override
            public boolean allKeysAreStrings() {
                return true;
            }
        };
    }
}
