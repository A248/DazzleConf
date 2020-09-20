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

import java.lang.reflect.Method;
import java.util.List;

import space.arim.dazzleconf.annote.ConfKey;

/**
 * An entry in a configuration. Part of low level API for {@link ConfigurationSorter}s
 * 
 * @author A248
 *
 */
public interface SortableConfigurationEntry {

	/**
	 * Gets the raw method corresponding to this entry
	 * 
	 * @return the raw method
	 */
	Method getMethod();
	
	/**
	 * Gets the config key for this entry. This is usually the method name unless
	 * {@link ConfKey} has been specified.
	 * 
	 * @return the config key for this entry
	 */
	String getKey();
	
	/**
	 * Gets an immutable list of the comments on this entry
	 * 
	 * @return the config comments on this entry, or an empty list if none exist
	 */
	List<String> getComments();
	
}
