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
package space.arim.dazzleconf.internal.deprocessor;

import java.util.List;

import space.arim.dazzleconf.factory.CommentedWrapper;
import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;
import space.arim.dazzleconf.internal.NestedConfEntry;

public class CommentedDeprocessor<C> extends MapDeprocessor<C> {

	public CommentedDeprocessor(ConfigurationDefinition<C> definition, C configData) {
		super(definition, configData);
	}
	
	@Override
	Object wrapValue(ConfEntry entry, Object value) {
		List<String> comments = entry.getComments();
		if (comments.size() > 0) {
			return new CommentedWrapper(comments, value);
		}
		return value;
	}
	
	@Override
	<N> MapDeprocessor<N> createChildDeprocessor(NestedConfEntry<N> childEntry, N childConf) {
		return new CommentedDeprocessor<>(childEntry.getDefinition(), childConf);
	}

}
