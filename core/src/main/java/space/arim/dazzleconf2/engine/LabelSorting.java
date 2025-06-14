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

package space.arim.dazzleconf2.engine;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.ConfigurationDefinition;
import space.arim.dazzleconf2.backend.Backend;

import java.util.Comparator;

/**
 * Controls how labels are sorted.
 * <p>
 * <b>Preservation of order</b>
 * <p>
 * Throughout the library, order is preserved from start to finish. From the moment the {@code DataTree} is loaded from
 * the backend, to the moment it is written back, order stays consistent. The only exception to the <i>preservation of
 * order</i> rule is that
 * <p>
 * <b>Sources of disorder</b>
 * <p>
 * That said, there are three reasons why the order of entries in a data tree would appear differently:
 * <ul>
 *     <li>1. The {@code Backend} implementation does not preserve order. If the backend does not preserve order, then
 *     order is undefined. Callers can inquire about this behavior using {@link Backend.Meta#preservesOrder(boolean)}.
 *     </li>
 *     <li>2. The {@code Instantiator} does not scan a configuration interface (i.e., its methods) in a consistent order.
 *     For example, the Java reflections API defines no order for {@link Class#getDeclaredMethods()}</li>
 *     <li>3. When loading a configuration, if missing entries are present (and substitute values exist, see
 *     {@link DefaultValues#ifMissing()}), then those missing entries will be added at the back.</li>
 * </ul>
 * <p>
 * <b>This Interface's Role</b>
 * <p>
 * This interface allows library callers to override the output order when writing to a data tree. By default, it is
 * implemented by {@code Configuration#configureWith} if the backend can write (but not read) order; this removes
 * the first source (listed above) of undefined order.
 *
 */
@API(status = API.Status.EXPERIMENTAL)
public interface LabelSorting {

    /**
     * Whether sorting is enabled at all
     *
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * If sorting is enabled, returns the comparator used to perform it.
     * <p>
     * The comparator will be called for pairs of labels, which are used as keys within a data tree. These labels
     * correspond to {@link ConfigurationDefinition.Layout#getLabels()} and this comparator will not be called for
     * anything else.
     *
     * @return a comparator for the definition's labels
     * @throws UnsupportedOperationException if {@code isEnabled} is false
     */
    @NonNull Comparator<String> labelComparator();

    /**
     * Makes an instances where sorting is disabled.
     *
     * @return a disabled {@code OutputSorting}
     */
    static @NonNull LabelSorting disabled() {
        return new LabelSorting() {
            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public @NonNull Comparator<String> labelComparator() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
