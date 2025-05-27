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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ImmutabilityGuard<V, S> implements AutoCloseable {

    private final V value;
    private final S initialSnapshot;
    private final List<ImmutabilityGuard<V, S>> addedValues = new ArrayList<>();

    @SuppressWarnings({"resource", "unchecked"})
    protected ImmutabilityGuard(V value, V[] extra) {
        this.value = value;
        this.initialSnapshot = takeSnapshot(value);

        if (extra.length != 0) {
            V[] dummyArray = (V[]) Array.newInstance(extra.getClass().getComponentType(), 0);
            for (V append : extra) {
                this.addedValues.add(new AddValue(append, dummyArray));
            }
        }
    }

    protected abstract S takeSnapshot(V value);

    protected <E> void testImmutable(List<E> list, E dummyElem) {
        assertThrows(UnsupportedOperationException.class, () -> {
            list.add(null);
            list.add(dummyElem);
            list.add(0, null);
            list.add(0, dummyElem);
            list.remove(0);
            list.remove(dummyElem);
            list.set(0, dummyElem);
            list.clear();
        });
        assertDoesNotThrow(list::size);
        assertDoesNotThrow(() -> list.stream().toList());
    }

    class AddValue extends ImmutabilityGuard<V, S> {

        protected AddValue(V value, V[] empty) {
            super(value, empty);
        }

        @Override
        protected S takeSnapshot(V value) {
            return ImmutabilityGuard.this.takeSnapshot(value);
        }
    }

    private void checkUnchanged() {
        S latestSnapshot = takeSnapshot(value);
        assertEquals(initialSnapshot, latestSnapshot);
    }

    @Override
    public void close() {
        checkUnchanged();
        addedValues.forEach(ImmutabilityGuard::checkUnchanged);
    }

}
