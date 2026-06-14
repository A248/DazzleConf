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

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

class KeyPathAsSequence<E extends CharSequence> implements CharSequence {

    private final KeyPath.Base<E> base;

    KeyPathAsSequence(KeyPath.Base<E> base) {
        this.base = base;
    }

    @Override
    public int length() {
        if (base.isEmpty()) {
            return 0;
        }
        int len = -1;
        for (CharSequence part : base.parts) {
            len += part.length();
            len += 1;
        }
        return len;
    }

    @Override
    public char charAt(int index) {
        int current = index;
        for (Iterator<E> partIter = base.parts.iterator(); partIter.hasNext();) {
            CharSequence part = partIter.next();
            int partLen = part.length();
            if (current < partLen) {
                return part.charAt(current);
            }
            current -= partLen;
            if (current == 0 && partIter.hasNext()) {
                return '.';
            }
            current -= 1;
        }
        throw new IndexOutOfBoundsException("Index " + index + " for key path '" + this + '\'');
    }

    //@Override - JDK 15
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public @NonNull CharSequence subSequence(int start, int end) {
        if (end < start || end < 0 || start < 0) {
            throw new IndexOutOfBoundsException("start, end = " + start + ", " + end);
        }
        int skip = start;
        int collect = end - start;
        if (collect == 0) {
            int length = length();
            if (end > length) {
                throw new IndexOutOfBoundsException("end > length: " + end + " > " + length);
            }
            return "";
        }
        StringBuilder builder = new StringBuilder(collect);
        for (Iterator<E> partIter = base.parts.iterator(); partIter.hasNext();) {
            CharSequence part = partIter.next();
            int partLen = part.length();
            if (skip < partLen) {
                int gather = Integer.min(partLen - skip, collect);
                collect -= gather;
                builder.append(part, skip, skip + gather);
                skip = 0;
            } else {
                skip -= partLen;
            }
            if (partIter.hasNext()) {
                if (skip == 0) {
                    if (collect != 0) {
                        builder.append('.');
                        collect -= 1;
                    }
                } else {
                    skip -= 1;
                }
            }
            if (collect == 0) {
                break;
            }
        }
        if (collect != 0) {
            throw new IndexOutOfBoundsException("end - start > length");
        }
        return builder;
    }

    private static final class PeriodAddingSpliterator implements Spliterator<CharSequence> {

        private final Spliterator<? extends CharSequence> inner;
        private final boolean immutable;

        private boolean sendPeriod;
        private CharSequence buffered;

        private PeriodAddingSpliterator(Spliterator<? extends CharSequence> inner, boolean immutable) {
            this.inner = inner;
            this.immutable = immutable;
        }

        // Must be called to initialize
        boolean reloadBuffer() {
            return inner.tryAdvance(part -> this.buffered = part);
        }

        @Override
        public boolean tryAdvance(Consumer<? super CharSequence> action) {
            if (sendPeriod) {
                sendPeriod = false;
                action.accept(".");
                return true;
            }
            if (buffered != null) {
                // Exception safety
                CharSequence takeBuffer = buffered;
                buffered = null;
                sendPeriod = reloadBuffer();
                action.accept(takeBuffer); // throw here
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<CharSequence> trySplit() {
            Spliterator<? extends CharSequence> splitInner = inner.trySplit();
            if (splitInner == null) {
                return null;
            }
            // Since we are ORDERED, must split off strict prefix of elements
            PeriodAddingSpliterator splitOff = new PeriodAddingSpliterator(splitInner, immutable);
            splitOff.sendPeriod = sendPeriod;
            splitOff.buffered = buffered;
            sendPeriod = false;
            buffered = null;
            reloadBuffer();
            return splitOff;
        }

        @Override
        public long estimateSize() {
            if (inner.hasCharacteristics(SIZED)) {
                long innerSize = inner.getExactSizeIfKnown();
                if (!sendPeriod && buffered == null) {
                    return innerSize == 0L ? 0L : (innerSize * 2) - 1;
                }
                if (buffered != null) {
                    innerSize += 1;
                }
                // Every remaining part will have a period before it
                innerSize *= 2;
                if (!sendPeriod) {
                    innerSize -= 1;
                }
                return innerSize;
            } else {
                // Even if we don't know how many remaining, approximate by doubling the unseen parts
                long innerEstimate = inner.estimateSize();
                long doubled = innerEstimate * 2;
                if (doubled < 0L) doubled = Long.MAX_VALUE; // overflow
                return doubled;
            }
        }

        @Override
        public int characteristics() {
            int preserved = ORDERED | SIZED | SUBSIZED;
            int imposed = NONNULL | (immutable ? IMMUTABLE : 0);
            int original = inner.characteristics();
            return (original & preserved) | imposed;
        }
    }

    private IntStream concatPartStreams(Function<CharSequence, IntStream> forEachSequence) {
        PeriodAddingSpliterator periodAddingSpliterator = new PeriodAddingSpliterator(
                base.parts.spliterator(), base instanceof KeyPath.Immut
        );
        periodAddingSpliterator.reloadBuffer();
        return StreamSupport.stream(periodAddingSpliterator, false).flatMapToInt(forEachSequence);
    }

    @Override
    public @NonNull IntStream chars() {
        return concatPartStreams(CharSequence::chars);
    }

    @Override
    public @NonNull IntStream codePoints() {
        return concatPartStreams(CharSequence::codePoints);
    }

    @Override
    public @NonNull String toString() {
        return base.printString();
    }
}
