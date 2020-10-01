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

/**
 * Allows specifying the comment header on a top level configuration or a comment on a sub section. <br>
 * <br>
 * If the target of this annotation is used as a top level config, {@link #value()} becomes the comment header
 * of the configuration. <br>
 * <br>
 * If the target of this annotation is used as a subsection, the value of this annotation instead becomes a comment
 * on that specific subsection. Note the presence of {@link ConfComments} on the method declaring the subsection
 * will override this annotation.
 * 
 * @author A248
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ConfHeader {

	/**
	 * The comment header itself. Each string is on its own line
	 * 
	 * @return the comment header
	 */
	String[] value();
	
}
