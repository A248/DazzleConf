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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

/**
 * General purpose key path. Mutable and consists of an ordered sequence of strings.
 * <p>
 * Example: "my-brave-world.this-feature.enabled" is a key path consisting of three strings.
 *
 */
public final class KeyPath {

    // This list is actually in backwards order
    // TODO Switch to using ArrayDeque to improve performance and readability
    // TODO Store locked KeyPaths as Object within the same collection
    private final ArrayList<String> parts;

    // TODO add a KeyMapper here
    private transient boolean locked;

    /**
     * Creates from the given parts. The constructed key path will not be locked.
     *
     * @param parts the parts
     */
    public KeyPath(@NonNull String @NonNull...parts) {
        this.parts = new ArrayList<>(Arrays.asList(parts));
        Collections.reverse(this.parts);
    }

    /**
     * Creates from another key path.
     * <p>
     * This will copy the contents of that key path into this one. The new key path will be unlocked, regardless of
     * the original key path's lock state.
     *
     * @param other the other key path
     */
    public KeyPath(@NonNull KeyPath other) {
        this.parts = new ArrayList<>(other.parts);
    }

    /**
     * Creates an empty key path
     *
     */
    public KeyPath() {
        this.parts = new ArrayList<>();
    }

    /**
     * Locks this key path, causing it to become immutable.
     * <p>
     * After this method has been called, no content-modifying methods can be used and will throw
     * {@code IllegalStateException} if attempted. Calling this method more than once has no effect.
     *
     */
    public void lockChanges() {
        if (!locked) {
            parts.trimToSize();
        }
        locked = true;
    }

    /**
     * Gets whether this key path is locked. If locked, it is immutable
     *
     * @return true if locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Creates a new key path which is a copy of this one, and locks it immutably.
     * <p>
     * This key path will remain unlocked and can continue to be modified, but the returned key path will be
     * unmodifiable.
     *
     * @return a locked copy of this key path
     */
    public @NonNull KeyPath newLockedCopy() {
        KeyPath lockedCopy = new KeyPath(this);
        lockedCopy.lockChanges();
        return lockedCopy;
    }

    /**
     * Adds a key part at the front.
     * <p>
     * This part will be prepended before the other parts.
     *
     * @param part the part
     * @throws IllegalStateException if this key path was locked to be immutable
     */
    public void addFront(@NonNull String part) {
        if (locked)
            throw new IllegalStateException("locked");
        parts.add(part);
    }

    /**
     * Adds a key part at the back.
     * <p>
     * This part will be appended behind the other parts.
     *
     * @param part the part
     * @throws IllegalStateException if this key path was locked to be immutable
     */
    public void addBack(@NonNull String part) {
        if (locked)
            throw new IllegalStateException("locked");
        parts.add(0, part);
    }

    /**
     * Turns into key path parts.
     * <p>
     * The returned array may be modified freely and will not mutate this key path.
     *
     * @return the key path's parts
     */
    public @NonNull String @NonNull [] intoParts() {
        String[] result = parts.toArray(new String[0]);
        Collections.reverse(Arrays.asList(result));
        return result;
    }

    /**
     * Same as {@link #intoParts()} but returns a list.
     * <p>
     * The returned list may be modified freely and will not mutate this key path. However, there is no guarantee
     * that the list has a non-fixed size, e.g. it might be {@code Arrays.asList}
     *
     * @return the key path parts
     */
    public @NonNull List<@NonNull String> intoPartsList() {
        return Arrays.asList(intoParts());
    }

    /**
     * Writes a string representation to the given builder. The key parts are separated by dots.
     *
     * @param builder the builder
     */
    public void toString(@NonNull StringBuilder builder) {
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
