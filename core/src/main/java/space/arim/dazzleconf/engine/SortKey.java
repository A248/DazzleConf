/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

package space.arim.dazzleconf.engine;

/**
 * A sort key is an opaque (but sortable) key for the nodes of a {@link DefinedLayout}.
 * <p>
 * Instances of this type must implement {@code equals()} and {@code hashCode()} with respect to itself in a way
 * that is <i>consistent</i> with {@link Comparable}.
 * <p>
 * Note that different {@code SortKey} implementations (such as from different layouts) may NOT be mutually
 * comparable.
 *
 * @param <K> the type of the key
 */
public interface SortKey<K extends SortKey<K>> extends Comparable<K> {
}
