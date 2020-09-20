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
package space.arim.dazzleconf.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import space.arim.dazzleconf.error.MissingKeyException;
import space.arim.dazzleconf.factory.CommentedWrapper;

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
		this.topLevelMap = topLevelMap;
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
	 * @throws IllegalStateException if there was another object at the key or the wrong object at some key
	 */
	public void put(String key, Object value) {
		put0(key, value, false);
	}
	
	/**
	 * Combines the map at the specified key with the specified map
	 * 
	 * @param key the key
	 * @param object the map to combine with. May be wrapped in {@link CommentedWrapper} (therefore an Object)
	 * @throws IllegalStateException if there was another object at the key or the wrong object at some key
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
			currentMap = computeMapOrFail(currentMap, keyParts[n]);
		}
		if (combine) {
			Map<String, Object> toCombineOriginal = computeMapOrFail(currentMap, keyParts[lastIndex]);
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
				throw new IllegalStateException("Replaced object " + previous + " at " + key + " with " + value);
			}
		}
	}
	
	private static Map<String, Object> computeMapOrFail(Map<String, Object> currentMap, String keyPart) {
		Object shouldBeMap = currentMap.computeIfAbsent(keyPart, (k) -> new LinkedHashMap<>());
		if (shouldBeMap instanceof CommentedWrapper) {
			shouldBeMap = ((CommentedWrapper) shouldBeMap).getValue();
		}
		if (!(shouldBeMap instanceof Map)) {
			throw new IllegalStateException("Value " + shouldBeMap + " is not a Map");
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
	 */
	public Object get(String key) throws MissingKeyException {
		Map<String, Object> currentMap = topLevelMap;
		String[] keyParts = PERIOD_PATTERN.split(key);
		int lastIndex = keyParts.length - 1;
		for (int n = 0; n < lastIndex; n++) {
			String keyPart = keyParts[n];
			currentMap = (Map<String, Object>) currentMap.get(keyPart);
			if (currentMap == null) {
				throw MissingKeyException.forKey(key);
			}
		}
		Object value = currentMap.get(keyParts[lastIndex]);
		if (value == null) {
			throw MissingKeyException.forKey(key);
		}
		return value;
	}
	
}
