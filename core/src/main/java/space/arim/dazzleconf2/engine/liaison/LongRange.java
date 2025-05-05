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
 * Controls the range of an integer type.
 * <p>
 * Supported by the integer, byte, short, and long liaisons.
 *
 */
public @interface LongRange {

    /**
     * The minimum value. If the type being used is not a long, it is cast for comparison.
     *
     * @return the min value, inclusive
     */
    long min() default Long.MIN_VALUE;

    /**
     * The maximum value. If the type being used is not a long, it is cast for comparison.
     *
     * @return the max value, inclusive
     */
    long max() default Long.MAX_VALUE;

}
