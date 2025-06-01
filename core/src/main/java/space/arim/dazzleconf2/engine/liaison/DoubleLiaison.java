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
 * Liaison for doubles
 *
 */
public final class DoubleLiaison extends BaseNumberLiaison<Double, DoubleDefault, DoubleRange> {

    /**
     * Creates
     */
    public DoubleLiaison() {}

    @Override
    @NonNull Class<Double> boxedType() {
        return Double.class;
    }

    @Override
    @NonNull Class<Double> primitiveType() {
        return double.class;
    }

    @Override
    @NonNull Class<DoubleDefault> defaultAnnotation() {
        return DoubleDefault.class;
    }

    @Override
    @NonNull Double defaultValue(@NonNull DoubleDefault defaultAnnotation) {
        return defaultAnnotation.value();
    }

    @Override
    @NonNull Double ifMissing(@NonNull DoubleDefault defaultAnnotation) {
        return defaultAnnotation.ifMissing();
    }

    @Override
    @Nullable Double castNumbers(@NonNull Object input) {
        if (input instanceof Double) {
            return (double) input;
        }
        if (input instanceof Float) {
            return (double) ((float) input);
        }
        if (input instanceof Long) {
            return (double) ((long) input);
        }
        if (input instanceof Integer) {
            return (double) ((int) input);
        }
        if (input instanceof Short) {
            return (double) ((short) input);
        }
        if (input instanceof Byte) {
            return (double) ((byte) input);
        }
        return null;
    }

    @Override
    @Nullable Double parseFrom(@NonNull String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    @NonNull Class<DoubleRange> rangeAnnotation() {
        return DoubleRange.class;
    }

    @Override
    @NonNull Double minFrom(@NonNull DoubleRange doubleRange) {
        return doubleRange.min();
    }

    @Override
    @NonNull Double maxFrom(@NonNull DoubleRange doubleRange) {
        return doubleRange.max();
    }

    @Override
    boolean isNaN(@NonNull Double value) {
        return value.isNaN();
    }

    @Override
    boolean greaterOrEq(@NonNull Double value, @NonNull Double min) {
        return min == Double.NEGATIVE_INFINITY || value >= min;
    }

    @Override
    boolean lessOrEq(@NonNull Double value, @NonNull Double max) {
        return max == Double.POSITIVE_INFINITY || value <= max;
    }
}
