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
 * Liaison for longs
 *
 */
public final class LongLiaison extends BaseNumberLiaison<Long, LongDefault, LongRange> {

    /**
     * Creates
     */
    public LongLiaison() {}

    @Override
    @NonNull Class<Long> boxedType() {
        return Long.class;
    }

    @Override
    @NonNull Class<Long> primitiveType() {
        return long.class;
    }

    @Override
    @NonNull Class<LongDefault> defaultAnnotation() {
        return LongDefault.class;
    }

    @Override
    @NonNull Long defaultValue(@NonNull LongDefault defaultAnnotation) {
        return defaultAnnotation.value();
    }

    @Override
    @NonNull Long ifMissing(@NonNull LongDefault defaultAnnotation) {
        return defaultAnnotation.ifMissing();
    }

    @Override
    @Nullable Long castNumbers(@NonNull Object input) {
        if (input instanceof Long) {
            return (Long) input;
        }
        if (input instanceof Integer) {
            return (long) ((int) input);
        }
        if (input instanceof Short) {
            return (long) ((short) input);
        }
        if (input instanceof Byte) {
            return (long) ((byte) input);
        }
        return null;
    }

    @Override
    @Nullable Long parseFrom(@NonNull String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    @NonNull Class<LongRange> rangeAnnotation() {
        return LongRange.class;
    }

    @Override
    @NonNull Long minFrom(@NonNull LongRange longRange) {
        return longRange.min();
    }

    @Override
    @NonNull Long maxFrom(@NonNull LongRange longRange) {
        return longRange.max();
    }

    @Override
    boolean greaterOrEq(@NonNull Long value, @NonNull Long min) {
        return value >= min;
    }

    @Override
    boolean lessOrEq(@NonNull Long value, @NonNull Long max) {
        return value <= max;
    }
}
