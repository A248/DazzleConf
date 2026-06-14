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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import space.arim.dazzleconf.backend.mutmodel.ActionBag;
import space.arim.dazzleconf.backend.mutmodel.SemiExhaustiveMutabilityTesting;
import space.arim.dazzleconf.backend.mutmodel.GenerateAction;
import space.arim.dazzleconf.backend.mutmodel.ModifyAction;
import space.arim.dazzleconf.backend.mutmodel.ObserveAction;
import space.arim.dazzleconf.backend.mutmodel.ProduceAction;
import space.arim.dazzleconf.backend.mutmodel.TransferAction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataTreeBulkMutabilityIT extends SemiExhaustiveMutabilityTesting<DataTree, ModelMap> {

    private static final ActionBag<DataTree, ModelMap> ACTION_BAG = new ActionBag.Builder<DataTree, ModelMap>()
            .generate(
                    new GenerateAction<>("new Immut()", Collections.singletonList((Void) null),
                            (arg) -> new DataTree.Immut(),
                            (arg) -> new ModelMap(Map.of(), false),
                            false),
                    new GenerateAction<>("new Mut()", Collections.singletonList((Void) null),
                            (arg) -> new DataTree.Mut(),
                            (arg) -> new ModelMap(new HashMap<>(), true),
                            true)
            )
            .modify(
                    new ModifyAction<>("put", DataUniverse.DATA_TREE_ENTRIES,
                            (dataTree, entry) -> ((DataTree.Mut) dataTree).put(entry.getKey(), entry.getValue()),
                            (map, entry) -> map.value().put(entry.getKey(), entry.getValue())),
                    new ModifyAction<>("remove", DataUniverse.SCALAR_KEYS,
                            (dataTree, key) -> ((DataTree.Mut) dataTree).remove(key),
                            (map, key) -> map.value().remove(key)),
                    new ModifyAction<>("clear", Collections.singletonList((Void) null),
                            ((dataTree, unused) -> ((DataTree.Mut) dataTree).clear()),
                            (map, unused) -> map.value().clear())
            )
            .observe(
                    new ObserveAction<>("get", DataUniverse.SCALAR_KEYS, (dataTree, map, key) -> {
                        assertEquals(dataTree.get(key), map.value().get(key));
                    }),
                    ObserveAction.aggregate("forEach", (dataTree, map) -> {
                        Map<Object, DataEntry> collected = new HashMap<>();
                        dataTree.forEach(collected::put);
                        assertEquals(map.value(), collected);
                    }),
                    ObserveAction.aggregate("size", (dataTree, map) -> {
                        assertEquals(map.value().size(), dataTree.size());
                    })
            )
            .produce(
                    ProduceAction.<DataTree, ModelMap>aggregate("intoImmut", DataTree::intoImmut, ModelMap::intoImmut, false),
                    ProduceAction.<DataTree, ModelMap>aggregate("intoMut", DataTree::intoMut, ModelMap::intoMut, true)
            )
            .transfer(
                    new TransferAction<>(
                            "setAll",
                            (from, to) -> ((DataTree.Mut) to).setAll(from),
                            (from, to) -> to.setAll(from)
                    ),
                    new TransferAction<>(
                            "putAll",
                            (from, to) -> ((DataTree.Mut) to).putAll(from),
                            (from, to) -> to.value().putAll(from.value())
                    )
            )
            .build();

    public DataTreeBulkMutabilityIT() {
        super(137114453687073139L, ACTION_BAG);
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
