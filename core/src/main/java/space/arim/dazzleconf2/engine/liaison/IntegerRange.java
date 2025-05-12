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

/**
 * Controls the range of an int.
 * <p>
 * This annotation is made to be used with {@code int} or {@code Integer} and not other numeric types. It is only
 * supported by the integer liaison.
 *
 */
public @interface IntegerRange {

    /**
     * The minimum value.
     *
     * @return the min value, inclusive
     */
    int min() default Integer.MIN_VALUE;

    /**
     * The maximum value.
     *
     * @return the max value, inclusive
     */
    int max() default Integer.MAX_VALUE;

}
