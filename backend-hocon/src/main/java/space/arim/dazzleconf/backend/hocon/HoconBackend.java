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

package space.arim.dazzleconf.backend.hocon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigResolveOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.impl.WriteHocon_Access;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf.ErrorContext;
import space.arim.dazzleconf.LoadResult;
import space.arim.dazzleconf.backend.Backend;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.DataEntry;
import space.arim.dazzleconf.backend.DataList;
import space.arim.dazzleconf.backend.DataTree;
import space.arim.dazzleconf.backend.KeyMapper;
import space.arim.dazzleconf.backend.ReadableRoot;
import space.arim.dazzleconf.backend.KebabCaseKeyMapper;
import space.arim.dazzleconf.engine.CommentLocation;
import space.arim.dazzleconf.internals.lang.LibraryLang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static space.arim.dazzleconf.backend.Printable.preBuilt;

/**
 * A backend for HOCON.
 * <p>
 * This backend follows the <a href="https://github.com/lightbend/config/blob/main/HOCON.md">HOCON specification.</a>
 * Thus, standard HOCON features are supported, like "include" declarations, substitutions, and environment variables.
 * If needed, some of these features can be disabled using the {@link Builder}.
 * <p>
 * <b>Comments</b>
 * <p>
 * The HOCON library is able to read and write comments. It supports only {@link CommentLocation#ABOVE}, comments above
 * the entry or in the document header.
 * <p>
 * However, there is a caveat: reading back a document with comments cannot distinguish the document-level header from
 * comments on the first entry. Therefore, comments cannot be round-tripped if a document header exists (the header
 * would be copied onto the first entry, again and again, with every read/write cycle).
 * <p>
 * To tackle this problem, users of this backend have a choice by setting the {@link HoconCommentMode}. Either ignore
 * the document header entirely, which enables reading and writing of all entry comments. Or disable reading comments,
 * which will overwrite any user edits every time the configuration is written. The default is to disable reading
 * comments with {@link HoconCommentMode#WRITE_ALWAYS}.
 * <p>
 * <b>Floats</b>
 * <p>
 * This backend does not support floats as a first-class type, so it converts them to double instead. Unfortunately,
 * the HOCON reference implementation, i.e. the lightbend/config library, follows this pattern and forces it on us.
 * Their maintainers have <a href="https://github.com/lightbend/config/pull/776">explicitly rejected</a> float support
 * on the pretext that their library is "feature complete."
 * <p>
 * <b>Null values</b>
 * <p>
 * Following the recommended practice of {@link Backend}, null values are substituted with the literal string "null".
 */
public final class HoconBackend implements Backend {

    private final ReadableRoot dataRoot;
    private final URL syntaxLinter;
    private final HoconCommentMode commentMode;

    private final ConfigParseOptions configParseOptions;
    private final ConfigResolveOptions configResolveOptions;
    private final ConfigRenderOptions configRenderOptions;

    private HoconBackend(ReadableRoot dataRoot, Builder builder) {
        this.dataRoot = dataRoot;
        this.syntaxLinter = syntaxLinterOrDefault(builder);
        this.commentMode = builder.commentMode;

        configParseOptions = ConfigParseOptions.defaults();
        configResolveOptions = ConfigResolveOptions.defaults()
                .setUseSystemEnvironment(builder.useEnvironment);
        configRenderOptions = ConfigRenderOptions.defaults()
                .setFormatted(true)
                .setOriginComments(false)
                .setComments(true)
                .setJson(false);
    }

    /**
     * Creates from a readable data root. For example, to load from a file:
     * <pre>
     *     {@code
     *         Backend hoconBackend = new HoconBackend(new PathRoot(Path.of("config.conf")));
     *         Configuration<MyConfig> configuration = Configuration.defaultBuilder(MyConfig.class).build();
     *         LoadResult<MyConfig> loaded = configuration.configureWith(hoconBackend);
     *     }
     * </pre>
     *
     * @param dataRoot the data root from which to read and write
     */
    public HoconBackend(@NonNull ReadableRoot dataRoot) {
        this(dataRoot, new Builder());
    }

    private static URL syntaxLinterOrDefault(Builder builder) {
        URL syntaxLinter = builder.syntaxLinter;
        return syntaxLinter == null ? defaultSyntaxLinter() : syntaxLinter;
    }

    private static URL defaultSyntaxLinter() {
        try {
            // Alternatives:
            // https://hocon-playground.avelier.dev/ and https://hocon-playground.tehbrian.dev
            return new URI("https", "toolbox.helpch.at", "/validators/hocon", null).toURL();
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * Builder for a HOCON backend. Allows setting additional options.
     *
     */
    public static final class Builder {

        private boolean useEnvironment = true;
        private URL syntaxLinter;
        private HoconCommentMode commentMode = HoconCommentMode.WRITE_ALWAYS;

        /**
         * Creates the builder
         */
        public Builder() {}

        /**
         * Whether to resolve environment variables that can affect the configuration.
         * <p>
         * By default, environment variables are enabled (true).
         *
         * @param useEnvironment true to use environment variables
         * @return this builder
         */
        public @NonNull Builder useEnvironment(boolean useEnvironment) {
            this.useEnvironment = useEnvironment;
            return this;
        }

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
         * See the variants of the enum for more information. Defaults to {@link HoconCommentMode#WRITE_ALWAYS}
         *
         * @param commentMode the comment mode
         * @return this builder
         */
        public @NonNull Builder commentMode(@NonNull HoconCommentMode commentMode) {
            this.commentMode = Objects.requireNonNull(commentMode, "commentMode");
            return this;
        }

        /**
         * Builds into a backend. This can be called as many times as needed.
         *
         * @param dataRoot the data root from which to read and write
         * @return the backend
         */
        public @NonNull HoconBackend build(@NonNull ReadableRoot dataRoot) {
            return new HoconBackend(dataRoot, this);
        }
    }

    @Override
    public @NonNull LoadResult<@Nullable Document> read(ErrorContext.@NonNull Source errorSource) {
        Config loaded;
        try {
            if (!dataRoot.dataExists()) {
                return LoadResult.of(null);
            }
            loaded = dataRoot.openReader(reader -> ConfigFactory.parseReader(reader, configParseOptions));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (ConfigException.IO ioEx) {
            Throwable cause = ioEx.getCause();
            if (cause instanceof IOException) {
                throw new UncheckedIOException((IOException) cause);
            }
            throw new UncheckedIOException(new IOException(ioEx));
        } catch (ConfigException.Parse parseEx) {
            LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
            ErrorContext error = errorSource.buildError(preBuilt(libraryLang.syntax()));
            error.addDetail(ErrorContext.BACKEND_MESSAGE, preBuilt(parseEx.getMessage()));
            error.addDetail(ErrorContext.SYNTAX_LINTER, syntaxLinter);
            return LoadResult.failure(error);
        } catch (ConfigException otherEx) {
            LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
            ErrorContext error = errorSource.buildError(preBuilt(libraryLang.otherReason()));
            error.addDetail(ErrorContext.BACKEND_MESSAGE, preBuilt(otherEx.getMessage()));
            return LoadResult.failure(error);
        }
        ConfigObject root = loaded.resolve(configResolveOptions).root();
        return LoadResult.of(Document.simple(dataTreeFromHocon(root)));
    }

    private DataTree dataTreeFromHocon(ConfigObject hoconObject) {
        DataTree.Mut dataTree = new DataTree.Mut();
        // HOCON chose to implement entrySet() horribly: they re-make the whole object just to avoid an unchecked cast
        // So, remember not to use their entrySet() method
        hoconObject.forEach((key, hoconValue) -> dataTree.put(key, entryFromHocon(hoconValue)));
        return dataTree;
    }

    private DataEntry entryFromHocon(ConfigValue hoconValue) {
        Object value;
        if (hoconValue instanceof ConfigObject) {
            value = dataTreeFromHocon((ConfigObject) hoconValue);
        } else if (hoconValue instanceof ConfigList) {
            ConfigList hoconList = (ConfigList) hoconValue;
            DataList.Mut dataList = new DataList.Mut(hoconList.size());
            for (ConfigValue hoconElem : hoconList) {
                dataList.add(entryFromHocon(hoconElem));
            }
            value = dataList;
        } else {
            Object unwrappedScalar = hoconValue.unwrapped();
            value = unwrappedScalar == null ? "null" : unwrappedScalar;
        }
        ConfigOrigin origin = hoconValue.origin();
        DataEntry entry = new DataEntry(value);
        if (commentMode == HoconCommentMode.ROUND_TRIP_OMIT_HEADER) {
            entry = entry.withComments(commentsFromHocon(origin.comments()));
        }
        // Add line number if set (unset is -1 in lightbend API)
        int hoconLineNumber = origin.lineNumber();
        return hoconLineNumber == -1 ? entry : entry.withLineNumber(hoconLineNumber);
    }

    private CommentData commentsFromHocon(List<String> hoconComments) {
        if (hoconComments.isEmpty()) {
            return CommentData.empty();
        }
        List<String> comments = new ArrayList<>(hoconComments.size());
        for (String hoconComment : hoconComments) {
            comments.add(hoconComment.startsWith(" ") ? hoconComment.substring(1) : hoconComment);
        }
        return CommentData.empty().setAt(CommentLocation.ABOVE, comments);
    }

    @Override
    public void write(@NonNull Document document) {
        WriteHocon writeHocon = new WriteHocon(
                new WriteHocon_Access(HoconBackend.class.getName())
        );
        ConfigObject hoconConfig = writeHocon.dataTreeToHocon(document.data());
        if (commentMode != HoconCommentMode.ROUND_TRIP_OMIT_HEADER) {
            List<String> header = document.comments().getAt(CommentLocation.ABOVE);
            hoconConfig = hoconConfig.withOrigin(hoconConfig.origin().withComments(header));
        }
        String rendered = hoconConfig.render(configRenderOptions);
        try {
            dataRoot.openWriter(writer -> {
                writer.write(rendered);
                return null;
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static final class WriteHocon {

        private final WriteHocon_Access writeHoconAccess;

        private WriteHocon(WriteHocon_Access writeHoconAccess) {
            this.writeHoconAccess = writeHoconAccess;
        }

        private ConfigObject dataTreeToHocon(DataTree dataTree) {
            LinkedHashMap<String, ConfigValue> hoconConfigMap = new LinkedHashMap<>(dataTree.size() + 1, 0.999f);
            dataTree.forEach((key, entry) -> hoconConfigMap.put(key.toString(), entryToHocon(entry)));
            return writeHoconAccess.fromMap(hoconConfigMap);
        }

        private ConfigList dataListToHocon(DataList dataList) {
            List<ConfigValue> hoconList = new ArrayList<>(dataList.size());
            dataList.forEach(entry -> hoconList.add(entryToHocon(entry)));
            return writeHoconAccess.fromList(hoconList);
        }

        private ConfigValue entryToHocon(DataEntry entry) {
            Object value = entry.getValue();
            ConfigValue hoconValue;
            if (value instanceof DataTree) {
                hoconValue = dataTreeToHocon((DataTree) value);
            }  else if (value instanceof DataList) {
                hoconValue = dataListToHocon((DataList) value);
            } else {
                hoconValue = writeHoconAccess.fromScalar(value);
            }
            List<String> comments = entry.getComments(CommentLocation.ABOVE);
            hoconValue = hoconValue.withOrigin(hoconValue.origin().withComments(comments));
            return hoconValue;
        }
    }

    @Override
    public @NonNull KeyMapper recommendKeyMapper() {
        return new KebabCaseKeyMapper();
    }

    @Override
    public @NonNull Meta meta() {
        HoconCommentMode commentMode = this.commentMode;
        return new Meta() {

            @Override
            public boolean supportsComments(boolean documentLevel, boolean reading, @NonNull CommentLocation location) {
                switch (commentMode) {
                    case WRITE_ALWAYS:
                        return !reading && location == CommentLocation.ABOVE;
                    case ROUND_TRIP_OMIT_HEADER:
                        return !documentLevel && location == CommentLocation.ABOVE;
                    default:
                        throw new IncompatibleClassChangeError("Unknown comment mode: " + commentMode);
                }
            }

            @Override
            public boolean preservesOrder(boolean reading) {
                return !reading;
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
