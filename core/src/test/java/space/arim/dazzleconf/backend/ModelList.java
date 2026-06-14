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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

abstract class ModelList<E, L extends ModelList<E, L>> {

    private final List<E> value;
    private final boolean mutable;

    ModelList(List<E> value, boolean mutable) {
        this.value = value;
        this.mutable = mutable;
    }

    List<E> value() {
        return value;
    }

    abstract L castSelf();

    abstract L construct(List<E> value, boolean mutable);

    L intoImmut() {
        if (!mutable) {
            return castSelf();
        }
        return construct(copyToImmut(value), false);
    }

    L intoMut() {
        if (mutable) {
            return castSelf();
        }
        return construct(copyToMut(value), true);
    }

    abstract List<E> copyToImmut(List<E> value);

    abstract List<E> copyToMut(List<E> value);

    static class Data extends ModelList<DataEntry, Data> {

        Data(List<DataEntry> value, boolean mutable) {
            super(value, mutable);
        }

        @Override
        Data castSelf() {
            return this;
        }

        @Override
        Data construct(List<DataEntry> value, boolean mutable) {
            return new Data(value, mutable);
        }

        @Override
        List<DataEntry> copyToImmut(List<DataEntry> value) {
            List<DataEntry> immutValue = new ArrayList<>(value.size());
            value.forEach(elem -> immutValue.add(elem.intoImmutDeep()));
            return immutValue;
        }

        @Override
        List<DataEntry> copyToMut(List<DataEntry> value) {
            List<DataEntry> mutValue = new ArrayList<>(value.size());
            value.forEach(elem -> mutValue.add(elem.intoMutDeep()));
            return mutValue;
        }
    }

    static class Path extends ModelList<CharSequence, Path> {

        Path(List<CharSequence> value, boolean mutable) {
            super(value, mutable);
        }

        @Override
        Path castSelf() {
            return this;
        }

        @Override
        Path construct(List<CharSequence> value, boolean mutable) {
            return new Path(value, mutable);
        }

        @Override
        List<CharSequence> copyToImmut(List<CharSequence> value) {
            List<String> copy = new ArrayList<>(value.size());
            for (CharSequence element : value) {
                copy.add(element.toString());
            }
            return List.copyOf(copy);
        }

        @Override
        List<CharSequence> copyToMut(List<CharSequence> value) {
            return new LinkedList<>(value);
        }
    }
}
