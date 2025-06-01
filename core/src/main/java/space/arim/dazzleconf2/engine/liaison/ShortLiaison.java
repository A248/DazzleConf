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
 * Liaison for shorts
 *
 */
public final class ShortLiaison extends BaseNumberLiaison<Short, ShortDefault, ShortRange> {

    /**
     * Creates
     */
    public ShortLiaison() {}

    @Override
    @NonNull Class<Short> boxedType() {
        return Short.class;
    }

    @Override
    @NonNull Class<Short> primitiveType() {
        return short.class;
    }

    @Override
    @NonNull Class<ShortDefault> defaultAnnotation() {
        return ShortDefault.class;
    }

    @Override
    @NonNull Short defaultValue(@NonNull ShortDefault defaultAnnotation) {
        return defaultAnnotation.value();
    }

    @Override
    @NonNull Short ifMissing(@NonNull ShortDefault defaultAnnotation) {
        return defaultAnnotation.ifMissing();
    }

    @Override
    @Nullable Short castNumbers(@NonNull Object input) {
        if (input instanceof Short) {
            return (Short) input;
        }
        if (input instanceof Byte) {
            return (short) ((byte) input);
        }
        if (input instanceof Integer) {
            int intValue = (Integer) input;
            if ((short) intValue == intValue) {
                return (short) intValue;
            }
        }
        if (input instanceof Long) {
            long longValue = (Long) input;
            if ((short) longValue == longValue) {
                return (short) longValue;
            }
        }
        return null;
    }

    @Override
    @Nullable Short parseFrom(@NonNull String input) {
        try {
            return Short.parseShort(input);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    @NonNull Class<ShortRange> rangeAnnotation() {
        return ShortRange.class;
    }

    @Override
    @NonNull Short minFrom(@NonNull ShortRange shortRange) {
        return shortRange.min();
    }

    @Override
    @NonNull Short maxFrom(@NonNull ShortRange shortRange) {
        return shortRange.max();
    }

    @Override
    boolean greaterOrEq(@NonNull Short value, @NonNull Short min) {
        return value >= min;
    }

    @Override
    boolean lessOrEq(@NonNull Short value, @NonNull Short max) {
        return value <= max;
    }
}
