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

package space.arim.dazzleconf.backend.ini;

import com.sshtools.jini.INI;
import com.sshtools.jini.INIReader;
import com.sshtools.jini.INIWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DefaultKeyMapper;
import space.arim.dazzleconf2.backend.KeyMapper;
import space.arim.dazzleconf2.backend.ReadableRoot;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.ParseException;
import java.util.Objects;

import static space.arim.dazzleconf2.backend.Printable.preBuilt;

/**
 * A backend for INI.
 * <p>
 * <b>Comments</b>
 * <p>
 * This backend supports writing comments, but not reading them.
 * <p>
 * <b>Trees and Lists</b>
 * <p>
 * Nested data trees are supported by using a period-delimited sequence for section names. No indentation is applied.
 * The resulting INI sections can be freely moved around, and when re-reading the document, the section names are
 * parsed and data is shuffled into the correct location.
 * <p>
 * Because INI does not natively support lists, this backend uses a manual indexing approach. Scalar list elements are
 * written by appending a period to the key followed by the index, and trees are written with the index in the section
 * name.
 * <p>
 * For example, a list with a single element might be encoded as <code>mylist.0 = "element 1"</code>. A list element
 * that is a tree would be written as a section, like <code>[mysection.subkey.0]</code>.
 * <p>
 * <b>Empty values</b>
 * <p>
 * When empty values are written in the INI document, they are loaded as empty strings in this backend implementation.
 */
public final class IniBackend implements Backend {

    private final ReadableRoot dataRoot;

    /**
     * Creates from a readable data root. For example, to load from a file:
     * <pre>
     *     {@code
     *         Backend iniBackend = new IniBackend(new PathRoot(Path.of("config.ini")));
     *         Configuration<MyConfig> configuration = Configuration.defaultBuilder(MyConfig.class).build();
     *         LoadResult<MyConfig> loaded = configuration.configureWith(iniBackend);
     *     }
     * </pre>
     *
     * @param dataRoot the data root from which to read and write
     */
    public IniBackend(@NonNull ReadableRoot dataRoot) {
        this.dataRoot = Objects.requireNonNull(dataRoot);
    }

    @Override
    public @NonNull LoadResult<@Nullable Document> read(ErrorContext.@NonNull Source errorSource) {
        INI ini;
        try {
            if (!dataRoot.dataExists()) {
                return LoadResult.of(null);
            }
            INIReader iniReader = new INIReader.Builder()
                    .withCaseSensitiveKeys(true)
                    .withCaseSensitiveSections(true)
                    .withParseExceptions(true)
                    .withPreserveOrder(true)
                    // SEE BELOW
                    // If these options are changed, the reader code must be updated accordingly
                    .withDuplicateKeysAction(INIReader.DuplicateAction.ABORT)
                    .withDuplicateSectionAction(INIReader.DuplicateAction.ABORT)
                    .build();
            LoadResult<INI> iniLoadResult = dataRoot.openReader(reader -> {
                try {
                    return LoadResult.of(iniReader.read(reader));
                } catch (ParseException parseEx) {
                    LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
                    ErrorContext error = errorSource.buildError(preBuilt(libraryLang.syntax()));
                    error.addDetail(ErrorContext.BACKEND_MESSAGE, preBuilt(parseEx.getMessage()));
                    return LoadResult.failure(error);
                }
            });
            if (iniLoadResult.isFailure()) {
                return LoadResult.failure(iniLoadResult.getErrorContexts());
            }
            ini = iniLoadResult.getOrThrow();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        DataTree.Mut data = new DataTree.Mut();
        ReadIni readIni = new ReadIni(errorSource);
        ErrorContext error = readIni.read(ini, data);
        if (error != null) {
            return LoadResult.failure(error);
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

    @Override
    public void write(@NonNull Document document) {
        INI ini = new WriteIni().write(document);
        try {
            INIWriter iniWriter = new INIWriter.Builder()
                    .withCommentPlacementMode(INIWriter.CommentPlacementMode.ALWAYS_ABOVE)
                    .withEmptyValuesHaveSeparator(true)
                    .build();
            dataRoot.openWriter(writer -> {
                iniWriter.write(ini, writer);
                return null;
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public @NonNull KeyMapper recommendKeyMapper() {
        return new DefaultKeyMapper();
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
                return true;
            }

            @Override
            public boolean writesFloatAsDouble() {
                return true;
            }

            @Override
            public boolean allKeysAreStrings() {
                return true;
            }

            // Later - add this method to main Backend.Meta interface
            /**
             * Whether the backend only supports plain keys. A plain key consists only of the characters A through Z,
             * a through z, or 0 through 9.
             *
             * @return true if the backend is limited to plain keys
             */
            public boolean onlyAlphanumericKeys() {
                return true;
            }

        };
    }
}
