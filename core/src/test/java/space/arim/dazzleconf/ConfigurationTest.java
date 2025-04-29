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

package space.arim.dazzleconf;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.DataTreeMut;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationTest {

    @Test
    public void genericConfig() {
        Configuration<GenericConfig<String>> config = Configuration
                .defaultBuilder(new TypeToken<GenericConfig<String>>() {})
                .build();
        List<String> testList = List.of("hi", "nope", "yes");
        DataTreeMut sourceTree = new DataTreeMut();
        sourceTree.set("someType", new DataTree.Entry(testList));
        assertEquals(testList, config.readFrom(sourceTree).getOrThrow().testList());
    }

    public interface GenericConfig<T> {

        default List<T> testList() {
            return List.of();
        }
    }
}
