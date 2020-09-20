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
package space.arim.dazzleconf.factory;

import java.util.List;
import java.util.Objects;

import space.arim.dazzleconf.internal.ImmutableCollections;

/**
 * A simple wrapper used to attach comments to a configuration wrapper. Often requires special treatment when handling
 * configuration maps.
 * 
 * @author A248
 *
 */
public final class CommentedWrapper {

	private final List<String> comments;
	private final Object value;
	
	/**
	 * Creates from a list of comments and a value
	 * 
	 * @param comments the comments
	 * @param value the value
	 * @throws NullPointerException if {@code value} or {@code comments} or any element in {@code comments} is null
	 */
	public CommentedWrapper(List<String> comments, Object value) {
		this.comments = ImmutableCollections.listOf(comments);
		this.value = Objects.requireNonNull(value, "value");
	}
	
	/**
	 * Gets the comments, which come before the value
	 * 
	 * @return the comments
	 */
	public List<String> getComments() {
		return comments;
	}
	
	/**
	 * Gets the config value
	 * 
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + comments.hashCode();
		result = prime * result + value.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof CommentedWrapper)) {
			return false;
		}
		CommentedWrapper other = (CommentedWrapper) object;
		return value.equals(other.value) && comments.equals(other.comments);
	}

	@Override
	public String toString() {
		return "CommentedWrapper [comments=" + comments + ", value=" + value + "]";
	}
	
}
