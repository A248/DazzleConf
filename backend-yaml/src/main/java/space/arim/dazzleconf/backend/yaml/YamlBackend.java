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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.schema.JsonSchema;
import org.snakeyaml.engine.v2.schema.Schema;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.Printable;
import space.arim.dazzleconf2.backend.ReadableRoot;
import space.arim.dazzleconf2.backend.SnakeCaseKeyMapper;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * A backend for YAML.
 * <p>
 * Comments are fully supported. The implementation uses a shaded copy of SnakeYAML, which is not exposed.
 *
 */
public final class YamlBackend implements Backend {

    private final ReadableRoot dataRoot;
    private final URL syntaxLinter;

    private final LoadSettings loadSettings;
    private final DumpSettings dumpSettings;

    private static final int INDENT_PER_LEVEL = 2;

    /**
     * Creates from a readable data root, and a URL pointing to a syntax linter.
     * <p>
     * The URL, when supplied by this constructor, may be provided to users in the form of error messages. It should
     * point to a live website where end users can paste and validate their configuration file's YAML syntax.
     *
     * @param dataRoot the data root from which to read and write
     * @param syntaxLinter a link to an online syntax linter
     */
    public YamlBackend(@NonNull ReadableRoot dataRoot, @NonNull URL syntaxLinter) {
        this.dataRoot = Objects.requireNonNull(dataRoot, "dataRoot");
        this.syntaxLinter = Objects.requireNonNull(syntaxLinter, "syntaxLinter");

        JsonSchema jsonSchema = new JsonSchema();
        loadSettings = LoadSettings.builder()
                .setParseComments(true)
                .setSchema(jsonSchema)
                .setAllowDuplicateKeys(false)
                .setAllowRecursiveKeys(false)
                .setLabel(YamlBackend.class.getSimpleName())
                .build();
        dumpSettings = DumpSettings.builder()
                .setDumpComments(true)
                .setSchema(jsonSchema)
                .setExplicitRootTag(Optional.of(Tag.MAP))
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
        Node node;
        try {
            if (!dataRoot.dataExists()) {
                return LoadResult.of(null);
            }
            Compose compose = new Compose(loadSettings);
            node = dataRoot.openReader(reader -> {
                try {
                    return compose.composeReader(reader).orElse(null);
                } catch (YamlEngineException yamlEx) {
                    Throwable cause = yamlEx.getCause();
                    if (cause instanceof IOException) {
                        throw (IOException) cause;
                    }
                    throw yamlEx;
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (YamlEngineException yamlEx) {
            LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
            ErrorContext error = errorSource.buildError(Printable.preBuilt(libraryLang.failed()));
            error.addDetail(ErrorContext.BACKEND_MESSAGE, Printable.preBuilt(yamlEx.getMessage()));
            error.addDetail(ErrorContext.SYNTAX_LINTER, syntaxLinter);
            return LoadResult.failure(error);
        }
        if (node == null) {
            return LoadResult.of(null);
        }
        CommentMarshall commentMarshall = new CommentMarshall();
        Tag rootTag = node.getTag();
        if (rootTag == Tag.COMMENT) {
            // Return an empty document, with just comments
            return LoadResult.of(new Document() {
                @Override
                public @NonNull CommentData comments() {
                    Predicate<CommentLine> alwaysTrue = (c) -> true;
                    return CommentData.empty()
                            .setAt(CommentLocation.ABOVE, commentMarshall.getComments1to1(node.getBlockComments(), alwaysTrue))
                            .setAt(CommentLocation.INLINE, commentMarshall.getComments1to1(node.getInLineComments(), alwaysTrue))
                            .setAt(CommentLocation.BELOW, commentMarshall.getComments1to1(node.getEndComments(), alwaysTrue));
                }

                @Override
                public @NonNull DataTree data() {
                    return new DataTree.Immut();
                }
            });
        }
        if (!(node instanceof MappingNode)) {
            LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
            return errorSource.throwError(libraryLang.yamlNotAMap());
        }
        MappingNode mappingNode = (MappingNode) node;
        ReadYaml readYaml = new ReadYaml();
        LoadResult<DataTree> data = readYaml.mappingNodeToTree(mappingNode);
        if (data.isFailure()) {
            return LoadResult.failure(data.getErrorContexts());
        }
        /*
        // Scan for header and footer
        for (NodeTuple nodeTuple : mappingNode.getValue()) {
            System.out.println("Found block comments on key " + nodeTuple.getKeyNode().getBlockComments());
            System.out.println("Found end comments on key " + nodeTuple.getKeyNode().getEndComments());
            System.out.println("Found block comments on value " + nodeTuple.getValueNode().getBlockComments());
            System.out.println("Found end comments on value " + nodeTuple.getValueNode().getEndComments());
        }
         */
        CommentData comments = readYaml.getComments(node, node);
        return LoadResult.of(new Document() {
            @Override
            public @NonNull CommentData comments() {
                return comments;
            }

            @Override
            public @NonNull DataTree data() {
                return data.getOrThrow();
            }
        });
    }

    @Override
    public void write(@NonNull Document document) {
        WriteYaml writeYaml = new WriteYaml();
        Node node = writeYaml.dataTreeToNode(document.data());
        {
            // Inline comments cannot be written at the document level
            // Block and end comments can be written, but we need to distinguish them from any mappings
            CommentData comments = document.comments();
            List<String> header = comments.getAt(CommentLocation.ABOVE);
            List<String> footer = comments.getAt(CommentLocation.BELOW);
            writeYaml.setComments1to1(node, header, Node::setBlockComments);
            writeYaml.setComments1to1(node, footer, Node::setEndComments);
            // Write these border values to separate the header and the footer, so we can recognize them later
            /*if (!header.isEmpty()) {
                node.getBlockComments().add(new CommentLine(null, null, HEADER_FOOTER_DEMARCATOR, CommentType.BLOCK));
                node.getBlockComments().add(blankLine());
            }
            if (!footer.isEmpty()) {
                node.getEndComments().add(blankLine());
                node.getEndComments().add(new CommentLine(null, null, HEADER_FOOTER_DEMARCATOR, CommentType.BLOCK));
            }*/
        }
        try {
            dataRoot.openWriter(writer -> {
                try {
                    writeYaml.yaml.serialize(node, writer);
                } catch (YAMLException yamlEx) {
                    Throwable cause = yamlEx.getCause();
                    if (cause instanceof IOException) {
                        throw (IOException) cause;
                    } else {
                        throw new IOException("Unexpected YAMLException while writing to stream", yamlEx);
                    }
                }
                return null;
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static class ReadYaml {

        
    }
    private static class CommentMarshall {

        private List<String> getComments1to1(List<CommentLine> engineComments, Predicate<CommentLine> filter) {
            if (engineComments == null) {
                return Collections.emptyList();
            }
            List<String> comments = new ArrayList<>(engineComments.size());
            for (CommentLine engineComment : engineComments) {
                if (filter.test(engineComment)) {
                    comments.add(extractComment(engineComment));
                }
            }
            return comments;
        }

        private String extractComment(CommentLine engineComment) {
            String value = engineComment.getValue();
            return value.startsWith(" ") ? value.substring(1) : value;
        }

    }
    @Override
    public boolean supportsComments(@NonNull CommentLocation location) {
        return true;
    }

    @Override
    public @NonNull KeyMapper recommendKeyMapper() {
        return new SnakeCaseKeyMapper();
    }
}
