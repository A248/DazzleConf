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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

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
 * <b>Mutability</b>
 * <p>
 * Mutability of this class is <b>not defined</b>. Please use {@link KeyPath.Mut} or {@link KeyPath.Immut} if you need
 * mutable or immutable versions, or see the package javadoc for more information on the mutability model we use.
 * <p>
 * <b>Key mapping</b>
 * <p>
 * A key path stores strings without distinction and does not handle key mapping. For convenience, users can apply a
 * key mapper to edit existing path elements, via {@link Mut#applyKeyMapper(KeyMapper)}.
 *
 */
public abstract class KeyPath implements Printable {

    /// The {@code KeyMapper} is lazily applied, meaning the contents of this collection are not mapped
    ArrayDeque<CharSequence> parts;
    transient KeyMapper keyMapper;

    private static final ArrayDeque<CharSequence> SHARED_EMPTY_PARTS = new ArrayDeque<>();

    /**
     * Creates from the given parts
     *
     * @param parts the parts
     */
    KeyPath(@NonNull String @NonNull...parts) {
        this.parts = new ArrayDeque<>(Arrays.asList(parts));
    }

    /**
     * Creates from another key path.
     * <p>
     * This will copy the parts of that key path into this one.
     *
     * @param other the other key path
     */
    KeyPath(@NonNull KeyPath other) {
        if (other.keyMapper == null) {
            this.parts = new ArrayDeque<>(other.parts);
        } else {
            ArrayDeque<CharSequence> parts = new ArrayDeque<>(other.parts.size());
            other.parts.forEach(part -> parts.addLast(other.keyMapper.labelToKey(part.toString())));
            this.parts = parts;
        }
    }

    KeyPath(ArrayDeque<CharSequence> parts, KeyMapper keyMapper) {
        this.parts = parts;
        this.keyMapper = keyMapper;
    }

    /**
     * Returns an empty key path.
     * <p>
     * This method is provided for convenience and readability. Mutability of the returned value is not specified.
     *
     * @return an empty key path
     */
    public static @NonNull KeyPath empty() {
        return new Mut();
    }

    /**
     * Gets whether this key path is empty
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return parts.isEmpty();
    }

    /**
     * Specifies either the start or the end of the key path sequence.
     *
     */
    public enum SequenceBoundary {
        /// The front of the key path
        FRONT,
        /// The back of the key path
        BACK;

        /**
         * Returns the opposite boundary of this one.
         * <p>
         * I.e. {@code FRONT -> BACK} and {@code BACK -> FRONT}
         *
         * @return the opposite boundary of this one
         */
        public @NonNull SequenceBoundary opposite() {
            return this == FRONT ? BACK : FRONT;
        }
    }

    /**
     * Regarding this key path as a sequence of strings, this function returns either the very first value (if using
     * {@code SequenceBoundary.FRONT}) or the very last value (if {@code SequenceBoundary.BACK}).
     *
     * @param where the front or the back
     * @return the leading value at that end, or {@code null} if this key path is empty
     */
    public @Nullable CharSequence getLeading(@NonNull SequenceBoundary where) {
        CharSequence edgeValue;
        if (where.equals(SequenceBoundary.FRONT)) {
            edgeValue = parts.peekFirst();
        } else {
            edgeValue = parts.peekLast();
        }
        if (edgeValue == null) {
            return null;
        }
        String unmapped = edgeValue.toString();
        KeyMapper keyMapper = this.keyMapper;
        return keyMapper == null ? unmapped : keyMapper.labelToKey(unmapped).toString();
    }

    /**
     * Runs an action for each key part in the sequence.
     * <p>
     * Lets the caller pick from which end of the sequence to start from, and move in the opposite direction.
     *
     * @param from   the edge to start from; this function will iterate from it toward the other end
     * @param action the action on each part
     */
    public void iterateFrom(@NonNull SequenceBoundary from, Consumer<? super @NonNull CharSequence> action) {
        CharSequence[] parts = intoParts();
        if (from.equals(SequenceBoundary.FRONT)) {
            for (CharSequence part : parts) {
                action.accept(part);
            }
        } else {
            for (int n = parts.length - 1; n >= 0; n--) {
                action.accept(parts[n]);
            }
        }
    }

    /**
     * Runs an action for each key part in the sequence, starting from the front
     *
     * @param action the action on each part
     */
    public void forEach(@NonNull Consumer<? super @NonNull CharSequence> action) {
        iterateFrom(SequenceBoundary.FRONT, action);
    }

    /**
     * Gets this key path as a mutable one.
     * <p>
     * If not mutable, the data is copied to a new key path which is returned. This copying may be performed lazily,
     * such as by deferring to the first mutative operation on the returned object.
     *
     * @return this key path if mutable, or a mutable copy if needed
     */
    public abstract KeyPath.@NonNull Mut intoMut();

    /**
     * Gets this key path as an immutable one.
     * <p>
     * The data contained within this {@code KeyPath} is moved to an immutable instance. The old instance may still be
     * used, but the implementation of this method may be optimized for the case that it is not.
     * <p>
     * If this instance is already {@code DataTree.Immut}, then it may be returned without changes.
     *
     * @return an immutable key path
     */
    public abstract KeyPath.@NonNull Immut intoImmut();

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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyPath)) return false;

        KeyPath that = (KeyPath) o;
        if (keyMapper == that.keyMapper) {
            // Careful: ArrayDeque does not implement equals based on its contents
            return Arrays.equals(parts.toArray(), that.parts.toArray());
        }
        if (parts.size() != that.parts.size()) {
            return false;
        }
        return Arrays.equals(intoParts(), that.intoParts());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(intoParts());
    }

    @Override
    public @NonNull String toString() {
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

    /**
     * A key path which is unmistakably immutable.
     *
     */
    public static final class Immut extends KeyPath {

        /**
         * Creates an empty key path
         *
         */
        public Immut() {
            super(SHARED_EMPTY_PARTS, null);
        }

        /**
         * Creates from the given parts
         *
         * @param parts the parts
         */
        public Immut(@NonNull String @NonNull...parts) {
            super(parts);
        }

        /**
         * Creates from another key path.
         * <p>
         * This will copy the parts of that key path into this one.
         *
         * @param other the other key path
         */
        public Immut(@NonNull KeyPath other) {
            super(other);
        }

        Immut(ArrayDeque<CharSequence> parts, KeyMapper keyMapper) {
            super(parts, keyMapper);
        }

        @Override
        public @NonNull Mut intoMut() {
            Mut mutCopy = new Mut(parts, keyMapper);
            mutCopy.dataFrozen = true;
            return mutCopy;
        }

        @Override
        public @NonNull Immut intoImmut() {
            return this;
        }
    }

    public static final class Mut extends KeyPath {

        // If the data in this Mut is shared with an Immut, it should not be modified
        private boolean dataFrozen;

        /**
         * Creates an empty key path
         *
         */
        public Mut() {
            super(SHARED_EMPTY_PARTS, null);
            dataFrozen = true;
        }

        /**
         * Creates from the given parts
         *
         * @param parts the parts
         */
        public Mut(@NonNull String @NonNull...parts) {
            super(parts);
        }

        /**
         * Creates from another key path.
         * <p>
         * This will copy the parts of that key path into this one.
         *
         * @param other the other key path
         */
        public Mut(@NonNull KeyPath other) {
            super(other);
        }

        Mut(ArrayDeque<CharSequence> parts, KeyMapper keyMapper) {
            super(parts, keyMapper);
        }

        @Override
        public @NonNull Mut intoMut() {
            return this;
        }

        @Override
        public @NonNull Immut intoImmut() {
            // Setting dataFrozen prevents future modifications
            dataFrozen = true;
            return new Immut(parts, keyMapper);
        }

        private void flushKeyMapper() {
            KeyMapper keyMapper = this.keyMapper;
            if (keyMapper != null) {
                if (!parts.isEmpty()) {
                    ArrayDeque<CharSequence> mappedParts = new ArrayDeque<>(this.parts.size() + 8);
                    for (CharSequence part : this.parts) {
                        mappedParts.add(keyMapper.labelToKey(part.toString()));
                    }
                    this.parts = mappedParts;
                    dataFrozen = false;
                }
                this.keyMapper = null;
            }
        }

        private void ensureMutable() {
            flushKeyMapper();
            if (dataFrozen) {
                parts = new ArrayDeque<>(parts);
                dataFrozen = false;
            }
        }

        /**
         * Applies a key mapper to existing key parts.
         * <p>
         * All existing key parts will be mapped through the provided argument. Parts added later will not be
         * affected.
         *
         * @param keyMapper the key mapper
         */
        public void applyKeyMapper(@NonNull KeyMapper keyMapper) {
            flushKeyMapper();
            this.keyMapper = Objects.requireNonNull(keyMapper);
        }

        /**
         * Adds a key part at the front.
         * <p>
         * This part will be prepended before the other parts.
         *
         * @param part the part
         */
        public void addFront(@NonNull CharSequence part) {
            ensureMutable();
            parts.addFirst(part);
        }

        /**
         * Adds a key part at the back.
         * <p>
         * This part will be appended behind the other parts.
         *
         * @param part the part
         */
        public void addBack(@NonNull CharSequence part) {
            ensureMutable();
            parts.addLast(part);
        }

        /**
         * Adds another key path to this one, either at the front or back of this key path.
         *
         * @param boundary where to add the key path; should it be prepended at the front, or appended at the back
         * @param toAdd that which is added to this one
         */
        public void addPath(@NonNull SequenceBoundary boundary, @NonNull KeyPath toAdd) {
            if (toAdd.isEmpty()) {
                return;
            }
            ensureMutable();
            // Start from the other direction in the added key path
            SequenceBoundary sourceStartFrom = boundary.opposite();
            // And add values in the given direction in this key path
            toAdd.iterateFrom(sourceStartFrom, (boundary == SequenceBoundary.FRONT) ? parts::addFirst : parts::addLast);
        }
    }
}
