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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

record RandomGen(Backend.Meta backendMeta, ThreadLocalRandom random, CountEntries countEntries) {

    private static final int DEPTH_LIMIT = 4;
    private static final int LENGTH_LIMIT = 5;

    void fillDataTree(DataTree.Mut dataTree, int lengthMinimum, int nestDepth) {
        if (nestDepth == DEPTH_LIMIT) {
            return;
        }
        // Number of key-value pairs to generate
        int length = random.nextInt(lengthMinimum, LENGTH_LIMIT);
        // Anti-duplicator for keys which have the same string-based representation
        Set<String> seenKeys = backendMeta.allKeysAreStrings() ? new HashSet<>() : null;

        for (int n = 0; n < length; n++) {
            // Keep generating the key until we find a unique one
            Object key;
            do {
                key = generateRandomValue(true, nestDepth);
            } while (seenKeys != null && !seenKeys.add(key.toString()));

            dataTree.set(key, generateRandomEntry(nestDepth));
        }
        countEntries.count += length;
    }

    CommentData generateDocumentComments() {
        return generateComments(true);
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
        countEntries.count += length;
        return entryList;
    }

    private DataEntry generateRandomEntry(int nestDepth) {
        DataEntry entry = new DataEntry(generateRandomValue(false, nestDepth));
        boolean comments;
        {
            Object value = entry.getValue();
            comments = !(value instanceof DataTree) && !(value instanceof List);
        }
        if (comments && random.nextBoolean()) {
            entry = entry.withComments(generateComments(false));
        }
        return entry;
    }

    private CommentData generateComments(boolean documentLevel) {
        CommentData commentData = CommentData.empty();
        for (CommentLocation commentLocation : CommentLocation.values()) {
            if (backendMeta.supportsComments(documentLevel, false, commentLocation)) {
                int cap = switch (commentLocation) {
                    case ABOVE, BELOW -> 3;
                    case INLINE -> 1;
                };
                String[] comments = new String[cap];
                for (int n = 0; n < cap; n++) {
                    String generated;
                    do {
                        generated = randString();
                    } while (generated.codePoints().anyMatch(Character::isISOControl));
                    comments[n] = generated;
                }
                commentData = commentData.setAt(commentLocation, comments);
            }
        }
        return commentData;
    }

    private Object generateRandomValue(boolean key, int nestDepth) {
        boolean scalar = key || random.nextBoolean();
        if (scalar) {
            int switchLimit;
            if (!key) {
                switchLimit = 9; // Accept everything below
            } else if (backendMeta.allKeysAreStrings()) {
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
        fillDataTree(dataTree, 0, nestDepth + 1);
        return dataTree;
    }

    private static final char[] alphanumeric = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890".toCharArray();

    private String randAlphanumericString() {
        int length = 1 + random.nextInt(8);
        char[] build = new char[length];
        for (int n = 0; n < length; n++) {
            build[n] = alphanumeric[random.nextInt(alphanumeric.length)];
        }
        return String.valueOf(build);
    }

    private String randString() {
        byte[] data = new byte[random.nextInt(32)];
        random.nextBytes(data);
        return new String(data, StandardCharsets.UTF_8);
    }

}
