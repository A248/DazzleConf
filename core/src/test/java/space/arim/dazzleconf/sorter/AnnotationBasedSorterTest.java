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

package space.arim.dazzleconf.sorter;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationBasedSorterTest {

	@Test
	public void sort() {
		List<String> entries = Arrays.stream(SortableConfig.class.getDeclaredMethods())
				.map(SimpleEntry::new).sorted(new AnnotationBasedSorter())
				.map(SimpleEntry::getMethod).map(Method::getName).toList();
		assertEquals(List.of("two", "four", "five", "thirteen"), entries);
	}

	public interface SortableConfig {

		@AnnotationBasedSorter.Order(4)
		String four();

		@AnnotationBasedSorter.Order(13)
		String thirteen();

		@AnnotationBasedSorter.Order(2)
		String two();

		@AnnotationBasedSorter.Order(5)
		String five();

	}

	private static final class SimpleEntry implements SortableConfigurationEntry {

		private final Method method;

		SimpleEntry(Method method) {
			this.method = Objects.requireNonNull(method, "method");
		}

		@Override
		public Method getMethod() {
			return method;
		}

		@Override
		public String getKey() {
			return method.getName();
		}

		@Override
		public List<String> getComments() {
			return List.of();
		}
	}

}
