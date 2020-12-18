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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.serialiser.ValueSerialiser;

/**
 * Container for annotations used to specify the default value on a config entry. Although such annotations
 * are not required to be present, one of them is necessary to use {@link ConfigurationFactory#loadDefaults()}. <br>
 * <br>
 * When specifying default values, it is not necessary to provide the exact type. Default values specified are treated
 * as pre{@literal -}deserialisation values. As with a user defined configuration, DazzleConf will do its best to convert
 * them, including by its own means and {@link ValueSerialiser}s.
 * 
 * @author A248
 *
 */
public final class ConfDefault {

	private ConfDefault() {}
	
	/**
	 * Specifies the default value as a boolean
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultBoolean {
		boolean value();
	}
	/**
	 * Specifies the default value as a boolean array. This should be used for boolean collections
	 */
	@Retention(RUNTIME)
	@Target(METHOD)

	public @interface DefaultBooleans {
		boolean[] value();
	}
	/**
	 * Specifies the default value as an integer. If necessary, the integer will be converted to another numeric type
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultInteger {
		int value();
	}
	/**
	 * Specifies the default value as an integer array. If necessary, the integers will be converted to another numeric type.
	 * This should be used for numeric integer collections
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultIntegers {
		int[] value();
	}
	/**
	 * Specifies the default value as a long. If necessary, the long will be converted to another numeric type
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultLong {
		long value();
	}
	/**
	 * Specifies the default value as a long array. If necessary, the longs will be converted to another numeric type.
	 * This should be used for long collections
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultLongs {
		long[] value();
	}
	/**
	 * Specifies the default value as a double. If necessary, the double will be converted to another numeric type
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultDouble {
		double value();
	}
	/**
	 * Specifies the default value as a double array. If necessary, the doubles will be converted to another numeric type.
	 * This should be used for floating point numeric collections
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultDoubles {
		double[] value();
	}
	/**
	 * Specifies the default value as string. This should also be used for enums and custom types. 
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultString {
		String value();
	}
	/**
	 * Specifies the default value as a string array. This should be used for collections, where the element type
	 * is a string, enum, or custom type. 
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultStrings {
		String[] value();
	}
	/**
	 * Specifies the default value as a map. The values of this annotation must be in the form of paired key and value strings.
	 * Therefore the length of the array would be even. <br>
	 * <br>
	 * For example, {@literal @DefaultMap({"key1", "true", "key2", "15"})} translates to the same Map as
	 * {@literal Map.of("key1", true, "key2", 15)}
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultMap {
		String[] value();
	}

	/**
	 * Specifies the fully qualified name of the static method returning the default value. <br>
	 * <br>
	 * The method must be static, and it must be visible. It cannot have parameters.
	 * When the default configuration is loaded, the method will be invoked. <br>
	 * <br>
	 * For example, {@literal @DefaultObject("com.mypackage.ConfigDefaults.someObject")}
	 * will invoke the public static method called 'someObject' in the class ConfigDefaults.
	 *
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface DefaultObject {
		String value();
	}
}
