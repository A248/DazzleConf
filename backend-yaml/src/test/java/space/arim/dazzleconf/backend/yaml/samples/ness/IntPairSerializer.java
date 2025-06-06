/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
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

package space.arim.dazzleconf.backend.yaml.samples.ness;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.engine.DefaultValues;
import space.arim.dazzleconf2.engine.DeserializeInput;
import space.arim.dazzleconf2.engine.SerializeDeserialize;
import space.arim.dazzleconf2.engine.SerializeOutput;
import space.arim.dazzleconf2.engine.TypeLiaison;
import space.arim.dazzleconf2.reflect.TypeToken;

abstract class IntPairSerializer<T> implements TypeLiaison, SerializeDeserialize<T> {

	public abstract Class<T> getTargetClass();

	@Override
	public @Nullable <V> Agent<V> makeAgent(@NonNull TypeToken<V> typeToken, @NonNull Handshake handshake) {
		return Agent.matchOnToken(typeToken, getTargetClass(), () -> {
			return new Agent<T>() {
				@Override
				public @Nullable DefaultValues<T> loadDefaultValues(@NonNull DefaultInit defaultInit) {
					return null;
				}

				@Override
				public @NonNull SerializeDeserialize<T> makeSerializer() {
					return IntPairSerializer.this;
				}
			};
		});
	}

	@Override
	public @NonNull LoadResult<@NonNull T> deserialize(@NonNull DeserializeInput deser) {
		return deser.requireString().flatMap(string -> {
			String[] info = string.split(":", 2);
			try {
				return LoadResult.of(fromInts(Integer.parseInt(info[0]), Integer.parseInt(info[1])));
			} catch (NumberFormatException ex) {
				return deser.throwError("Because of " + ex.getMessage());
			}
		});

	}

	@Override
	public void serialize(@NonNull T value, @NonNull SerializeOutput ser) {
		int[] toInts = toInts(value);
		ser.outString(toInts[0] + ":" + toInts[1]);
	}

	abstract T fromInts(int value1, int value2);
	
	abstract int[] toInts(T value);
	
}
