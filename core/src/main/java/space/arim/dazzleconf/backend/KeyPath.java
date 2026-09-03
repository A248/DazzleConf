/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import space.arim.dazzleconf.DeveloperMistakeException;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A key path consists of an ordered sequence of non-empty strings.
 * <p>
 * Example: "my.brave.world" is a key path consisting of three strings. The front of the key path would be "my" and the
 * back would be "enabled". A {@code KeyPath} stores this ordered sequence and can be expanded at the front or the
 * back, but it cannot be shrunk (this class does not support shrinking).
 * <p>
 * Key paths can be used in different contexts. Configuration labels, entry paths, and translation keys are some of the
 * usages employed by this library. A key path in one context usually does not have meaning in another, so using key
 * path objects across contexts is almost always a mistake.
 * <p>
 * <b>Printing and display</b>
 * <p>
 * This class implements {@link Printable} for display purposes. The dot-separated form will be the displayed product:
 * <pre>
 *     {@code
 *     KeyPath myBraveWorld = new KeyPath.Immut("my", "brave", "world"); // var-args constructor
 *     assert "my.brave.world".equals(myBraveWorld.printString());
 *     }
 * </pre>
 * <p>
 * <b>Mutability and thread safety</b>
 * <p>
 * Mutability of this class is <b>not defined</b>. Please use {@link Mut} or {@link Immut} if you need mutable or
 * immutable versions, and please read the package documentation regarding the mutability model and thread safety of
 * these types.
 * <p>
 * Note that {@code instanceof} is not a reliable way to determine mutability or lack thereof. This class may
 * support additional subclasses for different purposes, such as lazy evaluation or specialized internal structures.
 * <p>
 * <b>Non-empty components</b>
 * <p>
 * A key path's parts are non-empty. Attempting to insert an empty path component will throw an exception.
 * <p>
 * However, because this class deals with {@link CharSequence}s, it is theoretically possible for a mutable character
 * sequence (or a malicious one) to shrink after being added to a mutable key path. This is treated as a contract
 * violation. Empty character sequences, or sequences which become empty, must not be added to a {@code KeyPath.Mut}
 * according to contract.
 * <p>
 * A {@code KeyPath.Immut} always ensures that its sequences are non-empty.
 */
public abstract class KeyPath implements Printable {

    KeyPath() {}

    private static void checkNonEmpty(CharSequence argument) {
        if (argument.length() == 0) {
            throw new IllegalArgumentException("Key path part cannot be empty: " + argument);
        }
    }

    /**
     * Returns an empty key path.
     * <p>
     * This method is provided for convenience and readability. Mutability of the returned value is not specified.
     *
     * @return an empty key path
     */
    @SideEffectFree
    public static @NonNull KeyPath empty() {
        return new Mut();
    }

    /**
     * Whether this key path is empty
     *
     * @return true if empty
     */
    @Pure
    public abstract boolean isEmpty();

    /**
     * Gets the number of key parts in the sequence
     *
     * @return the number of parts in this key path
     */
    @Pure
    public abstract int size();

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
    @SideEffectFree
    public abstract @Nullable CharSequence getLeading(@NonNull SequenceBoundary where);

    /**
     * Runs an action for each key part in the sequence.
     * <p>
     * Lets the caller pick from which end of the sequence to start from, and move in the opposite direction.
     *
     * @param from   the edge to start from; this function will iterate from it toward the other end
     * @param action the action on each part
     */
    @SideEffectFree
    public abstract void iterateFrom(@NonNull SequenceBoundary from, Consumer<? super @NonNull CharSequence> action);

    /**
     * Runs an action for each key part in the sequence, starting from the front
     *
     * @param action the action on each part
     */
    @SideEffectFree
    public void forEach(@NonNull Consumer<? super @NonNull CharSequence> action) {
        iterateFrom(SequenceBoundary.FRONT, action);
    }

    /**
     * Gets this key path as a mutable one.
     * <p>
     * If not mutable, the data is copied to a new key path which is returned. This copying may be performed lazily,
     * such as by deferring to the first mutative operation on the returned object.
     * <p>
     * Note that because {@link #intoImmut()} converts all contained {@code CharSequence}s to strings, this function
     * is not the strict inverse of that one. For example, filling a mutable key path with {@link StringBuilder},
     * changing it into immutable, then using this function again will produce an object with strings instead.
     *
     * @return this key path if mutable, or a mutable copy if needed
     */
    @SideEffectFree
    public abstract KeyPath.@NonNull Mut intoMut();

    /**
     * Gets this key path as an immutable one.
     * <p>
     * The data contained within this {@code KeyPath} is copied to an immutable instance. Because {@link Immut}
     * guarantees deep immutability, all {@code CharSequence}s in this path are copied into strings to ensure they are
     * immutable.
     * <p>
     * If this instance is already immutable, then it may be returned without changes.
     *
     * @return an immutable key path
     */
    @SideEffectFree
    public abstract KeyPath.@NonNull Immut intoImmut();

    /**
     * Turns into key path parts.
     * <p>
     * The returned array may be modified freely and will not mutate this key path.
     *
     * @return the key path's parts
     */
    @SideEffectFree
    public abstract @NonNull CharSequence @NonNull [] intoParts();

    /**
     * Same as {@link #intoParts()} but returns a list.
     * <p>
     * The returned list may be modified freely and will not mutate this key path. However, there is no guarantee
     * that the list has a non-fixed size, e.g. it might be {@code Arrays.asList}
     *
     * @return the key path parts
     */
    @SideEffectFree
    public @NonNull List<@NonNull CharSequence> intoPartsList() {
        return Arrays.asList(intoParts());
    }

    /**
     * Presents this key path as itself another character sequence.
     * <p>
     * The value of the character sequence, if observed, will be equivalent to using {@link #printString()} at any
     * particular point. However, the sequence will stay up-to-date and track changes to this key path object.
     *
     * @return this key path as a character sequence
     */
    @SideEffectFree
    public abstract @NonNull CharSequence asCharSequence();

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
    public void printTo(@NonNull StringBuilder output) {
        try {
            printTo((Appendable) output);
        } catch (IOException ex) {
            throw new AssertionError("StringBuilder does not throw IOException", ex);
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyPath)) return false;

        KeyPath that = (KeyPath) o;
        if (size() != that.size()) {
            return false;
        }
        // Note: ArrayDeque does not implement equality based on elements
        return Arrays.equals(intoParts(), that.intoParts());
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(intoParts());
    }

    abstract ArrayDeque<String> partsForImmut();

    /**
     * Parses a key path from its representation
     *
     * @param dotted a sequence of key path parts separated by the period ({@code "."}) character
     * @return the key path
     * @throws DeveloperMistakeException if the dotted value cannot be parsed as a key path
     */
    public static @NonNull Mut parse(@NonNull String dotted) {
        String[] parts = dotted.split("\\.", -1);
        Mut mut;
        try {
            mut = new Mut(parts);
        } catch (IllegalArgumentException ex) {
            throw new DeveloperMistakeException("Failed to parse '" + dotted + '\'', ex);
        }
        return mut;
    }

    static abstract class Base<E extends CharSequence> extends KeyPath {

        ArrayDeque<E> parts;

        static final ArrayDeque<String> SHARED_EMPTY_PARTS = new ArrayDeque<>();

        static <E1 extends CharSequence, E2 extends CharSequence> ArrayDeque<E2> castParts(ArrayDeque<E1> parts) {
            ArrayDeque<?> partsTemp = parts;
            @SuppressWarnings("unchecked")
            ArrayDeque<E2> partsCast = (ArrayDeque<E2>) partsTemp;
            return partsCast;
        }

        /**
         * Creates from the given parts
         *
         * @param parts the parts
         */
        Base(@NonNull CharSequence @NonNull... parts) {
            ArrayDeque<E> partsDeque = castParts(new ArrayDeque<>(Arrays.asList(parts)));
            for (E part : partsDeque) {
                KeyPath.checkNonEmpty(part);
            }
            this.parts = partsDeque;
        }

        Base(ArrayDeque<E> parts) {
            this.parts = parts;
        }

        @Override
        public final boolean isEmpty() {
            return parts.isEmpty();
        }

        @Override
        public final int size() {
            return parts.size();
        }

        @Override
        public final @Nullable CharSequence getLeading(@NonNull SequenceBoundary where) {
            CharSequence edgeValue;
            if (where.equals(SequenceBoundary.FRONT)) {
                edgeValue = parts.peekFirst();
            } else {
                edgeValue = parts.peekLast();
            }
            return edgeValue;
        }

        @Override
        public final void iterateFrom(@NonNull SequenceBoundary from, Consumer<? super @NonNull CharSequence> action) {
            if (from.equals(SequenceBoundary.FRONT)) {
                for (E part : parts) {
                    action.accept(part);
                }
            } else {
                CharSequence[] parts = intoParts();
                for (int n = parts.length - 1; n >= 0; n--) {
                    action.accept(parts[n]);
                }
            }
        }

        @Override
        public @NonNull CharSequence @NonNull [] intoParts() {
            return parts.toArray(new CharSequence[0]);
        }

        @Override
        public final void printTo(@NonNull Appendable output) throws IOException {
            CharSequence[] parts = this.parts.toArray(new CharSequence[0]);
            for (int n = 0; n < parts.length; n++) {
                if (n != 0) {
                    output.append('.');
                }
                output.append(parts[n]);
            }
        }

        @Override
        public final @NonNull CharSequence asCharSequence() {
            return new KeyPathAsSequence<>(this);
        }
    }

    /**
     * A key path which is unmistakably immutable.
     *
     */
    public static final class Immut extends Base<String> {

        /**
         * Creates an empty key path
         *
         */
        public Immut() {
            super(Base.SHARED_EMPTY_PARTS);
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
            super(other.partsForImmut());
        }

        Immut(ArrayDeque<String> parts) {
            super(parts);
        }

        @Override
        public @NonNull Mut intoMut() {
            Mut mutCopy = new Mut(castParts(parts));
            assert mutCopy.dataFrozen : "class change";
            return mutCopy;
        }

        @Override
        public @NonNull Immut intoImmut() {
            return this;
        }

        @Override
        ArrayDeque<String> partsForImmut() {
            return parts;
        }
    }

    /**
     * A mutable key path.
     * <p>
     * Note that the identity of this class (via {@code instanceof}) is not an ideal way to obtain it. Instead, prefer
     * {@link KeyPath#intoMut()}.
     *
     */
    public static final class Mut extends Base<CharSequence> {

        // If the data in this Mut is shared with an Immut, it should not be modified
        private boolean dataFrozen;
        // If all the parts are Strings, we can further optimize in relation to Immut
        private boolean allString;

        Mut(ArrayDeque<String> sharedParts) {
            super(castParts(sharedParts));
            dataFrozen = true;
            allString = true;
        }

        /**
         * Creates an empty key path
         *
         */
        public Mut() {
            this(Base.SHARED_EMPTY_PARTS);
        }

        /**
         * Creates from the given parts
         *
         * @param parts the parts
         */
        public Mut(@NonNull String @NonNull...parts) {
            super(parts);
            allString = true;
        }

        /**
         * Creates from another key path.
         * <p>
         * This will copy the parts of that key path into this one.
         *
         * @param other the other key path
         */
        public Mut(@NonNull KeyPath other) {
            super(new ArrayDeque<>(other.intoPartsList()));
            if (other instanceof Immut) {
                allString = true;
            } else if (other instanceof Mut) {
                allString = ((Mut) other).allString;
            }
        }

        @Override
        public @NonNull Mut intoMut() {
            return this;
        }

        @Override
        public @NonNull Immut intoImmut() {
            return new Immut(partsForImmut());
        }

        @Override
        ArrayDeque<String> partsForImmut() {
            if (allString) {
                dataFrozen = true;
                return castParts(parts);
            }
            ArrayDeque<String> stringParts = new ArrayDeque<>(parts.size());
            for (CharSequence part : parts) {
                String stringPart = part.toString();
                checkNonEmpty(stringPart);
                stringParts.add(stringPart);
            }
            return stringParts;
        }

        private void ensureMutable() {
            if (dataFrozen) {
                ArrayDeque<CharSequence> oldParts = parts;
                ArrayDeque<CharSequence> newParts;
                if (oldParts.isEmpty()) {
                    newParts = new ArrayDeque<>();
                } else {
                    // Add extra space: we call this method when we need it, after all
                    newParts = new ArrayDeque<>(oldParts.size() + 1);
                    newParts.addAll(oldParts);
                }
                parts = newParts;
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
         * @deprecated this method no longer offers any advantage over mapping the entire path manually.
         */
        @Deprecated
        public void applyKeyMapper(@NonNull KeyMapper keyMapper) {
            if (isEmpty()) {
                return;
            }
            ensureMutable();
            // Recycle all the parts in circular fashion
            int size = parts.size();
            for (int n = 0; n < size; n++) {
                CharSequence part = parts.removeLast();
                CharSequence mapped = keyMapper.labelToKey(part);
                checkNonEmpty(mapped);
                parts.addFirst(mapped);
                allString &= mapped instanceof String;
            }
        }

        /**
         * Adds a key part at the front.
         * <p>
         * This part will be prepended before the other parts. It must not be empty
         *
         * @param part the part
         * @throws IllegalArgumentException if the part is detected as empty
         */
        public void addFront(@NonNull CharSequence part) {
            checkNonEmpty(part);
            ensureMutable();
            parts.addFirst(part);
            allString &= part instanceof String;
        }

        /**
         * Adds a key part at the back.
         * <p>
         * This part will be appended behind the other parts. It must not be empty.
         *
         * @param part the part
         * @throws IllegalArgumentException if the part is detected as empty
         */
        public void addBack(@NonNull CharSequence part) {
            checkNonEmpty(part);
            ensureMutable();
            parts.addLast(part);
            allString &= part instanceof String;
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
            if (allString && (toAdd instanceof KeyPath.Mut)) {
                allString = ((Mut) toAdd).allString;
            }
        }
    }
}
