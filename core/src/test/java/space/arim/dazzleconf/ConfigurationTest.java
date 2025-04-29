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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.Configuration;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ConfigurationTest {

    @Test
    public void simple() {
        Configuration<HelloWorld> config = Configuration
                .defaultBuilder(HelloWorld.class)
                .build();
        assertEquals("hi", config.loadDefaults().helloThere());

        DataTree.Mut sourceTree = new DataTree.Mut();
        sourceTree.set("helloThere", new DataEntry("goodbye"));

        assertEquals("goodbye", config.readFrom(sourceTree).getOrThrow().helloThere());
    }

    public interface HelloWorld {

        default String helloThere() {
            return "hi";
        }
    }

    @Test
    public void generic() {
        Configuration<GenericWorld<String>> config = Configuration
                .defaultBuilder(new TypeToken<GenericWorld<String>>() {})
                .build();
        List<String> testList = List.of("hi", "nope", "yes");
        DataTree.Mut sourceTree = new DataTree.Mut();
        sourceTree.set("testList", new DataEntry(testList));
        assertEquals(testList, config.readFrom(sourceTree).getOrThrow().testList());
    }

    public interface GenericWorld<T> {

        default List<T> testList() {
            return List.of();
        }
    }
}
