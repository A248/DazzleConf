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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the default value as string.
 * <p>
 * While primarily used by {@link StringLiaison}, this annotation can theoretically be used by any type liaison.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface DefaultString {

    /**
     * The string value to provide as a default
     *
     * @return the string value
     */
    String value();

    /**
     * The string value to provide as a missing value. If unset, this falls back to {@link #value()}
     *
     * @return the value to provide when missing
     */
    String ifMissing() default StringLiaison.IF_MISSING_STAND_IN;

}
