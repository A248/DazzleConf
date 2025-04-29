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

package space.arim.dazzleconf2;

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.backend.Printable;

import java.util.List;

/**
 * Output device for when an error occured
 *
 */
public interface ErrorPrint {

    /**
     * Displays the following error contexts in some way, shape, or form
     *
     * @param errorContexts the error contexts
     */
    void onError(@NonNull List<@NonNull ErrorContext> errorContexts);

    /**
     * The message sink (used by some {@code ErrorPrint} implementations) where printable messages are sent
     *
     */
    interface Output {

        /**
         * Outputs the given printable content
         *
         * @param printable the printable content
         */
        void output(@NonNull Printable printable);
    }
}
