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

package space.arim.dazzleconf.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.MissingKeyException;
import space.arim.dazzleconf.error.MissingValueException;
import space.arim.dazzleconf.factory.CommentedWrapper;
import space.arim.dazzleconf.internal.error.DeveloperError;
import space.arim.dazzleconf.internal.error.Errors;
import space.arim.dazzleconf.internal.error.UserError;

/**
 * Helper class for working with nested hierarchical maps
 * 
 * @author A248
 *
 */
@SuppressWarnings("unchecked")
public class NestedMapHelper {
	
	private final Map<String, Object> topLevelMap;
	
	private static final Pattern PERIOD_PATTERN = Pattern.compile(".", Pattern.LITERAL);

	/**
	 * Creates from a top level map
	 * 
	 * @param topLevelMap the top map
	 */
	public NestedMapHelper(Map<String, Object> topLevelMap) {
		this.topLevelMap = Objects.requireNonNull(topLevelMap, "topLevelMap");
	}
	
	/**
	 * Gets the top level map this helper was created with
	 * 
	 * @return the top level map
	 */
	public Map<String, Object> getTopLevelMap() {
		return topLevelMap;
	}
	
	/**
	 * Puts an object into the nested map
	 * 
	 * @param key the key
	 * @param value the value
	 * @throws IllDefinedConfigException if there was another object at the key or the wrong object at some key
	 */
	public void put(String key, Object value) {
		put0(key, value, false);
	}
	
	/**
	 * Combines the map at the specified key with the specified map
	 * 
	 * @param key the key
	 * @param object the map to combine with. May be wrapped in {@link CommentedWrapper} (therefore an Object)
	 * @throws IllDefinedConfigException if there was another object at the key or the wrong object at some key
	 */
	public void combine(String key, Object object) {
		put0(key, object, true);
	}
	
	/*
	 * 
	 * Insertion
	 * 
	 */
	
	private void put0(String key, Object value, boolean combine) {
		String[] keyParts = PERIOD_PATTERN.split(key);
		Map<String, Object> currentMap = topLevelMap;

		int lastIndex = keyParts.length - 1;
		for (int n = 0; n < lastIndex; n++) {
			currentMap = computeMapOrFail(key, currentMap, keyParts[n]);
		}
		if (combine) {
			Map<String, Object> toCombineOriginal = computeMapOrFail(key, currentMap, keyParts[lastIndex]);
			Map<String, Object> toCombine = (toCombineOriginal instanceof LinkedHashMap) ?
					toCombineOriginal : new LinkedHashMap<>(toCombineOriginal);

			Map<String, Object> combineWith;
			if (value instanceof CommentedWrapper) {
				CommentedWrapper commentWrapper = (CommentedWrapper) value;

				currentMap.put(keyParts[lastIndex], new CommentedWrapper(commentWrapper.getComments(), toCombine));
				combineWith = (Map<String, Object>) commentWrapper.getValue();
			} else {
				currentMap.put(keyParts[lastIndex], toCombine);
				combineWith = (Map<String, Object>) value;
			}
			toCombine.putAll(combineWith);

		} else {
			Object previous = currentMap.put(keyParts[lastIndex], value);
			if (previous != null) {
				throw DeveloperError.replacedObject(key, previous, value).toConfigException();
			}
		}
	}
	
	private static Map<String, Object> computeMapOrFail(String fullKey, Map<String, Object> currentMap, String keyPart) {
		Object shouldBeMap = currentMap.computeIfAbsent(keyPart, (k) -> new LinkedHashMap<>());
		if (shouldBeMap instanceof CommentedWrapper) {
			shouldBeMap = ((CommentedWrapper) shouldBeMap).getValue();
		}
		if (!(shouldBeMap instanceof Map)) {
			throw DeveloperError.expectedMap(Errors.When.WRITE_CONFIG, fullKey, shouldBeMap)
					.toConfigException();
		}
		return (Map<String, Object>) shouldBeMap;
	}
	
	/*
	 * 
	 * Retrieval
	 * 
	 */
	
	/**
	 * Gets a nested object at the specified key
	 * 
	 * @param key the key
	 * @return the object
	 * @throws MissingKeyException if the key is not present in the map
	 * @throws IllDefinedConfigException if a simple object was present where a map was expected
	 */
	public Object get(String key) throws MissingKeyException, MissingValueException {
		Map<String, Object> currentMap = topLevelMap;
		String[] keyParts = PERIOD_PATTERN.split(key);
		int lastIndex = keyParts.length - 1;
		for (int n = 0; n < lastIndex; n++) {
			String keyPart = keyParts[n];
			Object nextMap = currentMap.get(keyPart);
			if (nextMap == null) {
				throw MissingKeyException.forKeyAndMessage(key, UserError.missingKey(key));
			}
			if (!(nextMap instanceof Map)) {
				throw DeveloperError.expectedMap(Errors.When.LOAD_CONFIG, key, nextMap)
						.toConfigException();
			}
			currentMap = (Map<String, Object>) nextMap;
		}
		Object value = currentMap.get(keyParts[lastIndex]);
		if (value == null) {
			if (currentMap.containsKey(keyParts[lastIndex])) {
				// Null value
				throw MissingValueException.forKeyAndMessage(key, UserError.nullValue(key));
			} else {
				// Absent value
				throw MissingKeyException.forKeyAndMessage(key, UserError.missingKey(key));
			}
		}
		return value;
	}

}
