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
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.reflect.TypeToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationTest {

    @Test
    public void simple() {
        Configuration<HelloWorld> config = Configuration.defaultBuilder(HelloWorld.class).build();
        assertEquals("hi", config.loadDefaults().helloThere());

        DataTree.Mut sourceTree = new DataTree.Mut();
        sourceTree.set("helloThere", new DataEntry("goodbye"));
        HelloWorld loadedFromSource = config.readFrom(sourceTree.intoImmut()).getOrThrow();
        assertEquals("goodbye", loadedFromSource.helloThere());
        {
            DataTree.Mut writeBack = new DataTree.Mut();
            config.writeTo(loadedFromSource, writeBack);
            assertEquals(sourceTree, writeBack);
        }
        DataTree.Mut writeCustom = new DataTree.Mut();
        writeCustom.set("helloThere", new DataEntry("bye"));
        DataTree.Mut output = new DataTree.Mut();
        config.writeTo(new HelloWorld() {
            @Override
            public String helloThere() {
                return "bye";
            }
        }, output);
        assertEquals(writeCustom, output);
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
        assertEquals(List.of(), config.loadDefaults().testList());

        List<String> testList = List.of("hi", "nope", "yes");
        DataTree.Mut sourceTree = new DataTree.Mut();
        sourceTree.set("testList", new DataEntry(List.of(new DataEntry("hi"), new DataEntry("nope"), new DataEntry("yes"))));
        GenericWorld<String> loadedFromSource = config.readFrom(sourceTree).getOrThrow();
        assertEquals(testList, loadedFromSource.testList());
        {
            DataTree.Mut writeBack = new DataTree.Mut();
            config.writeTo(loadedFromSource, writeBack);
            assertEquals(sourceTree, writeBack);
        }
        DataTree.Mut writeCustom = new DataTree.Mut();
        writeCustom.set("testList", new DataEntry(List.of(new DataEntry("1"), new DataEntry("2"))));
        DataTree.Mut output = new DataTree.Mut();
        config.writeTo(new GenericWorld<>() {
            @Override
            public List<String> testList() {
                return List.of("1", "2");
            }
        }, output);
        assertEquals(writeCustom, output);
    }

    public interface GenericWorld<T> {

        default List<T> testList() {
            return List.of();
        }
    }

    @Test
    public void inherited() {
        Configuration<InheritedWorld> config = Configuration.defaultBuilder(InheritedWorld.class).build();
        assertEquals("hi", config.loadDefaults().helloThere());
        assertEquals(List.of(), config.loadDefaults().testList());

        List<Integer> testList = List.of(1, 3);
        DataTree.Mut sourceTree = new DataTree.Mut();
        sourceTree.set("helloThere", new DataEntry("goodbye"));
        sourceTree.set("testList", new DataEntry(List.of(new DataEntry(1), new DataEntry(3))));
        InheritedWorld loadedFromSource = config.readFrom(sourceTree).getOrThrow();
        assertEquals("goodbye", loadedFromSource.helloThere());
        assertEquals(testList, loadedFromSource.testList());
        {
            DataTree.Mut writeBack = new DataTree.Mut();
            config.writeTo(loadedFromSource, writeBack);
            assertEquals(sourceTree, writeBack);
        }
        DataTree.Mut writeCustom = new DataTree.Mut();
        writeCustom.set("helloThere", new DataEntry("bye"));
        writeCustom.set("testList", new DataEntry(List.of(new DataEntry(4), new DataEntry(5))));
        DataTree.Mut output = new DataTree.Mut();
        config.writeTo(new InheritedWorld() {
            @Override
            public String helloThere() {
                return "bye";
            }

            @Override
            public List<Integer> testList() {
                return List.of(4, 5);
            }
        }, output);
        assertEquals(writeCustom, output);
    }

    public interface InheritedWorld extends GenericWorld<Integer>, HelloWorld {}

}
