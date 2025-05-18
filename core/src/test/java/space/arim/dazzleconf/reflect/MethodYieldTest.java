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
import space.arim.dazzleconf2.reflect.MethodId;
import space.arim.dazzleconf2.reflect.MethodYield;
import space.arim.dazzleconf2.reflect.ReifiedType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MethodYieldTest {

    private final MethodId sample = new MethodId("sample", ReifiedType.Annotated.unannotated(String.class), new ReifiedType[0], false);

    public String sample() {
        return "sample";
    }

    @Test
    public void valuesFor() {
        MethodYield methodYield = new MethodYield();
        methodYield.addValue(MethodYieldTest.class, sample, "s");
        assertEquals(Map.of(), methodYield.valuesFor(/* sole superclass */ Object.class));
        assertEquals(Map.of(sample, "s"), methodYield.valuesFor(MethodYieldTest.class));
    }

    @Test
    public void clearValues() {
        MethodYield methodYield = new MethodYield();
        methodYield.addValue(MethodYieldTest.class, sample, "my value");
        methodYield.clearValues();
        assertEquals(Map.of(), methodYield.valuesFor(MethodYieldTest.class));
    }

    @Test
    public void constructFromOther() {
        MethodYield original = new MethodYield();
        original.addValue(MethodYieldTest.class, sample, "s");
        MethodYield copy = new MethodYield(original);
        assertEquals(Map.of(sample, "s"), copy.valuesFor(MethodYieldTest.class));
    }

    @Test
    public void constructFromOtherCannotMutate() {
        MethodYield original = new MethodYield();
        original.addValue(MethodYieldTest.class, sample, "s");
        MethodYield copy = new MethodYield(original);
        assertEquals(Map.of(sample, "s"), copy.valuesFor(MethodYieldTest.class));
        copy.clearValues();
        assertEquals(Map.of(sample, "s"), original.valuesFor(MethodYieldTest.class));
    }

    @Test
    public void constructFromOtherCannotBeMutated() {
        MethodYield original = new MethodYield();
        original.addValue(MethodYieldTest.class, sample, "s");
        MethodYield copy = new MethodYield(original);
        assertEquals(Map.of(sample, "s"), copy.valuesFor(MethodYieldTest.class));
        original.clearValues();
        assertEquals(Map.of(sample, "s"), copy.valuesFor(MethodYieldTest.class));
    }

    @Test
    public void equality() {
        EqualsVerifier.forClass(MethodYield.class)
                .withPrefabValues(MethodId.class, sample, new MethodId("equality", ReifiedType.Annotated.unannotated(void.class), new ReifiedType[0], true))
                .withPrefabValues(ReifiedType[].class, new ReifiedType.Annotated[0], new ReifiedType.Annotated[] {ReifiedType.Annotated.unannotated(void.class)})
                .verify();
    }

    @Test
    public void toStringTest() {
        MethodYield methodYield = new MethodYield();
        methodYield.addValue(MethodYieldTest.class, sample, "myvalue");
        assertTrue(methodYield.toString().contains("myvalue"));
    }
}
