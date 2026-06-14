/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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

package space.arim.dazzleconf.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import space.arim.dazzleconf.backend.mutmodel.ActionBag;
import space.arim.dazzleconf.backend.mutmodel.GenerateAction;
import space.arim.dazzleconf.backend.mutmodel.ModifyAction;
import space.arim.dazzleconf.backend.mutmodel.ObserveAction;
import space.arim.dazzleconf.backend.mutmodel.ProduceAction;
import space.arim.dazzleconf.backend.mutmodel.SemiExhaustiveMutabilityTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyPathBulkMutabilityIT extends SemiExhaustiveMutabilityTesting<KeyPath, ModelList.Path> {

    private static final List<CharSequence> KEY_ARGS = List.of(
            "key1", "key2", new StringBuilder("no-modify-1"), new StringBuffer("no-modify-2")
    );

    @AfterEach
    public void checkNotModified() {
        assertEquals("no-modify-1", KEY_ARGS.get(2).toString());
        assertEquals("no-modify-2", KEY_ARGS.get(3).toString());
    }

    private static final ActionBag<KeyPath, ModelList.Path> ACTION_BAG = new ActionBag.Builder<KeyPath, ModelList.Path>()
            .generate(
                    new GenerateAction<>(
                            "new Immut()", List.of(new String[] {}, new String[] {"key1"}, new String[] {"key1", "key2"}),
                            KeyPath.Immut::new,
                            (path) -> new ModelList.Path(Arrays.asList(path), false),
                            false),
                    new GenerateAction<>(
                            "new Mut()", List.of(new String[] {}, new String[] {"key1"}, new String[] {"key1", "key2"}),
                            KeyPath.Mut::new,
                            (path) -> new ModelList.Path(new LinkedList<>(Arrays.asList(path)), true),
                            true)
            )
            .modify(
                    new ModifyAction<>(
                            "addFront", KEY_ARGS,
                            (keyPath, key) -> ((KeyPath.Mut) keyPath).addFront(key),
                            (list, key) -> list.value().addFirst(key)
                    ),
                    new ModifyAction<>(
                            "addBack", KEY_ARGS,
                            (keyPath, key) -> ((KeyPath.Mut) keyPath).addBack(key),
                            (list, key) -> list.value().add(key)
                    )
            )
            .observe(
                    ObserveAction.aggregate("forEach", (keyPath, list) -> {
                        List<CharSequence> value = new ArrayList<>();
                        keyPath.forEach(value::add);
                        assertEquals(list.value(), value);
                    }),
                    ObserveAction.aggregate("intoParts", (keyPath, list) -> {
                        List<CharSequence> collected = Arrays.asList(keyPath.intoParts());
                        assertEquals(list.value(), collected);
                    }),
                    ObserveAction.aggregate("intoPartsList", (keyPath, list) -> {
                        List<CharSequence> collected = keyPath.intoPartsList();
                        assertEquals(list.value(), collected);
                    }),
                    ObserveAction.aggregate("size", (keyPath, list) -> {
                        assertEquals(list.value().size(), keyPath.size());
                    }),
                    ObserveAction.aggregate("printString", (keyPath, list) -> {
                        String printed = keyPath.printString();
                        assertEquals(String.join(".", list.value()), printed);
                    })
            )
            .produce(
                    ProduceAction.<KeyPath, ModelList.Path>aggregate("intoImmut", KeyPath::intoImmut, ModelList::intoImmut, false),
                    ProduceAction.<KeyPath, ModelList.Path>aggregate("intoMut", KeyPath::intoMut, ModelList::intoMut, true)
            )
            .build();

    public KeyPathBulkMutabilityIT() {
        super(137114453687073141L, ACTION_BAG);
    }

    @TestFactory
    public Stream<DynamicTest> testWithTwo() {
        return testAll(2, 6);
    }

    @TestFactory
    public Stream<DynamicTest> testWithThree() {
        return testAll(3, 7);
    }
}
