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

import com.sshtools.jini.Data;
import com.sshtools.jini.INI;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataList;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.KeyPath;
import space.arim.dazzleconf2.internals.lang.LibraryLang;

import java.util.Arrays;
import java.util.function.Supplier;

import static space.arim.dazzleconf2.backend.Printable.preBuilt;

final class ReadIni {

    private final ErrorContext.Source errorSource;

    ReadIni(ErrorContext.Source errorSource) {
        this.errorSource = errorSource;
    }

    ErrorContext read(INI ini, DataTree.Mut dataTree) {
        try {
            readValuesIn(null, ini, dataTree);
            readSectionsIn(ini, dataTree);
            assert thrownError == null;

        } catch (IllegalStateException checkForError) {
            if (checkForError != THROW_SIGNAL_ERROR) {
                throw checkForError;
            }
        }
        return thrownError;
    }

    // Error handling
    private ErrorContext thrownError;
    private static final IllegalStateException THROW_SIGNAL_ERROR = new IllegalStateException();

    private IllegalStateException throwError(ErrorContext error) {
        thrownError = error;
        throw THROW_SIGNAL_ERROR;
    }

    private IllegalStateException iniLibraryContract(String yieldedWhat) {
        throw new IllegalStateException("INI library yielded " + yieldedWhat);
    }

    abstract class LocationContext {

        abstract KeyPath produceKeyPath();

        /// Yields null and sets error if not a list
        DataList.Mut getOrMakeList(ContainerSlot where) {
            return getOrMakeContainer(where, DataList.Mut.class, DataList.Mut::new);
        }

        /// Yields null and sets error if not a tree
        DataTree.Mut getOrMakeTree(ContainerSlot where) {
            return getOrMakeContainer(where, DataTree.Mut.class, DataTree.Mut::new);
        }

        private <D> D getOrMakeContainer(ContainerSlot where, Class<D> type, Supplier<D> instantiator) {
            DataEntry ifPresent = where.getIfPresent();
            if (ifPresent == null) {
                D newContainer = instantiator.get();
                where.set(new DataEntry(newContainer));
                return newContainer;
            }
            Object existingValue = ifPresent.getValue();
            if (type.isInstance(existingValue)) {
                return type.cast(existingValue);
            }
            LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
            ErrorContext error = errorSource.buildError(preBuilt(libraryLang.failed()));
            error.addDetail(ErrorContext.BACKEND_MESSAGE, libraryLang.wrongTypeForValue(existingValue, type));
            error.addDetail(ErrorContext.ENTRY_PATH, produceKeyPath());
            throw throwError(error);
        }
    }

    final class SectionContext extends LocationContext {

        private final String[] sectionName;

        SectionContext(String[] sectionName) {
            this.sectionName = sectionName;
        }

        @Override
        KeyPath produceKeyPath() {
            return new KeyPath.Mut(sectionName);
        }
    }

    final class KeyContext extends LocationContext {

        private final String @Nullable [] sectionName;
        private final String iniKey;

        KeyContext(String @Nullable [] sectionName, String iniKey) {
            this.sectionName = sectionName;
            this.iniKey = iniKey;
        }

        private void insertValue(DataTree.Mut dataOutput, DataEntry value) {
            int firstDotIdx = iniKey.indexOf('.');
            if (firstDotIdx == -1) {
                // Scalar
                dataOutput.put(iniKey, value);
                return;
            }
            // SEE JAVADOC
            // Here we parse our own special syntax, for lists and nested lists
            // E.g. mylist.3.0.1 requires us to use a triple-nested list
            String baseKey = iniKey.substring(0, firstDotIdx);
            String[] strIndices = iniKey.substring(firstDotIdx + 1).split("\\.");
            int[] indices = new int[strIndices.length];
            for (int n = 0; n < strIndices.length; n++) {
                // Parse the index at this step
                String indexStr = strIndices[n];
                int index;
                try {
                    index = Integer.parseInt(indexStr);
                } catch (NumberFormatException ignored) {
                    LibraryLang libraryLang = LibraryLang.Accessor.access(errorSource, ErrorContext.Source::getLocale);
                    ErrorContext error = errorSource.buildError(preBuilt(libraryLang.failed()));
                    error.addDetail(
                            ErrorContext.BACKEND_MESSAGE,
                            preBuilt("Invalid key; expected number for list index, but got: " + indexStr)
                    );
                    throw throwError(error);
                }
                indices[n] = index;
            }
            DataList.Mut currentList = getOrMakeList(new TreeSlot(dataOutput, baseKey));
            for (int n = 0; n < indices.length; n++) {
                ListSlot listSlot = new ListSlot(currentList, indices[n]);
                if (n == indices.length - 1) {
                    // Success! Made it to the end
                    listSlot.set(value);
                    return;
                }
                currentList = getOrMakeList(listSlot);
                if (currentList == null) {
                    return; // Error
                }
            }
        }

        @Override
        KeyPath produceKeyPath() {
            KeyPath.Mut keyPath = sectionName == null ? new KeyPath.Mut() : new KeyPath.Mut(sectionName);
            keyPath.addBack(iniKey);
            return keyPath;
        }
    }

    private void readSectionsIn(INI ini, DataTree.Mut dataTree) {
        ini.sections().forEach((sectionName, sectionArray) -> {
            switch (sectionArray.length) {
                case 0:
                    throw iniLibraryContract("an empty section array");
                case 1:
                    throw iniLibraryContract("a multi-valued section array");
                default:
                    break;
            }
            readSection(sectionArray[0], sectionName.split("\\."), dataTree);
        });
    }

    private void readSection(INI.Section section, String[] sectionName, DataTree.Mut topLevelTree) {
        // Parse the index at each step, if applicable
        int[] sectionNameIndices = new int[sectionName.length];
        Arrays.fill(sectionNameIndices, -1);

        for (int n = 0; n < sectionName.length; n++) {
            String sectionNamePart = sectionName[n];
            if (sectionNamePart.isEmpty() || sectionNamePart.charAt(0) == '-') {
                // Someone could be trying to hack in negative indices
                continue;
            }
            try {
                sectionNameIndices[n] = Integer.parseInt(sectionNamePart);
            } catch (NumberFormatException ignored) {
            }
        }
        SectionContext sectionContext = new SectionContext(sectionName);
        ContainerSlot lastContainer = new TreeSlot(topLevelTree, sectionName[0]);
        for (int n = 1; n < sectionName.length; n++) {
            ContainerSlot newContainer;
            if (sectionNameIndices[n] == -1) {
                DataTree.Mut asTree = sectionContext.getOrMakeTree(lastContainer);
                newContainer = new TreeSlot(asTree, sectionName[n]);
            } else {
                DataList.Mut asList = sectionContext.getOrMakeList(lastContainer);
                newContainer = new ListSlot(asList, sectionNameIndices[n]);
            }
            lastContainer = newContainer;
        }
        readValuesIn(sectionName, section, sectionContext.getOrMakeTree(lastContainer));
    }

    private void readValuesIn(String @Nullable [] sectionName, Data dataInput, DataTree.Mut dataOutput) {
        dataInput.rawValues().forEach((iniKey, valueArray) -> {
            if (valueArray.length == 0) {
                // Empty user value: assume empty string
                dataOutput.put(iniKey, new DataEntry(""));
                return;
            }
            if (valueArray.length != 1) {
                throw iniLibraryContract("a multi-valued value array");
            }
            KeyContext keyContext = new KeyContext(sectionName, iniKey);
            keyContext.insertValue(dataOutput, new DataEntry(valueArray[0]));
        });
    }
}
