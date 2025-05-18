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
 * Controls the range of a long.
 * <p>
 * This annotation is made to be used with {@code long} or {@code Long} and not other numeric types. It is only
 * supported by the long liaison.
 *
 */
@Retention(RUNTIME)
@Target(TYPE_USE)
public @interface LongRange {

    /**
     * The minimum value
     *
     * @return the min value, inclusive
     */
    long min() default Long.MIN_VALUE;

    /**
     * The maximum value
     *
     * @return the max value, inclusive
     */
    long max() default Long.MAX_VALUE;

}
