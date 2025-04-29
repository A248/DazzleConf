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
 * Liaison for integers
 *
 */
public final class IntegerLiaison extends BaseNumberLiaison<Integer, IntegerDefault, IntegerRange> {

    /**
     * Creates
     */
    public IntegerLiaison() {}

    @Override
    @NonNull Class<Integer> boxedType() {
        return Integer.class;
    }

    @Override
    @NonNull Class<Integer> primitiveType() {
        return int.class;
    }

    @Override
    @NonNull Class<IntegerDefault> defaultAnnotation() {
        return IntegerDefault.class;
    }

    @Override
    @NonNull Integer defaultValue(@NonNull IntegerDefault defaultAnnotation) {
        return defaultAnnotation.value();
    }

    @Override
    @Nullable Integer castNumbers(@NonNull Object input) {
        if (input instanceof Integer) {
            return (Integer) input;
        }
        if (input instanceof Short) {
            return (int) ((short) input);
        }
        if (input instanceof Byte) {
            return (int) ((byte) input);
        }
        return null;
    }

    @Override
    @Nullable Integer parseFrom(@NonNull String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    @NonNull Class<IntegerRange> rangeAnnotation() {
        return IntegerRange.class;
    }

    @Override
    @NonNull Integer minFrom(@NonNull IntegerRange integerRange) {
        return integerRange.min();
    }

    @Override
    @NonNull
    Integer maxFrom(@NonNull IntegerRange integerRange) {
        return integerRange.max();
    }

    @Override
    boolean greaterOrEq(@NonNull Integer value, @NonNull Integer min) {
        return value >= min;
    }

    @Override
    boolean lessOrEq(@NonNull Integer value, @NonNull Integer max) {
        return value <= max;
    }
}
