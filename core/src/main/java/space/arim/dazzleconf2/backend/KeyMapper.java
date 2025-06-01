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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.Configuration;

/**
 * Simple interface for mapping method names into backend configuration keys.
 * <p>
 * For example, some formats (JSON) use lowerCamelCase for option names, whereas others use snake-case (YAML).
 * Fortunately, the key mapper is configured automatically when using {@link Configuration#configureWith(Backend)}.
 * <p>
 * <b>Equality</b>
 * <p>
 * Implementations of this class are required to provide <code>equals</code> and <code>hashCode</code> based on their
 * concrete type and the functionality they implement. {@code KeyMapper}s which are different concrete classes must
 * never be equal. However, if an implementation exposes additional settings to tweak its behavior, those settings
 * should be tested for equality.
 */
public interface KeyMapper {

    /**
     * Turns the argument into a key. The argument is usually a method name.
     *
     * @param label the method name
     * @return the key compponent
     */
    @NonNull CharSequence labelToKey(@NonNull String label);

    /**
     * Whether this key mapper is equal to another.
     * <p>
     * {@code KeyMapper} implementations are required to be equal to other instances of themselves. They cannot be
     * equal to {@code KeyMapper}s of different concrete classes, but they should consider their own fields (if any)
     * when comparing to their own concrete type. See the class javadoc for more details.
     *
     * @param other the object to test equality with
     * @return true if equal
     */
    @Override
    boolean equals(@Nullable Object other);

}
