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

package space.arim.dazzleconf2.engine.liaison;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Liaison for bytes
 *
 */
public final class ByteLiaison extends BaseNumberLiaison<Byte, ByteDefault, ByteRange> {

    /**
     * Creates
     */
    public ByteLiaison() {}

    @Override
    @NonNull Class<Byte> boxedType() {
        return Byte.class;
    }

    @Override
    @NonNull Class<Byte> primitiveType() {
        return byte.class;
    }

    @Override
    @NonNull Class<ByteDefault> defaultAnnotation() {
        return ByteDefault.class;
    }

    @Override
    @NonNull Byte defaultValue(@NonNull ByteDefault defaultAnnotation) {
        return defaultAnnotation.value();
    }

    @Override
    @Nullable Byte castNumbers(@NonNull Object input) {
        if (input instanceof Byte) {
            return (Byte) input;
        }
        return null;
    }

    @Override
    @Nullable Byte parseFrom(@NonNull String input) {
        try {
            return Byte.parseByte(input);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    @NonNull Class<ByteRange> rangeAnnotation() {
        return ByteRange.class;
    }

    @Override
    @NonNull Byte minFrom(@NonNull ByteRange byteRange) {
        return byteRange.min();
    }

    @Override
    @NonNull Byte maxFrom(@NonNull ByteRange byteRange) {
        return byteRange.max();
    }

    @Override
    boolean greaterOrEq(@NonNull Byte value, @NonNull Byte min) {
        return value >= min;
    }

    @Override
    boolean lessOrEq(@NonNull Byte value, @NonNull Byte max) {
        return value <= max;
    }
}
