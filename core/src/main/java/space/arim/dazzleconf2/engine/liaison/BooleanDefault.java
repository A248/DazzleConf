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

import space.arim.dazzleconf2.engine.DefaultValues;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the default value as a boolean.
 * <p>
 * This annotation is made to be used with {@code boolean} or {@code Boolean} through the boolean liaison.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface BooleanDefault {

    /**
     * The boolean value to provide as a default
     *
     * @return the boolean value
     */
    boolean value();

    /**
     * The boolean value to provide as an "if missing" default. See {@link DefaultValues#ifMissing()} for
     * considerations.
     *
     * @return the boolean value if missing
     */
    boolean ifMissing();

}
