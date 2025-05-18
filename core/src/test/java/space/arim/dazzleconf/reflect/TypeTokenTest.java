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

package space.arim.dazzleconf.reflect;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.reflect.ReifiedType;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.lang.reflect.AnnotatedElement;

public class TypeTokenTest {

    @Test
    public void equality() throws NoSuchMethodException {
        EqualsVerifier
                .forClass(TypeToken.class)
                .withPrefabValues(ReifiedType[].class, ReifiedType.Annotated.EMPTY_ARRAY, new ReifiedType.Annotated[] {ReifiedType.Annotated.unannotated(void.class)})
                .withPrefabValues(AnnotatedElement.class, getClass().getConstructor(), getClass().getMethod("equality"))
                .verify();
    }
}
