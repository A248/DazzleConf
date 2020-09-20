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
package space.arim.dazzleconf.serialiser;

import java.net.MalformedURLException;
import java.net.URL;

import space.arim.dazzleconf.error.BadValueException;

/**
 * Value serialiser for {@link URL}
 * 
 * @author A248
 *
 */
public class URLValueSerialiser implements ValueSerialiser<URL> {

	private static final URLValueSerialiser INSTANCE = new URLValueSerialiser();
	
	private URLValueSerialiser() {}
	
	public static URLValueSerialiser getInstance() {
		return INSTANCE;
	}
	
	@Override
	public URL deserialise(String key, Object value) throws BadValueException {
		URL url;
		try {
			url = new URL(value.toString());
		} catch (MalformedURLException ex) {
			throw new BadValueException.Builder().key(key).message("malformed URL " + value).cause(ex).build();
		}
		return url;
	}

	@Override
	public String serialise(URL value) {
		return value.toExternalForm();
	}

}
