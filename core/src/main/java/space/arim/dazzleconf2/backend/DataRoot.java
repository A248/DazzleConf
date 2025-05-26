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

package space.arim.dazzleconf2.backend;

import java.io.IOException;

/**
 * The root source of configuration data. This could be a file, a string, or a byte array.
 */
public interface DataRoot {

    /**
     * Checks whether the data is known to exist.
     *
     * @return true if the data is known to be present, false if not present or if its existence cannot be determined
     * @throws IOException if checking for data existence failed
     */
    boolean dataExists() throws IOException;

    /**
     * IO proof, functional interface for use with reader/writer methods
     * @param <R> the result type
     * @param <U> the utility type
     */
    interface Operation<R, U> {

        /**
         * Instructs the caller that this implementation handles its own buffering to the utility in
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
