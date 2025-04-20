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

import space.arim.dazzleconf.ConfigurationOptions;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies the key whose value will be used for this method. If this annotation is not specified,
 * the method name is used.
 * <p>
 * <b>WARNING:</b> Dotted key paths used to allow "{@literal .}" in order to have the data be pulled from a
 * subsection. This behavior is now <b>deprecated</b> and must be explicit enabled by setting
 * {@link ConfigurationOptions.Builder#setDottedPathInConfKey(boolean)}
 * 
 * @author A248
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ConfKey {

	/**
	 * Defines the key at which this entry's data should be pulled from. The key is relative to the position
	 * of this config method. It may contain {@literal .} in order to have the data be pulled from a subsection.
	 * <p>
	 * <b>WARNING:</b> Dotted key paths used to allow "{@literal .}" in order to have the data be pulled from a
	 * subsection. This behavior is now <b>deprecated</b> and must be explicit enabled by setting
	 * {@link ConfigurationOptions.Builder#setDottedPathInConfKey(boolean)}
	 * 
	 * @return the relative key
	 */
	String value();
	
}
