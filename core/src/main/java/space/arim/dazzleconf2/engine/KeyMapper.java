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

import org.checkerframework.checker.nullness.qual.NonNull;
import space.arim.dazzleconf2.Configuration;

/**
 * Simple interface for mapping method names into backend configuration keys.
 * <p>
 * For example, some formats (JSON) use lowerCamelCase for option names, whereas others use snake-case (YAML).
 * Fortunately, the key mapper is configured automatically when using {@link Configuration#configureWith}.
 */
public interface KeyMapper {

    /**
     * Turns a method name into a key
     * @param methodName the method name
     * @return the key compponent
     */
    @NonNull CharSequence methodNameToKey(@NonNull String methodName);

}
