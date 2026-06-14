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
import space.arim.dazzleconf.backend.mutmodel.GenerateAction;
import space.arim.dazzleconf.backend.mutmodel.ModifyAction;
import space.arim.dazzleconf.backend.mutmodel.ObserveAction;
import space.arim.dazzleconf.backend.mutmodel.ProduceAction;
import space.arim.dazzleconf.backend.mutmodel.SemiExhaustiveMutabilityTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataListBulkMutabilityIT extends SemiExhaustiveMutabilityTesting<DataList, ModelList.Data> {

    private static final ActionBag<DataList, ModelList.Data> ACTION_BAG = new ActionBag.Builder<DataList, ModelList.Data>()
            .generate(
                    new GenerateAction<>("new Immut()", Collections.singletonList((Void) null),
                            (arg) -> new DataList.Immut(),
                            (arg) -> new ModelList.Data(List.of(), false),
                            false),
                    new GenerateAction<>("new Mut()", Collections.singletonList((Void) null),
                            (arg) -> new DataList.Mut(),
                            (arg) -> new ModelList.Data(new ArrayList<>(), true),
                            true)
            )
            .modify(
                    new ModifyAction<>("add", DataUniverse.DATA_ENTRIES,
                            (dataList, entry) -> ((DataList.Mut) dataList).add(entry),
                            (list, entry) -> list.value().add(entry)),
                    new ModifyAction<>("clear", Collections.singletonList((Void) null),
                            ((dataList, unused) -> ((DataList.Mut) dataList).clear()),
                            (list, unused) -> list.value().clear())
            )
            .observe(
                    ObserveAction.aggregate("forEach", (dataList, list) -> {
                        List<DataEntry> collected = new ArrayList<>();
                        dataList.forEach(collected::add);
                        assertEquals(list.value(), collected);
                    }),
                    ObserveAction.aggregate("size", (dataList, list) -> {
                        assertEquals(list.value().size(), dataList.size());
                    })
            )
            .produce(
                    ProduceAction.<DataList, ModelList.Data>aggregate("intoImmut", DataList::intoImmut, ModelList::intoImmut, false),
                    ProduceAction.<DataList, ModelList.Data>aggregate("intoMut", DataList::intoMut, ModelList::intoMut, true)
            )
            .build();

    public DataListBulkMutabilityIT() {
        super(137114453687073140L, ACTION_BAG);
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
