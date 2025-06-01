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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Controls the range of a double.
 * <p>
 * This annotation is made to be used with {@code double} or {@code Double} and not other numeric types. It is only
 * supported by the double liaison.
 *
 */
@Retention(RUNTIME)
@Target(TYPE_USE)
public @interface DoubleRange {

    /**
     * The minimum value.
     * <p>
     * If set to {@code Double.NEGATIVE_INFINITY} (the default), the bounds check is disabled.
     *
     * @return the min value, inclusive
     */
    double min() default Double.NEGATIVE_INFINITY;

    /**
     * The maximum value.
     * <p>
     * If set to {@code Double.POSITIVE_INFINITY} (the default), the bounds check is disabled.
     *
     * @return the max value, inclusive
     */
    double max() default Double.POSITIVE_INFINITY;

}
