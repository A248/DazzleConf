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
package space.arim.dazzleconf.annote;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import space.arim.dazzleconf.validator.ValueValidator;

/**
 * Attaches a value validator to a config entry
 * 
 * @author A248
 * @deprecated Value validators will be removed in DazzleConf 2.0 since they do not fit the new API model. Meanwhile,
 * you might save time by switching usage of this class to modifying your value serialisers and checking output values
 * appropriately. If need be, make new types that uphold your requirements.
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
@Deprecated
public @interface ConfValidator {

	/**
	 * Specifies the {@link ValueValidator} to use for this config entry.  The {@code ValueValidator}
	 * must have a either a public static {@code getInstance()} method returning an instance or a public constructor,
	 * each with no arguments, in order to be created.
	 * 
	 * @return the value validator to use
	 */
	Class<? extends ValueValidator> value();
	
}
