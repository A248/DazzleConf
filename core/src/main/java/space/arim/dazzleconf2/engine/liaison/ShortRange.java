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
 * Controls the range of a short.
 * <p>
 * This annotation is made to be used with {@code short} or {@code Short} and not other numeric types. It is only
 * supported by the short liaison.
 *
 */
public @interface ShortRange {

    /**
     * The minimum value
     *
     * @return the min value, inclusive
     */
    short min() default Short.MIN_VALUE;

    /**
     * The maximum value
     *
     * @return the max value, inclusive
     */
    short max() default Short.MAX_VALUE;

}
