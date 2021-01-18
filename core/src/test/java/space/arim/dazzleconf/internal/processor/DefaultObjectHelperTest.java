/*
 * DazzleConf
 * Copyright © 2020 Anand Beh
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

package space.arim.dazzleconf.internal.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.internal.SingleConfEntry;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultObjectHelperTest {

	private DefaultObjectHelper helper;

	@BeforeEach
	public void setup() throws NoSuchMethodException {
		Method method = getClass().getMethod("setup");
		helper = new DefaultObjectHelper(new SingleConfEntry(method, "key", List.of(), null));
	}

	@Test
	public void toMap() {
		assertEquals(
				Map.of("key1", "true", "key2", "20", "key3", "value3"),
				helper.toMap("key1", "true", "key2", "20", "key3", "value3"));
		assertThrows(IllDefinedConfigException.class, () -> helper.toMap("key1", "true", "key2noValue"));
	}

	private Object toObject(Class<?> clazz, String methodName) {
		return helper.toObject(clazz.getName() + "." + methodName);
	}

	@Test
	public void toObjectNonexistentClassOrMethod() {
		assertThrows(IllDefinedConfigException.class, () -> helper.toObject("nonexistentPackage"));
		assertThrows(IllDefinedConfigException.class, () -> helper.toObject("nonexistent.package"));
		assertThrows(IllDefinedConfigException.class, () -> helper.toObject("package.ClassName"));
		assertThrows(IllDefinedConfigException.class, () -> helper.toObject("package.ClassName."));
		assertThrows(IllDefinedConfigException.class, () -> helper.toObject("package.ClassName.methodName"));
		assertThrows(IllDefinedConfigException.class, () -> toObject(InvisibleDefaults.class, "nonexistentMethod"));
		assertThrows(IllDefinedConfigException.class, () -> toObject(PublicDefaults.class, "nonexistentMethod"));
		assertThrows(IllDefinedConfigException.class, () -> toObject(PublicDefaults.class, "throwsException"));
	}

	@Test
	public void toObjectInvisibleMethods() {
		assertThrows(IllDefinedConfigException.class, () -> toObject(PublicDefaults.class, "privateMethod"));
		assertThrows(IllDefinedConfigException.class, () -> toObject(InvisibleDefaults.class, "insidePrivateClass"));
		assertThrows(IllDefinedConfigException.class, () -> toObject(PublicDefaults.class, "publicInstanceMethod"));
		assertThrows(IllDefinedConfigException.class, () -> toObject(PublicDefaults.class, "privateNonStaticMethod"));
	}

	@Test
	public void toObjectValidMethod() {
		assertEquals(PublicDefaults.validMethod(), toObject(PublicDefaults.class, "validMethod"));
		assertEquals(PublicDefaults.validMethodPrimitive(), toObject(PublicDefaults.class, "validMethodPrimitive"));
	}

}
