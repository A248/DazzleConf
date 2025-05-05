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
import space.arim.dazzleconf2.engine.AnnotationContext;

/**
 * Liaison for shorts
 *
 */
public final class ShortLiaison extends BaseIntegerLiaison<Short> {

    /**
     * Creates
     */
    public ShortLiaison() {}

    @Override
    @NonNull Class<Short> boxedType() {
        return Short.class;
    }

    @Override
    @NonNull Class<?> primitiveType() {
        return short.class;
    }

    @Override
    @Nullable Short defaultValue(@NonNull AnnotationContext annotationContext) {
        ShortDefault shortDefault = annotationContext.getAnnotation(ShortDefault.class);
        if (shortDefault != null) {
            return shortDefault.value();
        }
        ByteDefault byteDefault = annotationContext.getAnnotation(ByteDefault.class);
        if (byteDefault != null) {
            return (short) byteDefault.value();
        }
        return null;
    }

    @Override
    @Nullable Short castNumbers(@NonNull Object input) {
        if (input instanceof Short) {
            return (short) input;
        }
        if (input instanceof Byte) {
            return (short) ((byte) input);
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
    boolean lessOrEq(@NonNull Short value, long max) {
        return (long) value <= max;
    }

    @Override
    boolean greaterOrEq(@NonNull Short value, long min) {
        return (long) value >= min;
    }

    @Override
    boolean lessOrEq(@NonNull Short value, int max) {
        return (int) value <= max;
    }

    @Override
    boolean greaterOrEq(@NonNull Short value, int min) {
        return (int) value >= min;
    }
}
