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

package space.arim.dazzleconf.internal.error;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ElementaryTypeTest {

  @ParameterizedTest
  @EnumSource(ElementaryType.class)
  public void examples(ElementaryType elementaryType) {
    String[] examples = assertDoesNotThrow(elementaryType::examples);
    assertNotNull(examples);
    for (String example : examples) {
      assertNotNull(example);
    }
  }

  @ParameterizedTest
  @EnumSource(ElementaryType.class)
  public void toString(ElementaryType elementaryType) {
    String toString = assertDoesNotThrow(elementaryType::toString);
    assertNotNull(toString);
  }
}
