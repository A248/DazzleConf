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
package space.arim.dazzleconf.sorter;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Implementation of {@link ConfigurationSorter} to be paired with an annotation to sort entries
 * 
 * @author A248
 *
 */
public class AnnotationBasedSorter implements ConfigurationSorter {
	
	/**
	 * Specifies the order of an entry per this sorter
	 * 
	 * @author A248
	 *
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	@Inherited
	public @interface Order {
		/**
		 * The order of this config entry. Lower values come first
		 * 
		 * @return the order of this entry
		 */
		int value();
	}

	@Override
	public int compare(SortableConfigurationEntry entry1, SortableConfigurationEntry entry2) {
		Order o1 = entry1.getMethod().getAnnotation(Order.class);
		Order o2 = entry2.getMethod().getAnnotation(Order.class);
		if (o1 == null) {
			if (o2 == null) {
				return entry1.getKey().compareTo(entry2.getKey());
			}
			return 1;
		}
		if (o2 == null) {
			return -1;
		}
		return o1.value() - o2.value();
	}
	
}

