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

package space.arim.dazzleconf.backend;

import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

record Comparison(Backend.Document subject, Backend.Document reloaded, Backend.Meta backendMeta) {

    private String errorMsg(String msg) {
        String errorMsg = "Wrote data tree \n" + subject.data() + "\n and reloaded it to \n" + reloaded.data() +
                ".\n But failed because of " + msg;
        if (!subject.comments().isEmpty()) {
            errorMsg += ". Note that document comments were added: " + subject.comments();
        }
        return errorMsg;
    }

    private AssertionError error(String msg) {
        throw new AssertionError(errorMsg(msg));
    }

    private AssertionError error(String msg, Error cause) {
        throw new AssertionError(errorMsg(msg), cause);
    }

    void treesEqual(DataTree fromLibrary, DataTree fromBackend) {
        if (fromLibrary.size() != fromBackend.size()) throw error("Wrong size " + fromLibrary + " vs " + fromBackend);

        Map<String, DataEntry> fromLibraryMap = fromLibrary.keySet().stream().map(key -> {
            return Map.entry(key.toString(), fromLibrary.get(key));
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, DataEntry> fromBackendMap = fromBackend.keySet().stream().map(key -> {
            return Map.entry(key.toString(), fromBackend.get(key));
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertEquals(fromLibraryMap.keySet(), fromBackendMap.keySet());

        fromLibraryMap.forEach((key, entry) -> {
            DataEntry corresponding = fromBackendMap.get(key);
            try {
                entriesEqual(entry, corresponding);
            } catch (AssertionError ex) {
                throw error("Failed at key " + key, ex);
            }
        });
    }

    void entriesEqual(DataEntry fromLibrary, DataEntry fromBackend) {
        Object libraryValue = fromLibrary.getValue();
        Object backendValue = fromBackend.getValue();
        if (libraryValue instanceof DataTree libraryTree) {
            if (!(backendValue instanceof DataTree backendTree)) {
                throw error("Expected DataTree, but got " + backendValue);
            }
            treesEqual(libraryTree, backendTree);
            return; // Skip comment check
        } else if (libraryValue instanceof List<?> libraryList) {
            if (!(backendValue instanceof List<?> backendList) || libraryList.size() != backendList.size()) {
                throw error("Not a list " + backendValue + ", expected " + libraryList);
            }
            for (int n = 0; n < libraryList.size(); n++) {
                Object libraryElem = libraryList.get(n);
                Object backendElem = backendList.get(n);
                if (!(libraryElem instanceof DataEntry libraryElemEntry)
                        || !(backendElem instanceof DataEntry backendElemEntry)) {
                    throw error("Wrongly typed list entries " + libraryElem + " and " + backendElem);
                }
                entriesEqual(libraryElemEntry, backendElemEntry);
            }
            return; // Skip comment check
        } else if (libraryValue instanceof Float || libraryValue instanceof Double) {
            assertTrue(
                    (backendValue instanceof Float || backendValue instanceof Double)
                            && equalsDegreeOfAccuracy(((Number) libraryValue).doubleValue(), ((Number) backendValue).doubleValue()),
                    "Expected " + libraryValue + " but got " + backendValue
            );
        } else {
            // Byte, Short, Integer, Long, Boolean, Character, or String
            assertEquals(libraryValue.toString(), backendValue.toString());
        }
        commentsEqual(false, fromLibrary.getComments(), fromBackend.getComments());
    }

    void headerFooterEqual(Backend.Document fromLibrary, Backend.Document fromBackend) {
        try {
            commentsEqual(true, fromLibrary.comments(), fromBackend.comments());
        } catch (AssertionError ex) {
            throw error("Document-level comments became " + fromBackend.comments(), ex);
        }
    }

    private void commentsEqual(boolean documentLevel, CommentData fromLibrary, CommentData fromBackend) {
        for (CommentLocation commentLocation : CommentLocation.values()) {
            if (backendMeta.supportsComments(documentLevel, true, commentLocation)) {
                assertEquals(fromLibrary.getAt(commentLocation), fromBackend.getAt(commentLocation));
            }
        }
    }

    private static boolean equalsDegreeOfAccuracy(double val1, double val2) {
        double epsilon = 0.00001;
        return Math.abs(val1 - val2) < epsilon;
    }
}
