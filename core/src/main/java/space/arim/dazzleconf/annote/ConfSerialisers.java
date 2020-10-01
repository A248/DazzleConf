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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.serialiser.ValueSerialiser;

/**
 * Attaches a value serialiser to a configuration interface
 * 
 * @author A248
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ConfSerialisers {

	/**
	 * Specifies additional {@link ValueSerialiser}s to use for this configuration interface. Each {@code ValueSerialiser}
	 * must have a either a public static {@code getInstance()} method returning an instance or a public constructor
	 * with no arguments, in order to be created. <br>
	 * <br>
	 * These serialisers are in addition to the global {@link ConfigurationOptions#getSerialisers()}. When determining
	 * a serialiser for a configuration entry, this annotation is prioritised over the global options. <br>
	 * <br>
	 * Furthermore, note that configuration subsections do not "inherit" the value of this annotation in their own
	 * serialisers. Subsections should likewise use this annotation if it is needed.
	 * 
	 * @return the value serialisers to use
	 */
	Class<? extends ValueSerialiser<?>>[] value();
	
}
