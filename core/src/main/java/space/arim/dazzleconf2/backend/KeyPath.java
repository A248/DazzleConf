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
 * Mutability of this class is <b>not defined</b>. Please use {@link KeyPath.Immut} or {@link KeyPath.Mut} if you need
 * guaranteed mutable or immutable versions, or see the package javadoc for more information on the mutability model we use.
 * <p>
 * <b>Key mapping</b>
 * <p>
 * A {@code KeyPath} stores strings without distinction. However, users can add key mapping with
 * {@link Mut#applyKeyMapper(KeyMapper)}. If a key mapper is applied, it will map all existing key parts in this
 * {@code KeyPath}, as well as automatically map new key parts added (whether to the front or part). When those
 * same key parts are output at a later time (e.g. through <code>printString</code> or <code>intoParts</code>),
 * they are guaranteed to have been run through the mapper.
 * <p>
 * A key mapper, if attached to an instance of this class, is treated as "invisible" to other {@code KeyPath}
 * instances. The key mapper itself will never be copied to another instance, even if its (mapped) key parts are.
 *
 */
public abstract class KeyPath implements Printable {

    /// The {@code KeyMapper} is lazily applied, meaning the contents of this collection are not mapped
    ArrayDeque<CharSequence> parts;
    transient KeyMapper keyMapper;

    /**
     * Creates from the given parts
     *
     * @param parts the parts
     */
    protected KeyPath(@NonNull String @NonNull...parts) {
        this.parts = new ArrayDeque<>(Arrays.asList(parts));
    }

    /**
     * Creates from another key path.
     * <p>
     * This will copy the contents of that key path into this one.
     *
     * @param other the other key path
     */
    protected KeyPath(@NonNull KeyPath other) {
        if (other.keyMapper == null) {
            this.parts = new ArrayDeque<>(other.parts);
        } else {
            ArrayDeque<CharSequence> parts = new ArrayDeque<>(other.parts.size());
            other.parts.forEach(part -> parts.addLast(other.keyMapper.labelToKey(part.toString())));
            this.parts = parts;
        }
    }

    protected KeyPath(ArrayDeque<CharSequence> parts, KeyMapper keyMapper) {
        this.parts = parts;
        this.keyMapper = keyMapper;
    }

    /**
     * Runs an action for each key part in the sequence
     *
     * @param action the action
     */
    public void forEach(Consumer<? super @NonNull CharSequence> action) {
        for (CharSequence part : intoParts()) {
            action.accept(part);
        }
    }

    /**
     * Gets this key path as a mutable one.
     * <p>
     * If not mutable, the data is copied to a new key path which is returned. This copying may be performed lazily, such
     * as by deferring to the first mutative operation on the returned object.
     *
     * @return this key path if mutable, or a mutable copy if needed
     */
    public abstract KeyPath.@NonNull Mut intoMut();

    /**
     * Gets this key path as an immutable one.
     * <p>
     * The data contained within this {@code KeyPath} is evacuated and moved into a new instance. After the call, the
     * old instance (this object) is poisoned and must not be used.
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

    /**
     * A key path which is unmistakably immutable.
     *
     */
    public static final class Immut extends KeyPath {

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
         * This will copy the contents of that key path into this one.
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
            mutCopy.state = Mut.COPY_BEFORE_MUTATE;
            return mutCopy;
        }

        @Override
        public @NonNull Immut intoImmut() {
            return this;
        }
    }

    public static final class Mut extends KeyPath {

        private int state;

        private static final int REGULAR = 0;
        private static final int COPY_BEFORE_MUTATE = 1;
        private static final int POISONED = 2;

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
         * This will copy the contents of that key path into this one.
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
            // SAFETY
            // Setting state = POISONED prevents future modifications
            state = POISONED;
            return new Immut(parts, keyMapper);
        }

        private void ensureMutable() {
            if (state == POISONED) {
                throw new IllegalStateException("poisoned from #intoImmut");
            }
            if (state == COPY_BEFORE_MUTATE) {
                parts = new ArrayDeque<>(parts);
                state = REGULAR;
            }
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
         * @throws IllegalStateException if a key mapper has already been set
         */
        public void applyKeyMapper(@NonNull KeyMapper keyMapper) {
            if (this.keyMapper != null) {
                throw new IllegalStateException("key mapper already set");
            }
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

    }
}
