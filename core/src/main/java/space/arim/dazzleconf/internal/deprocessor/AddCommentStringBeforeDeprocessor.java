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

import space.arim.dazzleconf.internal.ConfEntry;
import space.arim.dazzleconf.internal.ConfigurationDefinition;

public class AddCommentStringBeforeDeprocessor<C> extends DeprocessorBase<C> {

	private final String suffix;

	public AddCommentStringBeforeDeprocessor(ConfigurationDefinition<C> definition, C configData, String suffix) {
		super(definition, configData);
		this.suffix = suffix;
	}
	
	@Override
	Object wrapValue(ConfEntry entry, Object value) {
		List<String> comments = entry.getComments();
		if (!comments.isEmpty()) {
			mapHelper.put(entry.getKey() + suffix, String.join("\n", comments));
		}
		return value;
	}
	
	@Override
	<N> DeprocessorBase<N> createChildDeprocessor(ConfigurationDefinition<N> childDefinition, N childConf) {
		return new AddCommentStringBeforeDeprocessor<>(childDefinition, childConf, suffix);
	}

}
