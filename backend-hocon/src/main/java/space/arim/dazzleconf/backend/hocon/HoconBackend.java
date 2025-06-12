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

package space.arim.dazzleconf.backend.hocon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigResolveOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.impl.WriteHocon_Access;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.DeveloperMistakeException;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyMapper;
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
import java.util.LinkedHashMap;
import java.util.List;

import static space.arim.dazzleconf2.backend.Printable.preBuilt;

/**
 * A backend for HOCON.
 * <p>
 * Standard HOCON features are supported. A shaded copy of the lightbend/config is used internally and not exposed.
 * Thus, users are able to use include declarations, substitutions, and environment variables like any HOCON file.
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

    private final ConfigParseOptions configParseOptions;
    private final ConfigResolveOptions configResolveOptions;
    private final ConfigRenderOptions configRenderOptions;

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
    public HoconBackend(@NonNull ReadableRoot dataRoot, @NonNull URL syntaxLinter) {
        this.dataRoot = dataRoot;
        this.syntaxLinter = syntaxLinter;

        configParseOptions = ConfigParseOptions.defaults();
        configResolveOptions = ConfigResolveOptions.defaults();
        configRenderOptions = ConfigRenderOptions.defaults()
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
        this(dataRoot, defaultSyntaxLinter());
    }

    private static URL defaultSyntaxLinter() {
        try {
            return new URI("https", "toolbox.helpch.at", "/validators/hocon", null).toURL();
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new AssertionError(ex);
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
            ErrorContext error = errorSource.buildError(preBuilt(libraryLang.failed()));
            error.addDetail(ErrorContext.BACKEND_MESSAGE, preBuilt(parseEx.getMessage()));
            error.addDetail(ErrorContext.SYNTAX_LINTER, syntaxLinter);
            return LoadResult.failure(error);
        } catch (ConfigException otherEx) {
            ErrorContext error = errorSource.buildError(preBuilt("Unknown error"));
            error.addDetail(ErrorContext.BACKEND_MESSAGE, preBuilt(otherEx.getMessage()));
            return LoadResult.failure(error);
        }
        ConfigObject root = loaded.resolve(configResolveOptions).root();
        return LoadResult.of(Document.simple(dataTreeFromHocon(root)));
    }

    private DataTree dataTreeFromHocon(ConfigObject hoconObject) {
        DataTree.Mut dataTree = new DataTree.Mut();
        // HOCON chose to implement entrySet() horribly: they re-make the whole object just to avoid an unchecked cast
        // So, we use keySet() iteration which is more efficient
        //noinspection KeySetIterationMayUseEntrySet
        for (String key : hoconObject.keySet()) {
            dataTree.set(key, entryFromHocon(hoconObject.get(key)));
        }
        return dataTree;
    }

    private DataEntry entryFromHocon(ConfigValue hoconValue) {
        Object value;
        if (hoconValue instanceof ConfigObject) {
            value = dataTreeFromHocon((ConfigObject) hoconValue);
        } else if (hoconValue instanceof ConfigList) {
            ConfigList hoconList = (ConfigList) hoconValue;
            List<DataEntry> entryList = new ArrayList<>(hoconList.size());
            for (ConfigValue hoconElem : hoconList) {
                entryList.add(entryFromHocon(hoconElem));
            }
            value = entryList;
        } else {
            Object unwrappedScalar = hoconValue.unwrapped();
            value = unwrappedScalar == null ? "null" : unwrappedScalar;
        }
        DataEntry entry = new DataEntry(value);
        // Add line number if set (unset == -1 in lightbend/config API)
        int hoconLineNumber = hoconValue.origin().lineNumber();
        return hoconLineNumber == -1 ? entry : entry.withLineNumber(hoconLineNumber);
    }

    @Override
    public void write(@NonNull Document document) {
        WriteHocon writeHocon = new WriteHocon(
                new WriteHocon_Access(HoconBackend.class.getName())
        );
        ConfigObject hoconConfig = writeHocon.dataTreeToHocon(document.data());
        List<String> header = document.comments().getAt(CommentLocation.ABOVE);
        hoconConfig = hoconConfig.withOrigin(hoconConfig.origin().withComments(header));
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
            LinkedHashMap<String, ConfigValue> hoconConfigMap = new LinkedHashMap<>();
            dataTree.forEach((key, entry) -> {
                hoconConfigMap.put(key.toString(), entryToHocon(entry));
            });
            return writeHoconAccess.fromMap(hoconConfigMap);
        }

        private ConfigList entryListToHocon(List<DataEntry> entryList) {
            List<ConfigValue> hoconList = new ArrayList<>(entryList.size());
            for (DataEntry dataEntry : entryList) {
                hoconList.add(entryToHocon(dataEntry));
            }
            return writeHoconAccess.fromList(hoconList);
        }

        private ConfigValue entryToHocon(DataEntry entry) {
            Object value = entry.getValue();
            ConfigValue hoconValue;
            if (value instanceof DataTree) {
                hoconValue = dataTreeToHocon((DataTree) value);
            }  else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<DataEntry> castValue = (List<DataEntry>) value;
                hoconValue = entryListToHocon(castValue);
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
        return new SnakeCaseKeyMapper();
    }

    @Override
    public @NonNull Meta meta() {
        return new Meta() {

            @Override
            public boolean supportsComments(boolean documentLevel, boolean reading, @NonNull CommentLocation location) {
                return !reading && location == CommentLocation.ABOVE;
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
