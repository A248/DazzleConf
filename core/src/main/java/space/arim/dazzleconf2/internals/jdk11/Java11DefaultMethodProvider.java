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

package space.arim.dazzleconf2.internals.jdk11;

import space.arim.dazzleconf2.internals.DefaultMethodProvider;
import space.arim.dazzleconf2.internals.MethodUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class Java11DefaultMethodProvider implements DefaultMethodProvider {

	@Override
	public MethodHandle getMethodHandle(Method method) throws IllegalAccessException {
		/*
		 * privateLookupIn requires the calling module read the target module
		 * This will indeed result in a cyclic module dependency. Luckily,
		 * JPMS permits cyclic dependencies created through readability edges
		 * (addReads) even though it strictly forbids cycles during initialization
		 * of the module graph.
		 * https://openjdk.java.net/projects/jigsaw/spec/issues/#CyclicDependences
		 */
		Class<?> declaringClass = method.getDeclaringClass();
		MethodUtil.class.getModule().addReads(declaringClass.getModule());
		return MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup())
				.unreflectSpecial(method, declaringClass);
	}

}
