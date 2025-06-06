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

package space.arim.dazzleconf.backend.yaml;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.snakeyaml.engine.v2.schema.CoreSchema;
import org.snakeyaml.engine.v2.schema.Schema;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.ReadableRoot;
import space.arim.dazzleconf2.backend.SnakeCaseKeyMapper;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static space.arim.dazzleconf2.backend.Printable.preBuilt;

/**
 * A backend for YAML.
 * <p>
 * The implementation uses a shaded copy of SnakeYAML Engine, which is not exposed, with 'block' flow style enabled.
 * <p>
 * <b>Environment variables</b>
 * <p>
 * This backend supports environment variable substitution as described on the
 * <a href="https://bitbucket.org/snakeyaml/snakeyaml-engine/wiki/Documentation">SnakeYAML-Engine wiki</a>. For
 * example, a string value can be set to <code>${DEBUG}</code> (provided it is unquoted) and the "DEBUG" environment
 * variable will be substituted at load time.
 * <p>
 * <b>Null values</b>
 * <p>
 * Following the recommended practice of {@link Backend}, null values are substituted with the literal string "null".
 * <p>
 * <b>Comment data</b>
 * <p>
 * Comments are fully supported, provided they are added through library mechanisms. Note that some comments in YAML
 * files, if not added by this backend (e.g., added by the user), might not be easily recognized as belonging to a
 * certain entry. This backend uses line breaks and indentation to sleuth out where comments belong, and comments
 * are discarded where not supported by the library model (e.g., inline comments on keys instead of values).
 * <p>
 * Because the backend writes the same line breaks and comments in the locations it expects, it can guarantee round
 * trips for its own comment data. A good way to comply with this backend's expectations is to add an extra line after
 * comments below an entry, to differentiate them from comments above, e.g.:
 * <pre>
 *     {@code
 *         # A header for the document always has a blank line separating it from the first entry
 *
 *         # Comment on 'first-entry'
 *         first-entry: "hi"
 *         # Comment on 'option'
 *         option: "some comments here"
 *         # Another comment on 'option'
 *         # Notice the empty space below
 *
 *         # Comment on 'rest-here'
 *         rest-here: "hooray!" # Inline comment on 'rest-here'
 *
 *         # Finally, a footer
 *     }
 * </pre>
 *
 */
public final class YamlBackend implements Backend {

    private final ReadableRoot dataRoot;
    private final URL syntaxLinter;

    private final LoadSettings loadSettings;
    private final DumpSettings dumpSettings;

    private static final int INDENT_PER_LEVEL = 2;
    /*
     A deprecated, unrecommended option (but kept as a fallback -- will likely be removed in 2.0 full release)
     Switching on this flag with -Dspace.arim.dazzleconf.backend.yaml.YamlBackend.useEngineComposer is not recommended,
     and as comment reading will no longer be exact, round-trip comment preservation will be disabled via Backend.Meta
     */
    private static final boolean USE_ENGINE_COMPOSER = Boolean.getBoolean(YamlBackend.class.getName() + ".useEngineComposer");

    /**
     * Creates from a readable data root, and a URL pointing to a syntax linter.
     * <p>
     * The URL, when supplied by this constructor, may be provided to users in the form of error messages. It should
     * point to a live website where end users can paste and validate their configuration file's syntax.
     *
     * @param dataRoot the data root from which to read and write
     * @param syntaxLinter a link to an online syntax linter
     */
    @API(status = API.Status.EXPERIMENTAL)
    public YamlBackend(@NonNull ReadableRoot dataRoot, @NonNull URL syntaxLinter) {
        this.dataRoot = Objects.requireNonNull(dataRoot, "dataRoot");
        this.syntaxLinter = Objects.requireNonNull(syntaxLinter, "syntaxLinter");

        Schema schema = new CoreSchema();
        loadSettings = LoadSettings.builder()
                .setParseComments(true)
                .setSchema(schema)
                .setAllowDuplicateKeys(false)
                .setAllowRecursiveKeys(false)
                .setLabel(YamlBackend.class.getSimpleName())
                .build();
        dumpSettings = DumpSettings.builder()
                .setNonPrintableStyle(NonPrintableStyle.ESCAPE)
                .setDumpComments(true)
                .setSchema(schema)
                .setDefaultFlowStyle(FlowStyle.BLOCK)
                .setIndent(INDENT_PER_LEVEL)
                .build();
    }

    /**
     * Creates from a readable data root. For example, to load from a file:
     * <pre>
     *     {@code
     *         Backend yamlBackend = new YamlBackend(new PathRoot(Path.of("config.yml")));
     *         Configuration<MyConfig> configuration = Configuration.defaultBuilder(MyConfig.class).build();
     *         LoadResult<MyConfig> loaded = configuration.configureWith(yamlBackend);
     *     }
     * </pre>
     *
     * @param dataRoot the data root from which to read and write
     */
    public YamlBackend(@NonNull ReadableRoot dataRoot) {
        this(dataRoot, defaultSyntaxLinter());
    }

    private static URL defaultSyntaxLinter() {
        try {
            return new URI("https", "yaml-online-parser.appspot.com", "/", null).toURL();
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new AssertionError(ex);
        }
    }

    static DeveloperMistakeException doesNotSupport(String feature) {
        throw new DeveloperMistakeException(YamlBackend.class.getSimpleName() + " does not support this capability: " + feature);
    }

    @Override
    public @NonNull LoadResult<@Nullable Document> read(ErrorContext.@NonNull Source errorSource) {
        try {
            if (!dataRoot.dataExists()) {
                return LoadResult.of(null);
            }
            return dataRoot.openReader(reader -> {
                LoadResult<@Nullable ReadYamlProduct> readResult;
                try {
                    if (USE_ENGINE_COMPOSER) {
                        readResult = readWithEngineComposer(errorSource, reader);
                    } else {
                        Parser parser = new ParserImpl(loadSettings, new StreamReader(loadSettings, reader));
                        readResult = new ReadEvents(
                                new ReadYaml(errorSource), loadSettings, new StandardConstructor(loadSettings), parser
                        ).read();
                    }
                } catch (YamlEngineException yamlEx) {
                    Throwable cause = yamlEx.getCause();
                    if (cause instanceof IOException) {
                        throw (IOException) cause;
                    }
                    throw yamlEx;
                }
                // Finish!
                return readResult.map(product -> {
                    if (product != null && product.dataTree.isEmpty() && product.headerFooter.isEmpty()) {
                        // Totally empty document... let's not take this seriously
                        return null;
                    }
                    return product;
                });
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (MarkedYamlEngineException parseEx) {
            LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
            ErrorContext error = errorSource.buildError(preBuilt(libraryLang.syntax()));
            error.addDetail(ErrorContext.BACKEND_MESSAGE, preBuilt(parseEx.getMessage()));
            error.addDetail(ErrorContext.SYNTAX_LINTER, syntaxLinter);
            parseEx.getProblemMark().map(Mark::getLine).ifPresent(lineNumber -> {
                error.addDetail(ErrorContext.LINE_NUMBER, lineNumber);
            });
            return LoadResult.failure(error);
        } catch (YamlEngineException otherEx) {
            LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
            ErrorContext error = errorSource.buildError(preBuilt(libraryLang.otherReason()));
            error.addDetail(ErrorContext.BACKEND_MESSAGE, preBuilt(otherEx.getMessage()));
            return LoadResult.failure(error);
        }
    }

    private @NonNull LoadResult<@Nullable ReadYamlProduct> readWithEngineComposer(
            ErrorContext.Source errorSource, Reader reader
    ) throws IOException {
        Compose compose = new Compose(loadSettings);
        Node node = compose.composeReader(reader).orElse(null);
        if (node == null) {
            return LoadResult.of(null);
        }
        MappingNode mappingNode;
        if (node instanceof MappingNode) {
            mappingNode = (MappingNode) node;
        } else if (node.getTag() == Tag.COMMENT) {
            // This can happen if there was an empty document
            // In that case, we can artificially construct a MappingNode and use it anyway
            MappingNode construct = new MappingNode(Tag.MAP, Collections.emptyList(), FlowStyle.AUTO);
            construct.setBlockComments(node.getBlockComments());
            construct.setEndComments(node.getEndComments());
            mappingNode = construct;
        } else {
            LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
            return errorSource.throwError(libraryLang.yamlNotAMap());
        }
        return new ReadNodes(
                new ReadYaml(errorSource), new StandardConstructor(loadSettings)
        ).runForMapping(mappingNode);
    }

    @Override
    public void write(@NonNull Document document) {
        List<String> header, footer;
        {
            CommentData headerFooter = document.comments();
            header = headerFooter.getAt(CommentLocation.ABOVE);
            footer = headerFooter.getAt(CommentLocation.BELOW);
        }
        Node node;
        Dump dump;
        {
            StandardRepresenter representer = new StandardRepresenter(dumpSettings);
            node = new WriteYaml(representer).dataTreeToNode(document.data());
            dump = new Dump(dumpSettings, representer);
        }
        try {
            dataRoot.openWriter(writer -> {
                if (!header.isEmpty()) {
                    writeComments(writer, header);
                    writer.write(System.lineSeparator());
                }
                dump.dumpNode(node, new StreamDataWriter() {
                    @Override
                    public void write(String str) {
                        try {
                            writer.write(str);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    }

                    @Override
                    public void write(String str, int off, int len) {
                        try {
                            writer.write(str, off, len);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    }
                });
                if (!footer.isEmpty()) {
                    writer.write(System.lineSeparator());
                    writeComments(writer, footer);
                }
                return null;
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void writeComments(Writer writer, List<String> lines) throws IOException {
        for (String line : lines) {
            writer.write("# ");
            writer.write(line);
            writer.write(System.lineSeparator());
        }
    }

    @Override
    public @NonNull KeyMapper recommendKeyMapper() {
        return new SnakeCaseKeyMapper();
    }

    @Override
    public @NonNull Meta meta() {
        return new Meta() {
            @Override
            public boolean supportsComments(boolean documentLevel, boolean reading, @NonNull CommentLocation location) {
                return
                        // Document-level INLINE is not supported - what would that even mean?
                        !(documentLevel && location == CommentLocation.INLINE)
                        // If USE_ENGINE_COMPOSER is turned on, reading is not supported
                        && (!USE_ENGINE_COMPOSER || !reading);
            }

            @Override
            public boolean preservesOrder(boolean reading) {
                return true;
            }

            @Override
            public boolean writesFloatAsDouble() {
                return true;
            }

            @Override
            public boolean allKeysAreStrings() {
                return false;
            }
        };
    }
}
