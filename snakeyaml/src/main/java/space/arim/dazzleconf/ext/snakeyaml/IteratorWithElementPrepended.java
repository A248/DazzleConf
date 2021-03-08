/*
 * DazzleConf
 * Copyright © 2021 Anand Beh
 *
 * DazzleConf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DazzleConf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */

package space.arim.dazzleconf.ext.snakeyaml;

import java.util.Iterator;
import java.util.Objects;

/**
 * Iterator does not have a peek() method, so this iterator enables "re-inserting" an element
 *
 * @param <E> the element type
 */
class IteratorWithElementPrepended<E> implements Iterator<E> {

	private E firstElement;
	private final Iterator<E> rest;

	IteratorWithElementPrepended(E firstElement, Iterator<E> rest) {
		this.firstElement = Objects.requireNonNull(firstElement);
		this.rest = rest;
	}

	@Override
	public boolean hasNext() {
		return firstElement != null || rest.hasNext();
	}

	@Override
	public E next() {
		if (firstElement != null) {
			E value = firstElement;
			firstElement = null;
			return value;
		}
		return rest.next();
	}
}
