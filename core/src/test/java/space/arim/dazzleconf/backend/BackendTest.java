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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import space.arim.dazzleconf.TestingErrorSource;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BackendTest {

    protected abstract Backend createBackend();

    protected abstract boolean nonStringKeys();

    public interface ForErrorPurposes {}

    @TestFactory
    public Stream<DynamicTest> readWriteRandomData() {
        ErrorContext.Source errorSource  = new TestingErrorSource().makeErrorSource();
        return Stream.generate(DataTree.Mut::new)
                .limit(100L)
                .map(dataTree -> {
                    {
                        ThreadLocalRandom random = ThreadLocalRandom.current();
                        new RandomGen(random, false).generateRandomDataTree(dataTree, 1, 0);
                    }
                    return DynamicTest.dynamicTest("Using data tree " + dataTree, () -> {
                        DataTree.Immut subject = dataTree.intoImmut();
                        Backend backend = createBackend();
                        assertDoesNotThrow(() -> backend.write(Backend.Document.simple(subject)));

                        LoadResult<Backend.Document> reloadResult = assertDoesNotThrow(() -> backend.read(errorSource));
                        assertTrue(reloadResult.isSuccess(), () -> "Failed to re-read data \n" + subject);
                        Backend.Document reloadedDocument = reloadResult.getOrThrow();
                        if (reloadedDocument == null) {
                            fail("Reloaded document is null for input " + subject);
                        }
                        DataTree reloaded = reloadedDocument.data();
                        new Comparison(dataTree, reloaded).treesEqual(dataTree, reloaded);
                        /*assertTrue(
                                treesEqual(subject, reloaded),
                                "Wrote data tree \n" + subject + "\n and reloaded it to \n" + reloaded
                        );*/
                        // TODO: Reload the DataTree, but need implementation to check float/double equality
                    });
                });
    }

    private record Comparison(DataTree subject, DataTree reloaded) {

        private AssertionError error(String msg) {
            throw new AssertionError(
                    "Wrote data tree \n" + subject + "\n and reloaded it to \n" + reloaded +
                            ".\n But failed because of " + msg
            );
        }

        private AssertionError error(String msg, Error cause) {
            throw new AssertionError(
                    "Wrote data tree \n" + subject + "\n and reloaded it to \n" + reloaded +
                            ".\n But failed because of " + msg, cause
            );
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
        }
    }

    private static boolean equalsDegreeOfAccuracy(double val1, double val2) {
        double epsilon = 0.00001;
        return Math.abs(val1 - val2) < epsilon;
    }

    private final class RandomGen {

        private final ThreadLocalRandom random;
        private final boolean comments;

        private RandomGen(ThreadLocalRandom random, boolean comments) {
            this.random = random;
            this.comments = comments;
        }

        private static final int DEPTH_LIMIT = 1;
        private static final int LENGTH_LIMIT = 10;

        private void generateRandomDataTree(DataTree.Mut dataTree, int lengthMinimum, int nestDepth) {
            if (nestDepth == DEPTH_LIMIT) {
                return;
            }
            int length = random.nextInt(lengthMinimum, LENGTH_LIMIT);
            for (int n = 0; n < length; n++) {
                Object key = generateRandomValue(true, nestDepth);
                DataEntry entry = generateRandomEntry(nestDepth);
                dataTree.set(key, entry);
            }
        }

        private List<DataEntry> generateRandomList(int nestDepth) {
            if (nestDepth == DEPTH_LIMIT) {
                return List.of();
            }
            int length = random.nextInt(LENGTH_LIMIT);
            List<DataEntry> entryList = new ArrayList<>(length);
            for (int n = 0; n < length; n++) {
                entryList.add(generateRandomEntry(nestDepth));
            }
            return entryList;
        }

        private DataEntry generateRandomEntry(int nestDepth) {
            DataEntry entry = new DataEntry(generateRandomValue(false, nestDepth));
            if (comments) {
                if (random.nextBoolean()) {
                    entry = entry.withComments(CommentLocation.ABOVE, generateComments(random.nextInt(5)));
                }
                if (random.nextBoolean()) {
                    entry = entry.withComments(CommentLocation.INLINE, generateComments(1));
                }
                if (random.nextBoolean()) {
                    entry = entry.withComments(CommentLocation.BELOW, generateComments(random.nextInt(3)));
                }
            }
            return entry;
        }

        private List<String> generateComments(int length) {
            List<String> comments = new ArrayList<>(length);
            for (int n = 0; n < length; n++) {
                comments.add(randString());
            }
            return comments;
        }

        private Object generateRandomValue(boolean key, int nestDepth) {
            boolean scalar = key || random.nextBoolean();
            if (scalar) {
                int switchLimit;
                if (!key) {
                    switchLimit = 9; // Everything below
                } else if (nonStringKeys()) {
                    switchLimit = 7; // Skip float, double
                } else {
                    switchLimit = 1; // Skip everything but 0 (string)
                }
                int type = random.nextInt(switchLimit);
                return switch (type) {
                    case 0 -> key ? randAlphanumericString() : randString();
                    case 1 -> random.nextBoolean();
                    case 2 -> (byte) random.nextInt(Byte.MIN_VALUE, ((int) Byte.MAX_VALUE) + 1);
                    case 3 -> (short) random.nextInt(Short.MIN_VALUE, ((int) Short.MAX_VALUE) + 1);
                    case 4 -> random.nextInt();
                    case 5 -> random.nextLong();
                    case 6 -> (char) random.nextInt(Character.MIN_VALUE, ((int) Character.MAX_VALUE) + 1);
                    // Never used for keys
                    case 7 -> random.nextFloat();
                    case 8 -> random.nextDouble();
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                };
            }
            if (random.nextBoolean()) {
                return generateRandomList(nestDepth + 1);
            }
            DataTree.Mut dataTree = new DataTree.Mut();
            generateRandomDataTree(dataTree, 0, nestDepth + 1);
            return dataTree;
        }

        private static final char[] chars = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890".toCharArray();
        private String randAlphanumericString() {
            int length = 1 + random.nextInt(8);
            char[] build = new char[length];
            for (int n = 0; n < length; n++) {
                build[n] = chars[random.nextInt(chars.length)];
            }
            return String.valueOf(build);
        }

        private String randString() {
            byte[] data = new byte[random.nextInt(32)];
            random.nextBytes(data);
            return new String(data, StandardCharsets.UTF_8);
        }
    }

}
