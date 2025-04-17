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

package space.arim.dazzleconf2.data;

import java.io.IOException;

public interface DataRoot {

    /**
     * IO proof, functional interface for use with reader/writer methods
     * @param <R> the result type
     * @param <U> the utility type
     */
    interface Operation<R, U> {

        /**
         * Instructs the caller that this implementation does not buffer its calls to the utility in
         * {@link #operateUsing(Object)}
         *
         * @return whether this implementation buffers of its own accord
         */
        default boolean handlesBuffering() {
            return false;
        }

        /**
         * Performs the operation
         * @param utility the utility with which it happens
         * @return the return value
         * @throws IOException upon failure
         */
        R operateUsing(U utility) throws IOException;
    }

}
