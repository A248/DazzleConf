/* 
 * DazzleConf-core
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-core. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.annote;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import space.arim.dazzleconf.serialiser.ValueSerialiser;

/**
 * Attaches a value serialiser to a specific config entry
 * 
 * @author A248
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
public @interface ConfSerialiser {

	/**
	 * Specifies the {@link ValueSerialiser} to use for this config entry. The {@code ValueSerialiser}
	 * must have a either a public static {@code getInstance()} method returning an instance or a public constructor,
	 * each with no arguments, in order to be created.
	 * 
	 * @return the value serialiser to use
	 */
	Class<? extends ValueSerialiser<?>> value();
	
}
