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
 * Liaison for floats
 *
 */
public final class FloatLiaison extends BaseNumberLiaison<Float, FloatDefault, FloatRange> {

    /**
     * Creates
     */
    public FloatLiaison() {}

    @Override
    @NonNull Class<Float> boxedType() {
        return Float.class;
    }

    @Override
    @NonNull Class<Float> primitiveType() {
        return float.class;
    }

    @Override
    @NonNull Class<FloatDefault> defaultAnnotation() {
        return FloatDefault.class;
    }

    @Override
    @NonNull Float defaultValue(@NonNull FloatDefault defaultAnnotation) {
        return defaultAnnotation.value();
    }

    @Override
    @NonNull Float ifMissing(@NonNull FloatDefault defaultAnnotation) {
        return defaultAnnotation.ifMissing();
    }

    @Override
    @Nullable Float castNumbers(@NonNull Object input) {
        if (input instanceof Float) {
            return (float) input;
        }
        if (input instanceof Long) {
            return (float) ((long) input);
        }
        if (input instanceof Integer) {
            return (float) ((int) input);
        }
        if (input instanceof Short) {
            return (float) ((short) input);
        }
        if (input instanceof Byte) {
            return (float) ((byte) input);
        }
        if (input instanceof Double) {
            double doubleValue = (Double) input;
            if (doubleValue <= Float.MAX_VALUE && doubleValue >= -Float.MAX_VALUE) {
                return (float) doubleValue;
            }
        }
        return null;
    }

    @Override
    @Nullable Float parseFrom(@NonNull String input) {
        try {
            return Float.parseFloat(input);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    @NonNull Class<FloatRange> rangeAnnotation() {
        return FloatRange.class;
    }

    @Override
    @NonNull Float minFrom(@NonNull FloatRange floatRange) {
        return floatRange.min();
    }

    @Override
    @NonNull Float maxFrom(@NonNull FloatRange floatRange) {
        return floatRange.max();
    }

    @Override
    boolean isNaN(@NonNull Float value) {
        return value.isNaN();
    }

    @Override
    boolean greaterOrEq(@NonNull Float value, @NonNull Float min) {
        return min == Float.NEGATIVE_INFINITY || value >= min;
    }

    @Override
    boolean lessOrEq(@NonNull Float value, @NonNull Float max) {
        return max == Float.POSITIVE_INFINITY || value <= max;
    }
}
