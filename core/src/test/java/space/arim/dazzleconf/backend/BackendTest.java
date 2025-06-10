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
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public abstract class BackendTest {

    protected abstract Backend createBackend();

    protected abstract boolean nonStringKeys();

    @TestFactory
    public Stream<DynamicTest> readWriteRandomData() {
        return Stream.generate(DataTree.Mut::new)
                .limit(100L)
                .map(dataTree -> {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    new RandomGen(random, false).generateRandomDataTree(dataTree, 1);

                    return DynamicTest.dynamicTest("Using data tree " + dataTree, () -> {
                        DataTree.Immut subject = dataTree.intoImmut();
                        Backend backend = createBackend();
                        assertDoesNotThrow(() -> backend.write(Backend.Document.simple(subject)));

                        // TODO: Reload the DataTree, but need implementation to check float/double equality
                    });
                });
    }

    private final class RandomGen {

        private final ThreadLocalRandom random;
        private final boolean comments;

        private RandomGen(ThreadLocalRandom random, boolean comments) {
            this.random = random;
            this.comments = comments;
        }

        private static final int DEPTH_LIMIT = 5;

        private void generateRandomDataTree(DataTree.Mut dataTree, int nestDepth) {
            if (nestDepth == DEPTH_LIMIT) {
                return;
            }
            int length = random.nextInt(10);
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
            int length = random.nextInt(10);
            List<DataEntry> entryList = new ArrayList<>(length);
            for (int n = 0; n < length; n++) {
                entryList.add(generateRandomEntry(nestDepth));
            }
            return entryList;
        }

        private DataEntry generateRandomEntry(int nestDepth) {
            DataEntry entry = new DataEntry(generateRandomValue(false, nestDepth));
            if (random.nextBoolean()) {
                entry = entry.withLineNumber(random.nextInt(500));
            }
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
                if (key && !nonStringKeys()) {
                    return randAlphanumericString();
                }
                int type = random.nextInt(9);
                return switch (type) {
                    case 0 -> key ? randAlphanumericString() : randString();
                    case 1 -> random.nextBoolean();
                    case 2 -> (byte) random.nextInt(Byte.MIN_VALUE, ((int) Byte.MAX_VALUE) + 1);
                    case 3 -> (short) random.nextInt(Short.MIN_VALUE, ((int) Short.MAX_VALUE) + 1);
                    case 4 -> random.nextInt();
                    case 5 -> random.nextLong();
                    case 6 -> random.nextFloat();
                    case 7 -> random.nextDouble();
                    case 8 -> (char) random.nextInt(Character.MIN_VALUE, ((int) Character.MAX_VALUE) + 1);
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                };
            }
            if (random.nextBoolean()) {
                return generateRandomList(nestDepth + 1);
            }
            DataTree.Mut dataTree = new DataTree.Mut();
            generateRandomDataTree(dataTree, nestDepth + 1);
            return dataTree;
        }

        private static final char[] chars = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890".toCharArray();
        private String randAlphanumericString() {
            int length = random.nextInt(8);
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
