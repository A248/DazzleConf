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

package space.arim.dazzleconf2.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * A key path. Consists of an ordered sequence of strings.
 * <p>
 * Example: "my-brave-world.this-feature.enabled" is a key path consisting of three strings.
 *
 */
public final class KeyPath {

    // This list is actually in backwards order
    // TODO Switch to using ArrayDeque to improve performance and readability
    private final ArrayList<String> parts;

    /**
     * Creates from the given parts
     * @param parts the parts
     */
    public KeyPath(String...parts) {
        this.parts = new ArrayList<>(Arrays.asList(parts));
        Collections.reverse(this.parts);
    }

    /**
     * Creates an empty key path
     *
     */
    public KeyPath() {
        this.parts = new ArrayList<>();
    }

    /**
     * Adds a key path at the front
     *
     * @param part the part
     */
    public void addFront(String part) {
        parts.add(part);
    }

    /**
     * Adds a key path at the back
     *
     * @param part the part
     */
    public void addBack(String part) {
        parts.add(0, part);
    }

    /**
     * Turns into key path parts
     *
     * @return the parts
     */
    public String[] intoParts() {
        String[] result = parts.toArray(new String[0]);
        Collections.reverse(Arrays.asList(result));
        return result;
    }

    /**
     * Writes a string representation to the given builder. The key parts are separated by dots.
     *
     * @param builder the builder
     */
    public void toString(StringBuilder builder) {
        String[] partsBackward = parts.toArray(new String[0]);
        for (int n = partsBackward.length - 1; n >= 0; n--) {
            builder.append(partsBackward[n]);
            if (n != 0) {
                builder.append('.');
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }
}
