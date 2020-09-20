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

/**
 * Controls the range of any numeric type. May be used for byte, short, int, long, float, and double.
 * 
 * @author A248
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
public @interface NumericRange {

	/**
	 * The minimum double value. If the type of the config value is not a double, the config
	 * value is casted for comparison.
	 * 
	 * @return the min value, inclusive
	 */
	double min() default Double.MIN_VALUE;
	
	/**
	 * The maximum double value. If the type of the config value is not a double, the config
	 * value is casted for comparison.
	 * 
	 * @return the max value, inclusive
	 */
	double max() default Double.MAX_VALUE;
	
}
