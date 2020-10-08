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
import java.util.Collection;
import java.util.Set;

import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.serialiser.Decomposer;
import space.arim.dazzleconf.serialiser.ValueSerialiser;
import space.arim.dazzleconf.serialiser.ValueSerialiserMap;

class DecomposerImpl implements Decomposer {

	private final String key;
	private final ValueSerialiserMap serialisers;
	
	DecomposerImpl(String key, ValueSerialiserMap serialisers) {
		this.key = key;
		this.serialisers = serialisers;
	}
	
	@Override
	public <T> Object decompose(Class<T> clazz, T value) {
		if (clazz == Collection.class || clazz == List.class || clazz == Set.class) {
			throw new IllDefinedConfigException(
					"Cannot serialise to a collection via Decomposer#decompose. "
					+ "Serialise each element, place it in a collection, and return that collection instead.");
		}

		// Primitives must come before casting
		if (clazz == char.class) {
			return value.toString();
		}
		if (clazz.isPrimitive()) {
			return value;
		}

		clazz.cast(value);

		if (value instanceof String || value instanceof Boolean || value instanceof Number) {
			return value;
		}
		if (value instanceof Character) {
			return value.toString();
		}
		if (clazz.isEnum()) {
			return ((Enum<?>) value).name();
		}
		return fromSerialiser(getSerialiser(clazz), value);
	}
	
	private <T> ValueSerialiser<T> getSerialiser(Class<T> clazz) {
		ValueSerialiser<T> serialiser  = serialisers.getSerialiser(clazz);
		if (serialiser == null) {
			throw new IllDefinedConfigException("No ValueSerialiser for " + clazz + " at entry " + key);
		}
		return serialiser;
	}
	
	private <T> Object fromSerialiser(ValueSerialiser<T> serialiser, T value) {
		Object serialised = serialiser.serialise(value, this);
		if (serialised == null) {
			throw new IllDefinedConfigException(
					"At key " + key + ", ValueSerialiser#serialise for " + serialiser + " returned null");
		}
		return serialised;
	}

}
