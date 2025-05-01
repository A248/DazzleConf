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

package space.arim.dazzleconf2.backend;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.*;

/**
 * A key path consists of an ordered sequence of strings.
 * <p>
 * Example: "my.brave.world" is a key path consisting of three strings. The front of the key path would be "my" and the
 * back would be "enabled". A {@code KeyPath} stores the ordered sequence and can be expanded at the front or the
 * back, but it cannot be shrunk (this class does not support shrinking).
 * <p>
 * <b>Printing and display</b>
 * <p>
 * This class implements {@link Printable} for display purposes. The dot-separated form will be the displayed product:
 * <pre>
 *     {@code
 *     KeyPath myBraveWorld = new KeyPath("my", "brave", "world"); // var-args constructor
 *     assert "my.brave.world".equals(myBraveWorld.printString());
 *     }
 * </pre>
 * <p>
 * <b>Mutability and locking changes</b>
 * <p>
 * {@code KeyPath} is mutable by default. However, an instance can be immutably locked it with
 * {@link #lockChanges()}, and a locked instance can be relied upon to never change (unlocking is not possible).
 * As a consequence, {@code KeyPath} is only thread safe if changes are locked.
 * <p>
 * <b>Key mapping</b>
 * A {@code KeyPath} stores strings without distinction. However, users can add key mapping with
 * {@link #applyKeyMapper(KeyMapper)}. If a key mapper is applied, it will map all existing key parts in this
 * {@code KeyPath}, as well as automatically map new key parts added (whether to the front or part). When those
 * same key parts are output at a later time (e.g. through <code>printString</code> or <code>intoParts</code>),
 * they are guaranteed to have been run through the mapper.
 * <p>
 * A key mapper, if attached to an instance of this class, is treated as "invisible" to other {@code KeyPath}
 * instances. The key mapper itself will never be copied to another instance, even if its (mapped) key parts are.
 *
 */
public final class KeyPath implements Printable {

    /// The {@code KeyMapper} is lazily applied, meaning the contents of this collection are not mapped
    private final ArrayDeque<CharSequence> parts;

    private transient KeyMapper keyMapper;
    private transient boolean locked;

    /**
     * Creates from the given parts. The constructed key path will not be locked.
     *
     * @param parts the parts
     */
    public KeyPath(@NonNull String @NonNull...parts) {
        this.parts = new ArrayDeque<>(Arrays.asList(parts));
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
        if (other.keyMapper == null) {
            this.parts = new ArrayDeque<>(other.parts);
        } else {
            ArrayDeque<CharSequence> parts = new ArrayDeque<>(other.parts.size());
            other.parts.forEach(part -> parts.addLast(other.keyMapper.labelToKey(part.toString())));
            this.parts = parts;
        }
    }

    /**
     * Creates an empty key path
     *
     */
    public KeyPath() {
        this.parts = new ArrayDeque<>();
    }

    /**
     * Applies a key mapper to all the key parts.
     * <p>
     * All existing key parts will be mapped through the provided argument, as will parts added later.
     * <p>
     * This method can only be called once per instance. Once called, the given key mapper will be attached to this
     * key path, but the key mapper itself will not be visible or usable by other {@code KeyPath} instances. Only the
     * mapped key parts (the result of applying the key mapper) will be observable.
     *
     * @param keyMapper the key mapper
     * @throws IllegalStateException if a key mapper has already been set, or this {@code KeyPath} is locked
     */
    public void applyKeyMapper(@NonNull KeyMapper keyMapper) {
        if (locked) {
            throw new IllegalStateException("locked");
        }
        if (this.keyMapper != null) {
            throw new IllegalStateException("key mapper already set");
        }
        this.keyMapper = Objects.requireNonNull(keyMapper);
    }

    /**
     * Locks this key path, causing it to become immutable.
     * <p>
     * After this method has been called, no content-modifying methods can be used and will throw
     * {@code IllegalStateException} if attempted. No key mapper can be applied either.
     * <p>
     * Calling this method more than once has no effect.
     *
     */
    public void lockChanges() {
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
    public void addFront(@NonNull CharSequence part) {
        if (locked)
            throw new IllegalStateException("locked");
        parts.addFirst(part);
    }

    /**
     * Adds a key part at the back.
     * <p>
     * This part will be appended behind the other parts.
     *
     * @param part the part
     * @throws IllegalStateException if this key path was locked to be immutable
     */
    public void addBack(@NonNull CharSequence part) {
        if (locked)
            throw new IllegalStateException("locked");
        parts.addLast(part);
    }

    /**
     * Turns into key path parts.
     * <p>
     * The returned array may be modified freely and will not mutate this key path.
     *
     * @return the key path's parts
     */
    public @NonNull CharSequence @NonNull [] intoParts() {
        CharSequence[] intoParts = parts.toArray(new CharSequence[0]);
        KeyMapper keyMapper = this.keyMapper;
        if (keyMapper != null) {
            for (int n = 0; n < intoParts.length; n++) {
                intoParts[n] = keyMapper.labelToKey(intoParts[n].toString());
            }
        }
        return intoParts;
    }

    /**
     * Same as {@link #intoParts()} but returns a list.
     * <p>
     * The returned list may be modified freely and will not mutate this key path. However, there is no guarantee
     * that the list has a non-fixed size, e.g. it might be {@code Arrays.asList}
     *
     * @return the key path parts
     */
    public @NonNull List<@NonNull CharSequence> intoPartsList() {
        return Arrays.asList(intoParts());
    }

    @Override
    public String toString() {
        return printString();
    }

    @Override
    public @NonNull String printString() {
        StringBuilder builder = new StringBuilder();
        printTo(builder);
        return builder.toString();
    }

    @Override
    public void printTo(@NonNull Appendable output) throws IOException {
        CharSequence[] parts = this.parts.toArray(new CharSequence[0]);
        KeyMapper keyMapper = this.keyMapper;
        for (int n = 0; n < parts.length; n++) {
            if (n != 0) {
                output.append('.');
            }
            output.append(keyMapper == null ?
                    parts[n] : keyMapper.labelToKey(parts[n].toString())
            );
        }
    }

    @Override
    public void printTo(@NonNull StringBuilder output) {
        try {
            printTo((Appendable) output);
        } catch (IOException e) {
            throw new AssertionError("StringBuilder does not throw IOException", e);
        }
    }
}
